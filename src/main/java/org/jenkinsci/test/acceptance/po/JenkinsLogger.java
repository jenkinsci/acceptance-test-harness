package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kohsuke Kawaguchi
 */
public class JenkinsLogger extends ContainerPageObject {
    public final String name;
    public JenkinsLogger(Jenkins jenkins, String name) {
        super(jenkins, jenkins.url("log/" + name + "/"));
        this.name = name;
    }

    public void waitForExistence() {
        waitForCond(new Callable<WebElement>() {
            public WebElement call() throws Exception {
                Thread.sleep(100);
                open();
                return getElement(by.xpath("//h1[text()='%s']", name));
            }
        });
    }

    /**
     * @see Jenkins#createLogger(String, Map)
     */
    public static JenkinsLogger create(Jenkins j, String name, Map<String, Level> levels) {
        try {
            j.visit("log/new");
            j.find(by.path("/name")).sendKeys(name);
            j.clickButton("OK");

            for (Entry<String, Level> e : levels.entrySet()) {
                j.clickButton("Add");
                Thread.sleep(1000);
                j.last(by.input("_.name")).sendKeys(e.getKey());
                WebElement o = j.last(by.input("level"))
                        .findElement(by.option(e.getValue().getName()));
                j.check(o);
            }
            j.clickButton("Save");
            return new JenkinsLogger(j,name);
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    /**
     * TODO: this is audit-trail specific
     */
    public List<String> getEvents() {
        open();
        List<String> events = new ArrayList<>();
        for (WebElement e : all(by.css("#main-panel pre"))) {
            Matcher m = LOG_PATTERN.matcher(e.getText());
            if (!m.matches())    continue; // Earlier versions used one element per log entry newer use two
            events.add(m.group(1));
        }
        return events;
    }

    public boolean isEmpty() {
        open();
        return getElement(by.css("#main-panel pre"))==null;
    }

    private static final Pattern LOG_PATTERN = Pattern.compile("((?:\\/\\w+)+.*?) by (.*)");
}
