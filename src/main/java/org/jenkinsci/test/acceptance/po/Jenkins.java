package org.jenkinsci.test.acceptance.po;

import hudson.util.VersionNumber;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import javax.inject.Inject;

import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.guice.TestScope;

import com.google.inject.Injector;

/**
 * Top-level object that acts as an entry point to various systems.
 *
 * This is also the only page object that can be injected since there's always one that points to THE Jenkins instance
 * under test.
 *
 * @author Kohsuke Kawaguchi
 */
@TestScope
public class Jenkins extends Node {
    private VersionNumber version;

    public final JobsMixIn jobs;
    public final ViewsMixIn views;
    public final SlavesMixIn slaves;

    public Jenkins(Injector injector, URL url) {
        super(injector,url);
        getVersion();
        jobs = new JobsMixIn(this);
        views = new ViewsMixIn(this);
        slaves = new SlavesMixIn(this);
    }

    @Inject
    public Jenkins(Injector injector, JenkinsController controller) {
        this(injector, startAndGetUrl(controller));
    }

    private static URL startAndGetUrl(JenkinsController controller) {
        try {
            controller.start();
            return controller.getUrl();
        } catch (IOException e) {
            throw new AssertionError("Failed to start JenkinsController",e);
        }
    }

    /**
     * Get the version of Jenkins under test.
     */
    public VersionNumber getVersion() {
        if (version!=null)      return  version;

        String text;
        try {
            text = url.openConnection().getHeaderField("X-Jenkins");
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
        int space = text.indexOf(' ');
        if (space != -1) {
            text = text.substring(0, space);
        }

        return version = new VersionNumber(text);
    }

    /**
     * Access global configuration page.
     */
    public JenkinsConfig getConfigPage() {
        return new JenkinsConfig(this);
    }

    /**
     * Visit login page.
     */
    public Login login(){
        Login login = new Login(this);
        visit(login.url);
        return login;
    }

    /**
     * Visit logout URL.
     */
    public void logout(){
        visit(new Logout(this).url);
    }

    /**
     * Access the plugin manager page object
     */
    public PluginManager getPluginManager() {
        return new PluginManager(this);
    }

    public void restart() {
        visit("restart");
        clickButton("Yes");

        do {
            elasticSleep(1000);
        } while (driver.getPageSource().contains("Please wait"));
    }

    public JenkinsLogger getLogger(String name) {
        return new JenkinsLogger(this,name);
    }

    public JenkinsLogger createLogger(String name, Map<String,Level> levels) {
        return JenkinsLogger.create(this,name,levels);
    }

    public Plugin getPlugin(String name) {
        return new Plugin(getPluginManager(), name);
    }

    public <T extends PageObject> T getPluginPage(Class<T> type) {
        String urlChunk = type.getAnnotation(PluginPageObject.class).value();

        return newInstance(type, injector, url("plugin/%s/", urlChunk));
    }

    @Override
    public String getName() {
        return "(master)";
    }
}
