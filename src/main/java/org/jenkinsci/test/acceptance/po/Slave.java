package org.jenkinsci.test.acceptance.po;

import com.google.common.base.Joiner;
import java.time.Duration;
import java.util.regex.Pattern;
import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.junit.Wait;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * An agent page object.
 * <p>
 * To create a new agent into a test, use {@link SlaveController}.
 *
 * @author Kohsuke Kawaguchi
 * @see Jenkins#slaves
 */
public class Slave extends Node {
    private final String name;

    public Slave(Jenkins j, String name) {
        super(j, j.url("computer/%s/", name));
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean isOnline() {
        return !isOffline();
    }

    /**
     * Waits for a slave to come online before proceeding.
     * @see #isOnline
     */
    public Slave waitUntilOnline() {
        waitFor().withMessage("Agent is online").until(new Wait.Predicate<Boolean>() {
            @Override
            public Boolean apply() {
                return isOnline();
            }

            @Override
            public String diagnose(Throwable lastException, String message) {
                return "Agent log:\n" + getLog();
            }
        });
        return this;
    }

    public String getLog() {
        visit("log");
        // population of the log is asynchronous
        // before population we just have the placeholder `<pre id="out"></pre>`
        // after population we have an extra div with the content <pre id="out"><div>...</div></pre>
        WebElement webElement = find(by.css("pre#out"));
        waitFor(webElement)
                .withMessage("waiting for initial log")
                .withTimeout(Duration.ofSeconds(10))
                .ignoring(NoSuchElementException.class)
                .until(we -> !we.findElements(by.css("div")).isEmpty());
        return webElement.getText();
    }

    public boolean isOffline() {
        return getJson().get("offline").asBoolean();
    }

    public int getExecutorCount() {
        return getJson().get("executors").size();
    }

    public static Matcher<Slave> runBuildsInOrder(final Job... jobs) {
        return new Matcher<>("agent run build in order: %s", Joiner.on(' ').join(jobs)) {
            @Override
            public boolean matchesSafely(Slave slave) {
                slave.visit("builds");
                // Jobs table may take a little to be populated, give it some time
                slave.elasticSleep(2000);
                String list = slave.find(by.id("projectStatus")).getText().replaceAll("\n", "");

                StringBuilder sb = new StringBuilder(".*");
                for (Job j : jobs) {
                    sb.append(j.name);
                    sb.append(".*");
                }

                return Pattern.compile(sb.toString(), Pattern.DOTALL)
                        .matcher(list)
                        .matches();
            }
        };
    }

    /**
     * If the agent is online, this method will mark it offline for testing purpose.
     */
    public void markOffline() {
        markOffline("Just for testing... be right back...");
    }

    public void markOffline(String message) {

        if (isOnline()) {
            open();
            try {
                clickButton("Mark this node temporarily offline");
            } catch (NoSuchElementException e) {
                clickLink("Mark temporarily offline");
            }

            find(by.input("offlineMessage")).clear();
            find(by.input("offlineMessage")).sendKeys(message);

            try {
                clickButton("Mark this node temporarily offline");
            } catch (NoSuchElementException e) {
                clickButton("Mark temporarily offline");
            }
        }
    }

    /**
     * If the agent has been marked offline, this method will bring it up again
     */
    public void markOnline() {

        if (isOffline()) {
            open();
            clickButton("Bring this node back online");
        }
    }

    /**
     * If the agent is online, this method will disconnect for testing purpose.
     */
    public void disconnect(String message) {
        if (isOnline()) {
            open();
            find(by.link("Disconnect")).click();
            find(by.input("offlineMessage")).clear();
            find(by.input("offlineMessage")).sendKeys(message);
            try {
                clickButton("Yes");
            } catch (NoSuchElementException e) {
                clickButton("Disconnect");
            }
        }
    }

    public void delete() {
        open();
        clickLink("Delete Agent");
        clickButton("Yes");
    }

    /**
     * If the agent is offline, this method will launch it.
     * @deprecated Prefer {@link #launch()}.
     */
    @Deprecated
    public void launchSlaveAgent() {
        launch();
    }

    /**
     * If the agent is offline, this method will launch it.
     */
    public void launch() {
        if (isOffline()) {
            open();
            try {
                clickButton("Launch agent");
            } catch (NoSuchElementException e) {
                clickButton("Launch");
            }
        }
    }
}
