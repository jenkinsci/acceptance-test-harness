package org.jenkinsci.test.acceptance.controller;

import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jclouds.compute.domain.Template;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;

import java.io.File;
import java.io.IOException;

import static com.google.common.base.Charsets.UTF_8;

/**
 * @author Vivek Pandey
 */
@Singleton
public class Ec2Provider extends MachineProvider {

    @Inject
    private Ec2Config config;


    @Inject
    public Ec2Provider(Ec2Config config){
        super("aws-ec2", config.getKey(), config.getSecret());
        this.config = config;

    }

    @Override
    public Template getTemplate() throws IOException{
        Template template =  computeService.templateBuilder().imageId(config.getRegion()+"/"+config.getImageId()).
                locationId(config.getRegion()).hardwareId(config.getInstanceType()).
                build();

        String publicKey = Files.toString(new File(System.getProperty("user.home") + "/.ssh/id_rsa.pub"), UTF_8);

        template.getOptions().as(EC2TemplateOptions.class).authorizePublicKey(publicKey).keyPair(config.getKeyPairName()).
                securityGroups(config.getSecurityGroups()).inboundPorts(config.getInboundPorts()).overrideLoginUser(config.getUser());
        return template;
    }


}
