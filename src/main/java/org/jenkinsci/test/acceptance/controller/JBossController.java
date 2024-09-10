package org.jenkinsci.test.acceptance.controller;

import com.cloudbees.sdk.extensibility.Extension;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.test.acceptance.utils.IOUtil;
import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;

/**
 * Launches Jenkins in JBoss.
 *
 * @author Vivek Pandey
 */
public class JBossController extends LocalController {
    private final File jbossHome;

    @Inject
    public JBossController(Injector i, @Named("jbossHome") File jbossHome) {
        super(i);
        if (!jbossHome.isDirectory()) {
            throw new RuntimeException("Invalid JBoss Home: " + jbossHome.getAbsolutePath());
        }

        this.jbossHome = jbossHome.getAbsoluteFile();
    }

    @Override
    public ProcessInputStream startProcess() throws IOException {
        File jenkinsDeploymentDir = new File(jbossHome, "standalone/deployments/jenkins.war.deployed");
        if (jenkinsDeploymentDir.exists()) {
            FileUtils.forceDelete(jenkinsDeploymentDir);
        }

        File jenkinsDeploymentFailDir = new File(jbossHome, "standalone/deployments/jenkins.war.failed");
        if (jenkinsDeploymentFailDir.exists()) {
            FileUtils.forceDelete(jenkinsDeploymentFailDir);
        }

        File jenkinsWarDeploymentPath = new File(jbossHome, "standalone/deployments/jenkins.war");
        if (jenkinsWarDeploymentPath.exists()) {
            FileUtils.forceDelete(jenkinsWarDeploymentPath);
        }

        FileUtils.copyFile(war, jenkinsWarDeploymentPath);

        File jbossLog = new File(jbossHome, "/standalone/log/server.log");
        if (jbossLog.exists()) {
            FileUtils.forceDelete(jbossLog);
        }

        File context = new File(jbossHome, "conf/context.xml");
        if (context.exists()) {
            org.apache.commons.io.FileUtils.forceDelete(context);
        }
        Files.writeString(
                context.toPath(),
                "<Context>\n"
                        + "    <Parameter name=\"jenkins.formelementpath.FormElementPathPageDecorator.enabled\" value=\"true\"/>\n"
                        + "</Context>",
                StandardCharsets.UTF_8);

        CommandBuilder cb = new CommandBuilder(jbossHome + "/bin/standalone.sh");
        cb.env.putAll(commonLaunchEnv());
        return cb.popen();
    }

    @Override
    public void stopNow() throws IOException {
        System.out.println("    Stopping a temporary Jenkins/JBoss instance\n");
        CommandBuilder cb = new CommandBuilder(jbossHome + "/bin/jboss-cli.sh", " --connect", "--command=:shutdown");
        try {
            int status = cb.system();
            if (status != 0) {
                System.out.println("Cannot stop JBoss: " + cb);
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
        @Inject
        Injector i;

        @Override
        public String getId() {
            return "jboss";
        }

        @Override
        public JBossController create() {
            return i.createChildInjector(i.getInstance(ModuleImpl.class)).getInstance(JBossController.class);
        }

        public static final class ModuleImpl extends AbstractModule {

            @Provides
            @Named("jbossHome")
            public File jbossHome(@Named("jenkins.war") File warFile) {
                try {
                    return IOUtil.firstExisting(
                            true,
                            System.getenv("JBOSS_HOME"),
                            new File(warFile.getParentFile(), "jboss").getAbsolutePath(),
                            "./jboss");
                } catch (IOException ex) {
                    throw new AssertionError("Cannot find JBoss home, maybe you forgot to set JBOSS_HOME env var?", ex);
                }
            }

            @Override
            protected void configure() {}
        }
    }
}
