package org.jenkinsci.test.acceptance.po;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.google.common.base.Predicate;
import com.google.inject.Injector;

import hudson.util.VersionNumber;

/**
 * Top-level object that acts as an entry point to various systems.
 *
 * This is also the only page object that can be injected since there's always one that points to THE Jenkins instance
 * under test.
 *
 * @author Kohsuke Kawaguchi
 */
public class Jenkins extends Node implements Container {
    private VersionNumber version;

    public final JobsMixIn jobs;
    public final ViewsMixIn views;
    public final SlavesMixIn slaves;

    private Jenkins(Injector injector, URL url) {
        super(injector,url);
        getVersion();
        jobs = new JobsMixIn(this);
        views = new ViewsMixIn(this);
        slaves = new SlavesMixIn(this);
    }

    @Override
    public Jenkins getJenkins() {
        return this;
    }

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
            URLConnection urlConnection = url.openConnection();
            text = urlConnection.getHeaderField("X-Jenkins");
            if (text == null) {

                String pageText = IOUtils.toString(urlConnection.getInputStream());
                throw new AssertionError(
                        "Application running on " + url + " does not seem to be Jenkins:\n" + pageText
                );
            }
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
     * Tells if Jenkins version under test is 1.X
     */
    public boolean isJenkins1X() {
        return getVersion().isOlderThan(new VersionNumber("2.0"));
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
     * Get user currently logged in.
     */
    public User getCurrentUser() {
        return User.getCurrent(this);
    }

    public User getUser(String name) {
        return new User(this, name);
    }

    /**
     * Access the plugin manager page object
     */
    public PluginManager getPluginManager() {
        return new PluginManager(this);
    }

    /** 
     * Some tests require they restart Jenkins - but depending on how the SUT is launched this is not always possible
     * so tests that require this should wrap this call in an {@link org.junit.Assume#assumeTrue(String, boolean)}
     * @return true if the Jenkins under test can restart itself.
     */
    public boolean canRestart() {
        visit("restart");
        return getElement(by.button("Yes")) != null;
    }

    public void restart() {
        visit("restart");
        clickButton("Yes");

        // Poll until we have the real page
        waitFor(driver).withTimeout(JenkinsController.STARTUP_TIMEOUT, TimeUnit.SECONDS)
                .ignoring(
                        AssertionError.class, // Still waiting
                        NoSuchElementException.class // No page served at all
                )
                .until(new Predicate<WebDriver>() {
                    @Override
                    public boolean apply(WebDriver driver) {
                        visit(driver.getCurrentUrl()); // the page sometimes does not reload (fast enough)
                        getJson("tree=nodeName"); // HudsonIsRestarting will serve a 503 to the index page, and will refuse api/json
                        return true;
                    }
                })
        ;
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

    @Override
    public JobsMixIn getJobs() {
        return jobs;
    }

    @Override
    public ViewsMixIn getViews() {
        return views;
    }
}
