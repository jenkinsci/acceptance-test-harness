package org.jenkinsci.test.acceptance.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.test.acceptance.utils.IOUtil;
import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;

import com.cloudbees.sdk.extensibility.Extension;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;

/**
 * @author: Vivek Pandey
 */
public class TomcatController extends LocalController {

    protected final File catalinaHome;

    @Inject
    public TomcatController(Injector i, @Named("tomcatHome") File catalinaHome) {
        super(i);
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

            File context =  new File(catalinaHome,"conf/context.xml");
            if(context.exists()){
                FileUtils.forceDelete(context);
            }
            FileUtils.write(context, "<Context>\n" +
                    "    <Parameter name=\"jenkins.formelementpath.FormElementPathPageDecorator.enabled\" value=\"true\"/>\n" +
                    "</Context>", StandardCharsets.UTF_8);



            CommandBuilder cb = new CommandBuilder(catalinaHome+"/bin/startup.sh");
            cb.env.putAll(commonLaunchEnv());
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
            return new URL("http://" + getSutHostName() + ":8080/jenkins/");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Extension
    public static class FactoryImpl extends LocalFactoryImpl {
        @Inject Injector i;

        @Override
        public String getId() {
            return "tomcat";
        }

        @Override
        public TomcatController create() {
            return i.createChildInjector(i.getInstance(ModuleImpl.class)).getInstance(TomcatController.class);
        }

        public static final class ModuleImpl extends AbstractModule {

            @Provides @Named("tomcatHome")
            public File tomcatHome(@Named("jenkins.war") File warFile) {
                try {
                    return IOUtil.firstExisting(true,
                            System.getenv("CATALINA_HOME"),
                            new File(warFile.getParentFile(), "tomcat").getAbsolutePath(),
                            "./tomcat"
                    );
                } catch (IOException ex) {
                    throw new AssertionError("Cannot find Tomcat home, maybe you forgot to set CATALINA_HOME env var?", ex);
                }
            }

            @Override
            protected void configure() {
            }
        }
    }
}
