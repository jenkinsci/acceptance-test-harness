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
 * @author: Vivek Pandey
 */
public class TomcatController extends LocalController {

    protected final File catalinaHome;

    public TomcatController(File war, File catalinaHome) {
        super(war);
        if (!catalinaHome.isDirectory()) {
            throw new RuntimeException("Invalid CATALINA_HOME: " + catalinaHome.getAbsolutePath());
        }
        this.catalinaHome = catalinaHome.getAbsoluteFile();
    }

    @Override
    public ProcessInputStream startProcess() throws IOException{
        try {
            File jenkinsDeploymentDir = new File(catalinaHome,"webapps/jenkins");
            if(jenkinsDeploymentDir.exists()){
                FileUtils.forceDelete(jenkinsDeploymentDir);
            }

            File jenkinsWarDeploymentPath = new File(catalinaHome,"webapps/jenkins.war");
            if(jenkinsWarDeploymentPath.exists()){
                FileUtils.forceDelete(jenkinsWarDeploymentPath);
            }

            FileUtils.copyFile(war, jenkinsWarDeploymentPath);

            File tomcatLog = new File(catalinaHome,"logs/catalina.out");
            if(tomcatLog.exists()){
                FileUtils.forceDelete(tomcatLog);
            }

            CommandBuilder cb = new CommandBuilder(catalinaHome+"/bin/startup.sh");
            cb.env.put("JAVA_OPTS", "-DJENKINS_HOME="+getJenkinsHome());
            int status = cb.system();
            if(status != 0){
                throw new RuntimeException("Failed during Tomcat startup: "+cb.toString());
            }
            CommandBuilder tail = new CommandBuilder("tail").add("-f", tomcatLog);
            return tail.popen();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void stopNow() throws IOException{
        System.out.println("    Stopping a temporary Jenkins/Tomcat instance\n");
        CommandBuilder cb = new CommandBuilder(catalinaHome+"/bin/shutdown.sh");
        try {
            int status = cb.system();
            if(status != 0){
                throw new RuntimeException("Cannot stop Tomcat");
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
            return "tomcat";
        }

        @Override
        public TomcatController create() {
            return new TomcatController(getWarFile(), getTomcatHome());
        }

        protected File getTomcatHome() {
            String catalinaHome = System.getenv("CATALINA_HOME");
            File home = firstExisting(true, catalinaHome,
                    new File(getWarFile().getParentFile(), "tomcat").getAbsolutePath(), "./tomcat");
            if (home == null || !home.isDirectory()) {
                if (StringUtils.isBlank(catalinaHome)) {
                    throw new AssertionError(
                            "Cannot find Tomcat home, maybe you forgot to set CATALINA_HOME env var? ");
                }
                throw new AssertionError(
                        catalinaHome + " doesn't exist, maybe you forgot to set CATALINA_HOME env var? ");
            }
            return home;
        }
    }
}
