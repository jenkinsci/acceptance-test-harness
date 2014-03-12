package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.jenkinsci.test.acceptance.guice.TestScope;
import org.jenkinsci.test.acceptance.resolver.JenkinsResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Vivek Pandey
 */
@TestScope
public class JenkinsProvider implements Provider<JenkinsController> {

    private static final Logger logger = LoggerFactory.getLogger(JenkinsProvider.class);
    private final Machine machine;


    private final String jenkinsHome;

    private final JenkinsController jenkinsController;

    private final JenkinsResolver jenkinsResolver;

    @Inject
    public JenkinsProvider(Machine machine, JenkinsResolver jenkinsResolver) {
        this.machine = machine;
        this.jenkinsResolver = jenkinsResolver;
        logger.info("New Jenkins Provider created");
        try{
            //install jenkins
            String jenkinsWar = jenkinsResolver.materialize(machine);
            this.jenkinsHome = machine.dir()+"/"+newJenkinsHome();
            try {
                Ssh ssh = machine.connect();
                ssh.executeRemoteCommand("mkdir -p " + jenkinsHome + "/plugins");

                File formPathElement = JenkinsController.downloadPathElement();

                //copy form-path-element
                ssh.copyTo(formPathElement.getAbsolutePath(), "path-element.hpi", "./"+jenkinsHome+"/plugins/");

                this.jenkinsController = new RemoteJenkinsController(machine, jenkinsHome,jenkinsWar);
            } catch (IOException e) {
                throw new AssertionError("Failed to copy form-path-element.hpi",e);
            }

        }catch(Exception e){
            try {
                machine.close();
            } catch (IOException e1) {
                throw new AssertionError(e);
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public JenkinsController get() {
        logger.info("New RemoteJenkinsController created");
        return jenkinsController;
    }

    private String newJenkinsHome(){
        return String.format("jenkins_home_%s", JcloudsMachine.newDirSuffix());
    }

}
