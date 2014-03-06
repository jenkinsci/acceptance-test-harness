package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.jclouds.compute.domain.NodeMetadata;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.getenv;

/**
 * @author Vivek Pandey
 */
public class JenkinsProvider implements Provider<JenkinsController> {

    private final Machine machine;

    private final AtomicBoolean warCopied = new AtomicBoolean(false);

    @Inject
    public JenkinsProvider(Machine machine) {
        this.machine = machine;
        if(!warCopied.get()){
            synchronized (this){ //TODO: fi properly for double checked locking
                if(!warCopied.get()){
                    NodeMetadata nodeMetadata = machine.getNodeMetadata();
                    Ssh ssh=null;
                    try{
                        String warLocation = getenv("JENKINS_WAR");

                        ssh = new Ssh(nodeMetadata.getCredentials().getUser(), nodeMetadata.getPublicAddresses().iterator().next());

                        ssh.copyTo(warLocation, "jenkins.war", ".");
                    }catch(Exception e){
                        machine.terminate(); //any exception and we clean the ec2 resource
                        throw new RuntimeException(e);
                    }finally {
                        if(ssh != null){
                            ssh.destroy();
                        }
                    }
                    warCopied.set(true);
                }
            }
        }

    }

    @Override
    public JenkinsController get() {
        return new RemoteJenkinsController(machine);
    }

}
