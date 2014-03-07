package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import static java.lang.System.getenv;

/**
 * @author Vivek Pandey
 */
@Singleton
public class JenkinsProvider implements Provider<JenkinsController> {

    private static final Logger logger = LoggerFactory.getLogger(JenkinsProvider.class);
    private final Machine machine;


    private final String jenkinsHome;

    private final JenkinsController jenkinsController;

    @Inject
    public JenkinsProvider(Machine machine) {
        this.machine = machine;
        logger.info("New Jenkins Provider created");
        Ssh ssh=null;
        try{
            String warLocation = getenv("JENKINS_WAR");

            String user = machine.getUser();
            ssh = new Ssh(user, machine.getPublicIpAddress());

            ssh.copyTo(warLocation, "jenkins.war", ".");

            this.jenkinsHome = newJenkinsHome();

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                ssh.executeRemoteCommand("mkdir -p " + jenkinsHome + "/plugins", os);

                File formPathElement = JenkinsController.downloadPathElement();

                //copy form-path-element
                ssh.copyTo(formPathElement.getAbsolutePath(), "path-element.hpi", "./"+jenkinsHome+"/plugins/");

                ssh.destroy();
                this.jenkinsController = new RemoteJenkinsController(machine, jenkinsHome);
            } catch (IOException e) {
                throw new RuntimeException(new String(os.toByteArray()),e);
            }

        }catch(Exception e){
            machine.terminate(); //any exception and we clean the ec2 resource
            throw new RuntimeException(e);
        }finally {
            if(ssh != null){
                ssh.destroy();
            }
        }
    }

    @Override
    public JenkinsController get() {
        logger.info("New RemoteJenkinsController created");
        return jenkinsController;
    }

    private String newJenkinsHome(){
        SecureRandom secureRandom = new SecureRandom();
        long secureInitializer = secureRandom.nextLong();
        Random rand = new Random( secureInitializer + Runtime.getRuntime().freeMemory() );
        return String.format("temp%sdir", rand.nextInt());
    }

}
