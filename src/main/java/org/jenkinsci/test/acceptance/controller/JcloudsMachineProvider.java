package org.jenkinsci.test.acceptance.controller;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.apis.Apis;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.Providers;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.Iterables.contains;
import static org.jclouds.compute.config.ComputeServiceProperties.TIMEOUT_SCRIPT_COMPLETE;
import static org.jclouds.compute.predicates.NodePredicates.inGroup;

/**
 * jcloud based cloud controller. Implementation is partly based on jcloud compute-basic example.
 *
 * @author Vivek Pandey
 */
@Singleton
public abstract class JcloudsMachineProvider implements MachineProvider,Closeable {
    private static final Map<String, ApiMetadata> allApis = Maps.uniqueIndex(Apis.viewableAs(ComputeServiceContext.class),
            Apis.idFunction());

    private static final Map<String, ProviderMetadata> appProviders = Maps.uniqueIndex(Providers.viewableAs(ComputeServiceContext.class),
            Providers.idFunction());

    private static final Set<String> supportedProviders = ImmutableSet.copyOf(Iterables.concat(appProviders.keySet(), allApis.keySet()));

    protected final ComputeService computeService;

    protected final ContextBuilder contextBuilder;

    private final String groupName="jenkins-test";
    private final String provider;
    private volatile BlockingQueue<Machine> queue=null;
    private final AtomicInteger provisionedMachineCount = new AtomicInteger();

    @Inject(optional = true)
    @Named("maxNumOfMachines")
    private int maxNumOfMachines=1;

    @Inject(optional = true)
    @Named("minNumOfMachines")
    private int minNumOfMachines=1;


    private final Map<String, Machine> machines = new ConcurrentHashMap<>();

    protected JcloudsMachineProvider(String provider, String identity, String credential) {
        logger.info("Initializing JCloudMachineProvider...");
        if(!contains(supportedProviders, provider)){
            throw new RuntimeException(String.format("Provider %s is not supported. Supported providers: %s",provider, Arrays.toString(supportedProviders.toArray())));
        }

        this.provider = provider;

        this.contextBuilder = initComputeService(provider, identity, credential);
        this.computeService =  contextBuilder.buildView(ComputeServiceContext.class).getComputeService();
    }

    @Override
    public Machine get() {
        if(provisionedMachineCount.get() == 0){
            initializeMachines();
        }
        if(queue.peek() != null){
            return queue.poll();
        }else{
            throw new AssertionError(String.format("All %s machine instances are in use", provisionedMachineCount.get()));
        }
    }

