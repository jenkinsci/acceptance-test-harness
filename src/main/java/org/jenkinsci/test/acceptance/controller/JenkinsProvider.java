package org.jenkinsci.test.acceptance.controller;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.jenkinsci.test.acceptance.resolver.JenkinsResolver;
import org.jenkinsci.test.acceptance.resolver.PluginDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author Vivek Pandey
 */
public class JenkinsProvider implements Provider<JenkinsController> {

    private static final Logger logger = LoggerFactory.getLogger(JenkinsProvider.class);

    private final Machine machine;



    private final JenkinsResolver jenkinsResolver;

    private final String jenkinsWar;

    private final File privateKeyFile;

    @Inject
    public JenkinsProvider(Machine machine, JenkinsResolver jenkinsResolver, SshKeyPair keyPair) {
        this.machine = machine;
        this.jenkinsResolver = jenkinsResolver;
        logger.info("New Jenkins Provider created");
        try{
            //install jenkins WAR
            this.jenkinsWar = JenkinsResolver.JENKINS_TEMP_DIR+"jenkins.war";
            jenkinsResolver.materialize(machine, jenkinsWar);
            this.privateKeyFile = keyPair.privateKey;
        }catch(Exception e){
            logger.error("Error during setting up Jenkins: "+e.getMessage(),e);
            throw new AssertionError(e);
        }
    }

    @Override
    public JenkinsController get() {
        logger.info("Creating new RemoteJenkinsController...");
        JenkinsController jenkinsController = createNewJenkinsController();
        try {
            jenkinsController.start();
        } catch (IOException e) {
            throw new AssertionError("Failed to start Jenkins: "+e.getMessage(),e);
        }
        return jenkinsController;
    }

    private JenkinsController createNewJenkinsController(){
        String jenkinsHome = machine.dir()+newJenkinsHome()+"/";
        String pluginDir = jenkinsHome +"plugins/";
        String path = JenkinsResolver.JENKINS_TEMP_DIR+"form-element-path.hpi";

        //install form-path-element plugin
        new PluginDownloader("form-element-path").materialize(machine, path);
        Ssh ssh = machine.connect();
        ssh.executeRemoteCommand("mkdir -p " + pluginDir);

        ssh.executeRemoteCommand(String.format("cp %s %s", path, pluginDir));

        return new RemoteJenkinsController(machine, jenkinsHome,jenkinsWar,privateKeyFile);
    }

    private String newJenkinsHome(){
        return String.format("jenkins_home_%s", JcloudsMachine.newDirSuffix());
    }

}
