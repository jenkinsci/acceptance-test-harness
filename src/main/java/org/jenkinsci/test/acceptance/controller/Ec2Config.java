package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author: Vivek Pandey
 */
@Singleton
public class Ec2Config {

    @Inject
    @Named("key")
    private  String key;

    @Inject
    @Named("secret")
    private  String secret;


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

    @Inject(optional = true)
    @Named("inboundPorts")
    private int[] inboundPorts={8080,8081};

    @Inject(optional = true)
    @Named("user")
    private String user="ubuntu";


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