    private void initializeMachines(){
        BlockingQueue bq = queue;
        if(bq == null){
            synchronized (this){
                bq = queue;
                if(bq == null){
                    logger.info(String.format("Setting up %s  %s machines...", maxNumOfMachines, provider));
                    queue = bq =  new LinkedBlockingQueue<>(maxNumOfMachines);
                }
            }
        }

        Set<? extends NodeMetadata> nodes = createNewNodes(minNumOfMachines, maxNumOfMachines);

        //TODO make it configurable
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        //setup new machines
        for(NodeMetadata node:nodes){
            executorService.submit(new MachineSanitizer(node));
        }


        try {
            executorService.shutdown();
            executorService.awaitTermination(2, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            terminateAllNodes();
            throw new RuntimeException("Failed to setup machines. ",e);
        }
        provisionedMachineCount.set(nodes.size());
        for(NodeMetadata node:nodes){
            Machine m = new JcloudsMachine(this,node);
            queue.add(m);
            machines.put(node.getId(),m);
        }
    }


    /**
     * Get the JClouds Template object to be used to provisioned with the machine
     * @return
     */
    public abstract Template getTemplate() throws IOException;

    /**
     * Each JCloud machine provider gets a chance to perform post startup setups, for example, authorize ports, tag instances etc.
     */
    public abstract  void postStartupSetup(NodeMetadata node);


    /**
     * Gives all available inbound ports
     */
    public abstract int[] getAvailableInboundPorts();

    public void offer(Machine m){
        logger.info(String.format("%s machine %s offered, will be recycled", provider, m.getId()));
        Machine machine = machines.get(m.getId());
        if(machine != null){
            machines.remove(m.getId());
            queue.add(machine);
        }else{
            throw new IllegalStateException("Did not find machine corresponding to offered id: "+m.getId());
        }
    }

    @Override
    public void close() throws IOException {
        terminateAllNodes();
    }

    /**
     * Options to be used during instance provisioning.
     */
    public  Properties getOptions(){
        Properties properties = new Properties();
        long scriptTimeout = TimeUnit.MILLISECONDS.convert(20, TimeUnit.MINUTES);
        properties.setProperty(TIMEOUT_SCRIPT_COMPLETE, scriptTimeout + "");
        return properties;
    }

    private Set<? extends NodeMetadata> createNewNodes(int minNumOfMachines, int maxNumOfMachines){
        Template template;
        try {
            template = getTemplate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Set<? extends NodeMetadata> nodes;
        try {
            nodes = computeService.createNodesInGroup(groupName, maxNumOfMachines, template);
        } catch (RunNodesException e) {
            logger.error(String.format("%s out of %s machines provisioned.",e.getSuccessfulNodes().size()));
            int numSuccessfulMachines = e.getSuccessfulNodes().size();
            if(numSuccessfulMachines < minNumOfMachines){
                logger.error("Requested minimum of %s machines, got %s. Aborting, all provisioned machines will be terminated", minNumOfMachines,numSuccessfulMachines);
                terminateAllNodes();
                throw new RuntimeException(e);
            }
            nodes = e.getSuccessfulNodes();
        }

        return nodes;
    }

    private void terminateAllNodes(){
        computeService.destroyNodesMatching(inGroup(groupName));
    }


    private ContextBuilder initComputeService(String provider, String identity, String credential) {
        Iterable<Module> modules = ImmutableSet.<Module>of(
                new SshjSshClientModule(),
                new SLF4JLoggingModule(),
                new EnterpriseConfigurationModule());

        ContextBuilder contextBuilder =  ContextBuilder.newBuilder(provider)
                .credentials(identity, credential)
                .modules(modules)
                .overrides(getOptions());
        logger.info(String.format("Initializing %s", contextBuilder.getApiMetadata()));

        return contextBuilder;
    }

    private void waitForSsh(String user, String host){
        int timeout = 120000; //2 minute
        long startTime = System.currentTimeMillis();
        while(true){
            logger.info(String.format("Making sure sshd is up on host: %s...",host));
            try {
                if(System.currentTimeMillis() - startTime > timeout){
                    throw new RuntimeException(String.format("ssh failed to work within %s seconds.",timeout/1000));
                }
                Ssh ssh = new Ssh(user, host);
                authenticator().authenticate(ssh.getConnection());
                logger.info("sshd is ready on host: "+host);
                ssh.destroy();
                return;
            } catch (IOException e) {
                try {
                    Thread.sleep(10000); //sleep 10 sec
                } catch (InterruptedException e1) {
                    logger.error(e.getMessage(), e);
                    return; //exit from this loop
                }
            }
        }
    }

    private class MachineSanitizer implements Runnable{
        private final NodeMetadata node;
        private MachineSanitizer(NodeMetadata node) {
            this.node = node;
        }

        @Override
        public void run() {
            try{
                postStartupSetup(node);
                waitForSsh(node.getCredentials().getUser(), node.getPublicAddresses().iterator().next()); //wait for ssh to be ready
            }catch (Exception e){
                String msg = String.format("There was problem in setting up machine: %s, this node will be destroyed.",node.getId());
                logger.error(msg ,e);
                computeService.destroyNode(node.getId());
                throw new RuntimeException(msg,e);
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(MachineProvider.class);
}
