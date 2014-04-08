package org.jenkinsci.test.acceptance.machine;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.jclouds.ContextBuilder;
import org.jclouds.apis.ApiMetadata;
import org.jclouds.apis.Apis;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.enterprise.config.EnterpriseConfigurationModule;
import org.jclouds.logging.slf4j.config.SLF4JLoggingModule;
import org.jclouds.providers.ProviderMetadata;
import org.jclouds.providers.Providers;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.jenkinsci.test.acceptance.Ssh;
import org.jenkinsci.test.acceptance.guice.SubWorld;
import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
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

    private final String provider;
    private volatile BlockingQueue<Machine> queue=null;
    private final AtomicInteger provisionedMachineCount = new AtomicInteger();

    @Inject(optional = true)
    private SubWorld subworld;

    @Inject(optional = true)
    @Named("maxNumOfMachines")
    private int maxNumOfMachines=1;

    @Inject(optional = true)
    @Named("minNumOfMachines")
    private int minNumOfMachines=1;

    @Inject(optional = true)
    @Named("terminateNodesOnExit")
    private boolean terminateNodesOnExit=false;


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
            //get a new machine
            return createNewMachines(1, 1).iterator().next();
        }
    }

    private void initializeMachines(){
        BlockingQueue bq = queue;
        if(bq == null){
            synchronized (this){
                bq = queue;
                if(bq == null){
                    queue = bq =  new LinkedBlockingQueue<>();
                }
            }
        }
        Set<Machine> machines = createNewMachines(minNumOfMachines, maxNumOfMachines);
        for(Machine m:machines){
            queue.add(m);
        }
    }


    /**
     * Every provider should provide a group name under which the machines need to be created
     */
    protected abstract String getGroupName();


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
        if(terminateNodesOnExit) {
            terminateAllNodes();
        }
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

    private Set<Machine> createNewMachines(int minNumOfMachines, int maxNumOfMachines){
        logger.info(String.format("Setting up %s  %s machines...", maxNumOfMachines, provider));

        Template template;
        try {
            template = getTemplate();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Set<? extends NodeMetadata> nodes;
        try {
            nodes = getRunningInstances();
            if(nodes.isEmpty()) { //get new ones
                logger.info(String.format("No running instances found, create %s new machines",maxNumOfMachines));
                nodes = computeService.createNodesInGroup(getGroupName(), maxNumOfMachines, template);
            }
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

        /** Now, wait till machine is up with sshd and  {@link MachineProvider}s have finished any post boot up steps
         *  TODO make it configurable
         */
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

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

        provisionedMachineCount.set(provisionedMachineCount.get()+nodes.size());

        Set<Machine> newMachines = new HashSet<>();
        for(NodeMetadata node:nodes){
            Machine m = new JcloudsMachine(this,node);
            machines.put(node.getId(),m);
            newMachines.add(m);
        }

        return newMachines;
    }

    private Set<NodeMetadata> getRunningInstances(){
        logger.info(String.format("Check if we already got running machines in the security group: %s... ", getGroupName()));
        Set<? extends NodeMetadata> nodeMetadatas = computeService.listNodesDetailsMatching(new Predicate<ComputeMetadata>() {
            @Override
            public boolean apply(ComputeMetadata computeMetadata) {
                return true;
            }
        });

        Set<NodeMetadata> filteredNodes = new HashSet<>();
        for(NodeMetadata nm:nodeMetadatas){
            if(getGroupName().equals(nm.getGroup()) && nm.getStatus() == NodeMetadata.Status.RUNNING){
                logger.info(String.format("Found running machine: %s",getGroupName()));
                filteredNodes.add(nm);
            }
        }
        return filteredNodes;
    }

    private void terminateAllNodes(){
        computeService.destroyNodesMatching(inGroup(getGroupName()));
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
            logger.info(String.format("Making sure sshd is up on host: %s ",host));
            try {
                if(System.currentTimeMillis() - startTime > timeout){
                    throw new RuntimeException(String.format("ssh failed to work within %s seconds.",timeout/1000));
                }
                try (Ssh ssh = new Ssh(host)) {
                    authenticator().authenticate(ssh.getConnection());
                    logger.info("sshd is ready on host: " + host);
                }
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
                waitForSsh(getUserName(node), node.getPublicAddresses().iterator().next()); //wait for ssh to be ready
                postStartupSetup(node);
            }catch (Exception e){
                String msg = String.format("There was problem in setting up machine: %s, this node will be destroyed.",node.getId());
                logger.error(msg ,e);
                computeService.destroyNode(node.getId());
                throw new RuntimeException(msg,e);
            }
        }
    }

    public static String getGroupName(String seed){
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String username = System.getProperty("user.name").toLowerCase();
            String localHost = getLocalHostName().toLowerCase().replace(".","");
            StringBuilder sb = new StringBuilder(username);
            sb.append(localHost).append(getConfigFileContent());
            if(seed != null){
                sb.append(seed);
            }
            md.update(sb.toString().getBytes("UTF-8"));
            byte[] digest = md.digest();
            if(seed != null){
                return String.format("jat-%s-%s-%s-%s",username,localHost,seed,DatatypeConverter.printHexBinary(digest).substring(0,7).toLowerCase());
            }else{
                return String.format("jat-%s-%s-%s",username,localHost,DatatypeConverter.printHexBinary(digest).substring(0,7).toLowerCase());
            }

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getLocalHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            //On MacOS this happens, so get it by forking 'hostname'
            CommandBuilder cb = new CommandBuilder("hostname");
            try {
                ProcessInputStream pis = cb.popen();

                return IOUtil.toString(pis.getInputStream()).trim();

            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }
        }
    }


    private static String getConfigFileContent(){
        String configFile = System.getProperty("CONFIG");
        if(configFile != null){
            try {
                return FileUtils.fileRead(configFile);
            } catch (IOException e) {
                return "config";
            }
        }else{
            return "config";
        }
    }

    private String getUserName(NodeMetadata node){
        return (node.getCredentials() == null) ? "ubuntu" : node.getCredentials().getUser();
    }

//    private static final String EC2_INSTANCE_LOG=System.getProperty("user.home")

    private static final Logger logger = LoggerFactory.getLogger(MachineProvider.class);
}
