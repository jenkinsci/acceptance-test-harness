package org.jenkinsci.test.acceptance.po;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.jenkinsci.utils.process.CommandBuilder;

import com.google.common.base.Joiner;

/**
 * A slave page object.
 *
 * To create a new slave into a test, use {@link SlaveController}.
 *
 * @author Kohsuke Kawaguchi
 * @see Jenkins#slaves
 */
public abstract class Slave extends Node {
    private final String name;

    protected Slave(Jenkins j, String name) {
        super(j, j.url("computer/%s/",name));
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
        waitForCond(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                return isOnline();
            }

            @Override public String toString() {
                return "Slave is online";
            }
        });
        return this;
    }

    public boolean isOffline() {
        return getJson().get("offline").asBoolean();
    }

    public int getExecutorCount() {
        return getJson().get("executors").size();
    }


    public void setExecutors(int n) {
        find(by.input("_.numExecutors")).clear(); //clear the previous value or it gets appended
        find(by.input("_.numExecutors")).sendKeys(String.valueOf(n));
        // in my chrome, I need to move the focus out from the control to have it recognize the value entered
        // perhaps it's related to the way input type=number is emulated?
        find(by.input("_.remoteFS")).click();
    }

    public void setRemoteFs(String s) {
        find(by.input("_.remoteFS")).sendKeys(s);
    }

    public void setLabels(String l) {
        find(by.path("/labelString")).sendKeys(l);
    }

    /**
     * Set up this slave as a local slave that launches slave on the same host as Jenkins
     * call this in the context of the config UI
     */
    public void asLocal() {
        assertCurl();
        File jar = new File("/tmp/slave"+createRandomName()+".jar");
        find(by.option("hudson.slaves.CommandLauncher")).click();
        find(by.input("_.command")).sendKeys(String.format(
                "sh -c 'curl -s -o %1$s %2$sjnlpJars/slave.jar && java -jar %1$s'",
                jar, url("../../")
        ));

    }

    // TODO: check if the installation could be achieved without curl
    private void assertCurl() {
        try {
            if (new CommandBuilder("which", "curl").system() != 0) {
                throw new IllegalStateException("curl is required to run tests that run on local slaves.");
            }
        }
        catch (IOException | InterruptedException e) {
            // ignore and assume that curl is installed
        }
    }

    public static Matcher<Slave> runBuildsInOrder(final Job... jobs) {
        return new Matcher<Slave>("slave run build in order: %s", Joiner.on(' ').join(jobs)) {
            @Override public boolean matchesSafely(Slave slave) {
                slave.visit("builds");
                String list = slave.find(by.id("projectStatus")).getText();

                StringBuilder sb = new StringBuilder(".*");
                for (Job j: jobs) {
                    sb.insert(0, j.name);
                    sb.insert(0, ".*");
                }

                return Pattern.compile(sb.toString(), Pattern.DOTALL)
                        .matcher(list)
                        .matches()
                ;
            }
        };
    }

    /**
     * If the slave is online, this method will mark it offline for testing purpose.
     */
    public void markOffline() {
        markOffline("Just for testing... be right back...");
    }

    public void markOffline(String message) {

        if(isOnline()) {
            open();
            clickButton("Mark this node temporarily offline");

            find(by.input("offlineMessage")).clear();
            find(by.input("offlineMessage")).sendKeys(message);

            clickButton("Mark this node temporarily offline");
        }
    }

    /**
     * If the slave has been marked offline, this method will bring it up again
     */

    public void markOnline(){

        if(isOffline()) {
            open();
            clickButton("Bring this node back online");
        }
    }

    /**
     * If the slave is online, this method will disconnect for testing purpose.
     */
    public void disconnect(String message) {
        if (isOnline()) {
            open();
            find(by.link("Disconnect")).click();
            find(by.input("offlineMessage")).clear();
            find(by.input("offlineMessage")).sendKeys(message);
            clickButton("Yes");
        }
    }

    /**
     * If the slave is offline, this method will launch the slave agent.
     */
    public void launchSlaveAgent() {
        if (isOffline()) {
            open();
            clickButton("Launch slave agent");
        }
    }
}
