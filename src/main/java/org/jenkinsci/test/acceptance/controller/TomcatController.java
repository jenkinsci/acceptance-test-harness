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
public class TomcatController extends LocalController {

    private String catalinaHome;

    public TomcatController(File war, File catalinaHome) {
        super(war);

        if(catalinaHome == null){
            if(System.getenv("CATALINA_HOME") != null){
                catalinaHome = new File(System.getenv("CATALINA_HOME"));
            }else{
                catalinaHome = FileUtils.resolveFile(new File(WORKSPACE), "./tomcat");
            }
        }
        if(!catalinaHome.isDirectory() || !catalinaHome.exists()){
            throw new RuntimeException("Invalid CATALINA_HOME: "+catalinaHome.getAbsolutePath());
        }

        this.catalinaHome = catalinaHome.getAbsolutePath();
    }

    @Override
    public ProcessInputStream startProcess() throws IOException{
        try {
            File jenkinsDeploymentDir = new File(catalinaHome+"/webapps/jenkins");
            if(jenkinsDeploymentDir.exists()){
                FileUtils.forceDelete(jenkinsDeploymentDir);
            }

            String jenkinsWarDeploymentPath = catalinaHome+"/webapps/jenkins.war";
            if(FileUtils.fileExists(jenkinsWarDeploymentPath)){
                FileUtils.forceDelete(jenkinsWarDeploymentPath);
            }

            FileUtils.copyFile(war, new File(jenkinsWarDeploymentPath));

            String tomcatLog = catalinaHome + "/logs/catalina.out";
            if(FileUtils.fileExists(tomcatLog)){
                FileUtils.forceDelete(tomcatLog);
            }

            CommandBuilder cb = new CommandBuilder(catalinaHome+"/bin/startup.sh");
            cb.env.put("JAVA_OPTS", "-DJENKINS_HOME="+getJenkinsHome());
            int status = cb.system();
            if(status != 0){
                throw new RuntimeException("Failed during Tomcat startup: "+cb.toString());
            }
            CommandBuilder tail = new CommandBuilder("tail","-f", tomcatLog);
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
            File home = new File(defaultsTo(System.getenv("CATALINA_HOME"), "./tomcat"));
            if (!home.isDirectory())
                throw new AssertionError(home+" doesn't exist, maybe you forgot to set CATALINA_HOME env var? ");
            return home;
        }
    }
}
