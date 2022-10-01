package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * @author Kohsuke Kawaguchi
 */
public class JenkinsLogger extends PageObject {
    public final String name;
    public JenkinsLogger(Jenkins jenkins, String name) {
        super(jenkins, jenkins.url("log/" + name));
        this.name = name;
    }

    /**
     * @see Jenkins#createLogger(String, Map)
     */
    public static JenkinsLogger create(Jenkins j, String name, Map<String, Level> levels) {
        j.visit("log/new");
        j.find(by.path("/name")).sendKeys(name);
        try {
            j.clickButton("Create");
        } catch (NoSuchElementException e) {
            j.clickButton("OK");
        }

        for (Entry<String, Level> e : levels.entrySet()) {
            j.clickButton("Add");
            j.elasticSleep(1000);
            j.last(by.input("_.name")).sendKeys(e.getKey());
            WebElement o = j.last(by.input("level"))
                    .findElement(by.option(e.getValue().getName()));
            j.check(o);
        }
        j.clickButton("Save");
        return new JenkinsLogger(j,name);
    }

    public boolean isEmpty() {
        open();
        return getElement(by.css("#main-panel pre"))==null;
    }

    public boolean hasLogged(Pattern pattern) {
        open();
        for (WebElement e : all(by.css("#main-panel pre"))) {
            if (pattern.matcher(e.getText()).matches()) return true;
        }

        return false;
    }

    public void waitForLogged(final Pattern pattern) {
        waitForLogged(pattern, 0);
    }

    public void waitForLogged(final Pattern pattern, final int timeout) {
        waitFor().withMessage("%s to be logged", pattern)
                .withTimeout(timeout, TimeUnit.SECONDS)
                .until(() -> hasLogged(pattern));
    }
}
