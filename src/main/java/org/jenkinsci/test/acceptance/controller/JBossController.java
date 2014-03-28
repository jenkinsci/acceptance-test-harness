package org.jenkinsci.test.acceptance.controller;

import com.cloudbees.sdk.extensibility.Extension;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Launches Jenkins in JBoss.
 *
 * @author: Vivek Pandey
 */
public class JBossController extends LocalController {
    private final File jbossHome;

    public JBossController(File war, File jbossHome) {
        super(war);
        if(!jbossHome.isDirectory()){
            throw new RuntimeException("Invalid JBoss Home: "+jbossHome.getAbsolutePath());
        }

        this.jbossHome = jbossHome.getAbsoluteFile();
    }

    @Override
    public ProcessInputStream startProcess() throws IOException {
        File jenkinsDeploymentDir = new File(jbossHome,"standalone/deployments/jenkins.war.deployed");
        if(jenkinsDeploymentDir.exists()){
            FileUtils.forceDelete(jenkinsDeploymentDir);
        }

        File jenkinsDeploymentFailDir = new File(jbossHome,"standalone/deployments/jenkins.war.failed");
        if(jenkinsDeploymentFailDir.exists()){
            FileUtils.forceDelete(jenkinsDeploymentFailDir);
        }

        File jenkinsWarDeploymentPath = new File(jbossHome,"standalone/deployments/jenkins.war");
        if(jenkinsWarDeploymentPath.exists()){
            FileUtils.forceDelete(jenkinsWarDeploymentPath);
        }

        FileUtils.copyFile(war, jenkinsWarDeploymentPath);

        File jbossLog = new File(jbossHome,"/standalone/log/server.log");
        if(jbossLog.exists()){
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
        public JBossController create() {
            return new JBossController(getWarFile(), getJBossHome());
        }

        protected File getJBossHome() {
            String jbossHome = System.getenv("JBOSS_HOME");
            File home = firstExisting(true, jbossHome,
                    new File(getWarFile().getParentFile(), "jboss").getAbsolutePath(), "./jboss");
            if (home == null || !home.isDirectory()) {
                if (StringUtils.isBlank(jbossHome)) {
                    throw new AssertionError("Cannot fine JBoss home, maybe you forgot to set JBOSS_HOME env var? ");
                }
                throw new AssertionError(jbossHome + " doesn't exist, maybe you forgot to set JBOSS_HOME env var? ");
            }
            return home;
        }
    }
}
