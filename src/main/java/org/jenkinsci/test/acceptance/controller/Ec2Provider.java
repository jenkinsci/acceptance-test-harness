package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.ec2.EC2Api;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;
import org.jclouds.net.domain.IpProtocol;
import org.jenkinsci.test.acceptance.guice.SubWorld;
import org.jenkinsci.test.acceptance.guice.WorldCleaner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;

import static com.google.common.base.Charsets.UTF_8;

/**
 * @author Vivek Pandey
 */
@Singleton
public class Ec2Provider extends JcloudsMachineProvider {

    private final Ec2Config config;

    @Inject
    @Named("publicKeyAuthenticator")
    private  Authenticator authenticator;

    @Inject
    private SshKeyPair keyPair;

    @Inject(optional=true)
    private SubWorld subworld;

    @Inject
    public Ec2Provider(Ec2Config config, WorldCleaner cleaner){
        super("aws-ec2", config.getKey(), config.getSecret());
        this.config = config;
        cleaner.addTask(this);
    }

    @Override
    public void postStartupSetup(NodeMetadata node) {
        Ssh ssh=null;
        try {
            ssh = new Ssh(config.getUser(),node.getPublicAddresses().iterator().next());
            authenticator().authenticate(ssh.getConnection());
            ssh.getConnection().exec(String.format("pkill -u $(id -u %s)", config.getUser()), System.out);
        } catch (IOException | InterruptedException e) {
            logger.error(e.getMessage());
        }finally {
            if(ssh != null){
                ssh.destroy();
            }
        }
    }

    @Override
    public int[] getAvailableInboundPorts() {
        return config.getInboundPorts();
    }

    @Override
    public Authenticator authenticator() {
        return authenticator;
    }

    @Override
    protected String getGroupName() {
        String name = (subworld != null)?subworld.getName() : null;
        return getGroupName(name);
    }

    @Override
    public Template getTemplate() throws IOException {
        if(config.getSecurityGroups().size() > 0){
            EC2Api client = contextBuilder.buildApi(EC2Api.class);
            for(String sg : config.getSecurityGroups()){
                try{
                client.getSecurityGroupApi().get().createSecurityGroupInRegion(config.getRegion(), sg, sg);
                client.getSecurityGroupApi().get().authorizeSecurityGroupIngressInRegion(config.getRegion(), sg,
                        IpProtocol.TCP, config.getInboundPorts()[0], config.getInboundPorts()[config.getInboundPorts().length - 1], "0.0.0.0/0");
                client.getSecurityGroupApi().get().authorizeSecurityGroupIngressInRegion(config.getRegion(), sg,
                        IpProtocol.TCP, 22,22,"0.0.0.0/0");
                }catch(IllegalStateException e){
                    // Lets ignore it, most likely its due to existing security roles, it might fail
                    logger.error("Failed to create and authorize IP ports in security group"+e.getMessage());
                }
            }
        }

        Template template =  computeService.templateBuilder().imageId(config.getRegion()+"/"+config.getImageId()).
                locationId(config.getRegion()).hardwareId(config.getInstanceType()).
                build();

        String publicKey = keyPair.readPublicKey();

        EC2TemplateOptions options = template.getOptions().as(EC2TemplateOptions.class);
        options.authorizePublicKey(publicKey)
                .securityGroups(config.getSecurityGroups())
                .inboundPorts(config.getInboundPorts())
                .overrideLoginUser(config.getUser());

        // FIXME: the key pair name we set here just doesn't get used at all. as of 1.6.0
        // in CreateKeyPairPlacementAndSecurityGroupsAsNeededAndReturnRunOptions#createNewKeyPairUnlessUserSpecifiedOtherwise
        // it goes through "and(hasPublicKeyMaterial, or(doesntNeedSshAfterImportingPublicKey, hasLoginCredential))"
        // check, which evaluates to true (because doesntNeedSshAfterImportingPublicKey is true), and it ends up going
        // through importExistingKeyPair.apply(...) that doesn't look at the key pair name we prefer.
        String kn = config.getKeyPairName();
        if (kn==null)
            try {
                kn = "jenkins-test-"+keyPair.getFingerprint().substring(0,11);
            } catch (GeneralSecurityException e) {
                throw new IOException("Failed to compute key fingerprint of",e);
            }
        options.keyPair(kn);

        //tag
        //Tag the provisioned instance with md5(CWD+HOST_IP_ADDRESS)
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(String.format("%s%s%s",System.getProperty("user.dir"),System.currentTimeMillis(), new SecureRandom().nextLong()).getBytes(UTF_8));
            byte[] digest = md.digest();
            String tag = DatatypeConverter.printHexBinary(digest);
            options.tags(Collections.singletonList(tag));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return template;
    }


    private static final Logger logger = LoggerFactory.getLogger(Ec2Provider.class);
}
