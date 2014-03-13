package org.jenkinsci.test.acceptance.controller;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.jclouds.compute.domain.Template;
import org.jclouds.ec2.EC2Client;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;
import org.jclouds.ec2.domain.IpProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;

/**
 * @author Vivek Pandey
 */
@Singleton
public class Ec2Provider extends JcloudsMachineProvider {

    @Inject
    private Ec2Config config;

    @Inject
    @Named("publicKeyAuthenticator")
    private  Authenticator authenticator;

    @Inject
    @Named("publicKeyFile")
    private  File publicKeyFile;

    @Inject
    public Ec2Provider(Ec2Config config){
        super("aws-ec2", config.getKey(), config.getSecret());
        this.config = config;

    }

    @Override
    public void authorizeInboundPorts() {
        if(config.getSecurityGroups().size() > 0){
            EC2Client client = contextBuilder.buildApi(EC2Client.class);

            try{
            client.getSecurityGroupServices().authorizeSecurityGroupIngressInRegion(config.getRegion(), config.getSecurityGroups().get(0),
                    IpProtocol.TCP, config.getInboundPorts()[0],config.getInboundPorts()[config.getInboundPorts().length - 1],"0.0.0.0/0");
            }catch(IllegalStateException e){
                // Lets ignore it, most likely its due to existing security roles, it might fail
                logger.error("Failed to authorize IP ports in security group"+e.getMessage());
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
    public Template getTemplate() throws IOException {
        Template template =  computeService.templateBuilder().imageId(config.getRegion()+"/"+config.getImageId()).
                locationId(config.getRegion()).hardwareId(config.getInstanceType()).
                build();

        String publicKey = Files.toString(publicKeyFile, UTF_8);

        template.getOptions().as(EC2TemplateOptions.class).authorizePublicKey(publicKey).keyPair(config.getKeyPairName()).
                securityGroups(config.getSecurityGroups()).inboundPorts(config.getInboundPorts()).overrideLoginUser(config.getUser());
        return template;
    }


    private static final Logger logger = LoggerFactory.getLogger(Ec2Provider.class);
}
