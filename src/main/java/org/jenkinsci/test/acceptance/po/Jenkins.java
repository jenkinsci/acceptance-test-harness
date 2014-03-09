package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import hudson.util.VersionNumber;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.openqa.selenium.By;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Top-level object that acts as an entry point to various systems.
 *
 * This is also the only page object that can be injected since there's always one that points to THE Jenkins instance
 * under test.
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class Jenkins extends ContainerPageObject {
    private VersionNumber version;

    public Jenkins(Injector injector, URL url) {
        super(injector,url);
        getVersion();
    }

    @Inject
    public Jenkins(Injector injector, JenkinsController controller) {
        this(injector, controller.getUrl());
    } 
    /**
     * Get the version of Jenkins under test.
     */
    public VersionNumber getVersion() {
        if (version!=null)      return  version;

        String prefix = "About Jenkins ";
        visit("about");
        String text = waitFor(By.xpath("//h1[starts-with(., '"+prefix+"')]")).getText();

        Matcher m = VERSION.matcher(text);
        if (m.find())
            return version = new VersionNumber(m.group(1));
        else
            throw new AssertionError("Unexpected version string: "+text);
    }

    public <T extends Job> T createJob(Class<T> type, String name) {
        String sut_type = type.getAnnotation(JobPageObject.class).value();

        visit("newJob");
        fillIn("name", name);
        find(By.xpath("//input[starts-with(@value, '"+sut_type+"')]")).click();
        clickButton("OK");

        try {
            return type.getConstructor(Injector.class,URL.class,String.class)
                    .newInstance(injector, url("job/%s/", name), name);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    public <T extends Job> T createJob(Class<T> type) {
        return createJob(type, createRandomName());
    }

    /**
     * Access global configuration page.
     */
    public JenkinsConfig getConfigPage() {
        return new JenkinsConfig(this);
    }

    /**
     * Access the plugin manager page object
     */
    public PluginManager getPluginManager() {
        return new PluginManager(this);
    }

    public JenkinsLogger getLogger(String name) {
        return new JenkinsLogger(this,name);
    }

    public JenkinsLogger createLogger(String name, Map<String,Level> levels) {
        return JenkinsLogger.create(this,name,levels);
    }

    public DumbSlave createDumbSlave(String name) {
        return DumbSlave.create(this,name);
    }


    private static final Pattern VERSION = Pattern.compile("^About Jenkins ([^-]*)");
}
