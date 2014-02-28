package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import hudson.util.VersionNumber;
import org.openqa.selenium.By;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Top-level object that acts as an entry point to various systems
 *
 * @author Kohsuke Kawaguchi
 */
public class Jenkins extends PageObject {
    public Jenkins(Injector injector) {
        super(injector);
    }

    @Override
    public String getUrl() {
        return "/";
    }

    /**
     * Get the version of Jenkins under test.
     */
    public VersionNumber getVersion() {
        String prefix = "About Jenkins ";
        visit("/about");
        String text = waitFor(By.xpath("//h1[starts-with(., '"+prefix+"')]")).getText();

        Matcher m = VERSION.matcher(text);
        if (m.matches())
            return new VersionNumber(m.group(1));
        else
            throw new AssertionError("Unexpected version string: "+text);
    }


    private static final Pattern VERSION = Pattern.compile("^About Jenkins ([^-]*)");
}
