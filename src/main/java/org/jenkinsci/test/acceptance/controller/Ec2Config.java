package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author: Vivek Pandey
 */
@Singleton
public class Ec2Config {

    @Inject(optional = true)
    @Named("ec2ConfigFile")
    private String ec2ConfigFile = System.getProperty("user.home")+"/.ssh/ec2_keys";
    /** Ec2 key and secret read from ec2Config file **/
    private String key;

    private String secret;

    @Inject(optional = true)
    @Named("region") //default us-east-1
    private  String region="us-east-1";

    @Inject(optional = true)
    @Named("instanceType") //default m1.small
    private  String instanceType="m1.small";

    @Inject(optional = true)
    @Named("securityGroup") //default jenkins-test
    private List<String> securityGroups= Collections.singletonList("jenkins-test");

    @Inject(optional = true)
    @Named("keyPairName") //default jenkins-test
    private String keyPairName="jenkins-test";

    @Inject(optional = true)
    @Named("imageId")
    private String imageId ="ami-350c295c";//ebs image with ubuntu 10 and jdk6

    private final int[] inboundPorts;

    @Inject(optional = true)
    @Named("inboundPortRange")
    private String inboundPortRange="20000..21000";

    @Inject(optional = true)
    @Named("user")
    private String user="ubuntu";

    public Ec2Config() {
        Properties properties = new Properties();
        File f  = new File(ec2ConfigFile);
        try {
            properties.load(new FileInputStream(f));
            if(properties.getProperty("key") == null){
                throw new RuntimeException(String.format("EC2 config file %s does not have 'key'",ec2ConfigFile));
            }
            if(properties.getProperty("secret") == null){
                throw new RuntimeException(String.format("EC2 config file %s does not have 'secret'",ec2ConfigFile));
            }
            this.key = properties.getProperty("key");
            this.secret = properties.getProperty("secret");
        } catch (IOException e) {
            throw new RuntimeException(String.format("EC2 credential configuration file %s does not exist", ec2ConfigFile));
        }

        String[] ports = inboundPortRange.split("\\.\\.");
        if(ports.length == 0){
            throw new RuntimeException(String.format("inboundPortRange %s must be of format 'from_port..to_port', from_port>to_port and from_port >= %s", inboundPortRange, JcloudsMachine.BEGINNING_PORT));
        }
        int from = Integer.parseInt(ports[0]);
        int to;
        if(ports.length == 1){
            to = from+1;
        }else{
            to = Integer.parseInt(ports[1]);
        }

        if(to < from){
            throw new RuntimeException(String.format("inboundPortRange %s must be in increasing order, 'from_port..to_port' to_port > from_port. ",inboundPortRange));
        }

        inboundPorts = new int[to-from + 1];
        for(int i=0;i<inboundPorts.length;i++){
            inboundPorts[i] = from++;
        }
    }

    public String getRegion() {
        return region;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public List<String> getSecurityGroups() {
        return securityGroups;
    }

    public String getKeyPairName() {
        return keyPairName;
    }

    public String getKey() {
        return key;
    }

    public String getSecret() {
        return secret;
    }

    public int[] getInboundPorts() {
        return inboundPorts;
    }

    public String getUser() {
        return user;
    }

    public String getImageId() {
        return imageId;
    }
}
