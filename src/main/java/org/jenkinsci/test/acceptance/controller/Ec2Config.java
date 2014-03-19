package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.ini4j.Wini;
import org.jclouds.ec2.compute.options.EC2TemplateOptions;
import org.jenkinsci.test.acceptance.guice.SubWorld;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author: Vivek Pandey
 */
@Singleton
public class Ec2Config {

    @Inject(optional=true)
    private SubWorld subWorld;

    /**
     * This is the file we read credentials from. We reuse AWS CLI format.
     */
    @Inject(optional = true)
    @Named("ec2ConfigFile")
    private String ec2ConfigFile = System.getProperty("user.home")+"/.aws/config";

    /**
     * Section in {@link #ec2ConfigFile} to pick credential from.
     */
    @Inject(optional = true)
    @Named("profile")
    private String profile = "default";

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

    /**
     * EC2 key name.
     *
     * By default we generate the key name from the fingerprint of the public key, so that the same public key maps
     * to the same key pair name, and different ones to different names. Otherwise it's too easy to end up
     * in the situation where you have one key pair locally but the key name exists on the server for a different key
     * (and all you get is the authentication error when you try to login to the box.)
     *
     * Jclouds seems to offer the ability to launch an instance without a key pair {@link EC2TemplateOptions#noKeyPair()}, but it didn't work.
     * so we opt for generating our own unique name.
     *
     * @deprecated doesn't work
     */
    @Inject(optional = true)
    @Named("keyPairName")
    private String keyPairName=null ; // "jenkins-test-"+System.getProperty("user.name")+getLocalHostName();

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
        File f  = new File(ec2ConfigFile);
        try {
            Wini wini = new Wini(f);

            this.key = wini.get(profile,"aws_access_key_id",String.class);
            this.secret = wini.get(profile,"aws_secret_access_key",String.class);

            if(key == null){
                throw new RuntimeException(String.format("EC2 config file %s does not have 'key'",ec2ConfigFile));
            }
            if(secret == null){
                throw new RuntimeException(String.format("EC2 config file %s does not have 'secret'",ec2ConfigFile));
            }
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

    /**
     * @deprecated
     *      doesn't work with EC2. See Ec2Provider.
     */
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
