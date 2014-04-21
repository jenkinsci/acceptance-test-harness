package org.jenkinsci.test.acceptance.po;

import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.slave.SlaveController;

import com.google.common.base.Joiner;

import java.io.File;
import java.util.regex.Pattern;

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
        ensureConfigPage();
        find(by.path("/labelString")).sendKeys(l);
    }

    /**
     * Set up this slave as a local slave that launches slave on the same host as Jenkins
     * call this in the context of the config UI
     */
    public void asLocal() {
        File jar = new File("/tmp/slave"+createRandomName()+".jar");
        find(by.option("hudson.slaves.CommandLauncher")).click();
        find(by.input("_.command")).sendKeys(String.format(
                "sh -c 'curl -s -o %1$s %2$sjnlpJars/slave.jar && java -jar %1$s'",
                jar, url("../../")
        ));

    }

    public static Matcher<Slave> runBuildsInOrder(final Job... jobs) {
        return new Matcher<Slave>("slave run build in order: %s", Joiner.on(' ').join(jobs)) {
            @Override protected boolean matchesSafely(Slave slave) {
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
}
