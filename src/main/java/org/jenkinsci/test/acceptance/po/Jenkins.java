package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import hudson.util.VersionNumber;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.openqa.selenium.By;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URL;
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
public class Jenkins extends PageObject {
    public Jenkins(Injector injector, URL url) {
        super(injector,url);
    }

    @Inject
    public Jenkins(Injector injector, JenkinsController controller) {
        this(injector, controller.getUrl());
    } 
    /**
     * Get the version of Jenkins under test.
     */
    public VersionNumber getVersion() throws Exception {
        String prefix = "About Jenkins ";
        visit("about");
        String text = waitFor(By.xpath("//h1[starts-with(., '"+prefix+"')]")).getText();

        Matcher m = VERSION.matcher(text);
        if (m.matches())
            return new VersionNumber(m.group(1));
        else
            throw new AssertionError("Unexpected version string: "+text);
    }

    public <T extends Job> T createJob(Class<T> type, String name) throws Exception {
        String sut_type = type.getAnnotation(JobPageObject.class).value();

        visit("newJob");
        fillIn("name", name);
        find(By.xpath("//input[starts-with(@value, '"+sut_type+"')]")).click();
        clickButton("OK");

        return type.getConstructor(Injector.class,URL.class,String.class)
                .newInstance(injector, new URL(url, "job/" + name + "/"), name);
    }

    public <T extends Job> T createJob(Class<T> type) throws Exception {
        return createJob(type, createRandomName());
    }


    private static final Pattern VERSION = Pattern.compile("^About Jenkins ([^-]*)");
}
