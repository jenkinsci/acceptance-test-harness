package org.jenkinsci.test.acceptance.controller;

import com.cloudbees.sdk.extensibility.Extension;
import org.codehaus.plexus.util.FileUtils;
import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author: Vivek Pandey
 */
public class JbossController extends LocalController {
    private final String jbossHome;

    public JbossController(File war, File jbossHome) {
        super(war);
        if(jbossHome == null){
            if(System.getenv("CATALINA_HOME") != null){
                jbossHome = new File(System.getenv("CATALINA_HOME"));
            }else{
                jbossHome = FileUtils.resolveFile(new File(WORKSPACE), "./tomcat");
            }
        }
        if(!jbossHome.isDirectory() || !jbossHome.exists()){
            throw new RuntimeException("Invalid CATALINA_HOME: "+jbossHome.getAbsolutePath());
        }

        this.jbossHome = jbossHome.getAbsolutePath();
    }

    @Override
    public ProcessInputStream startProcess() throws IOException {
        File jenkinsDeploymentDir = new File(jbossHome+"/standalone/deployments/jenkins.war.deployed");
        if(jenkinsDeploymentDir.exists()){
            FileUtils.forceDelete(jenkinsDeploymentDir);
        }

        File jenkinsDeploymentFailDir = new File(jbossHome+"/standalone/deployments/jenkins.war.failed");
        if(jenkinsDeploymentFailDir.exists()){
            FileUtils.forceDelete(jenkinsDeploymentFailDir);
        }

        String jenkinsWarDeploymentPath = jbossHome+"/standalone/deployments/jenkins.war";
        if(FileUtils.fileExists(jenkinsWarDeploymentPath)){
            FileUtils.forceDelete(jenkinsWarDeploymentPath);
        }

        FileUtils.copyFile(war, new File(jenkinsWarDeploymentPath));

        String jbossLog = jbossHome + "/standalone/log/server.log";
        if(FileUtils.fileExists(jbossLog)){
            FileUtils.forceDelete(jbossLog);
        }

        CommandBuilder cb = new CommandBuilder(jbossHome+"/bin/standalone.sh");
        cb.env.put("JAVA_OPTS", "-DJENKINS_HOME="+getJenkinsHome());
        return cb.popen();
    }

    @Override
    public void stopNow() throws IOException {
        System.out.println("    Stopping a temporary Jenkins/JBoss instance\n");
        CommandBuilder cb = new CommandBuilder(jbossHome+"/bin/jboss-cli.sh"," --connect", "--command=:shutdown");
        try {
            int status = cb.system();
            if(status != 0){
                System.out.println("Cannot stop JBoss: "+cb.toString());
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public URL getUrl() {
        try {
            return new URL("http://127.0.0.1:8080/jenkins/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Extension
    public static class FactoryImpl extends LocalFactoryImpl {
        @Override
        public String getId() {
            return "jboss";
        }

        @Override
        public JbossController create() {
            return new JbossController(getWarFile(), getJBossHome());
        }

        protected File getJBossHome() {
            File home = new File(defaultsTo(System.getenv("JBOSS_HOME"), "./jboss"));
            if (!home.isDirectory())
                throw new AssertionError(home+" doesn't exist, maybe you forgot to set JBOSS_HOME env var? ");
            return home;
        }
    }
}
