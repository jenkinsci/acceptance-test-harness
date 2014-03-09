package org.jenkinsci.test.acceptance.po;

import java.io.File;

/**
 * A slave object
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Slave extends ContainerPageObject {
    private final String name;

    protected Slave(Jenkins j, String name) {
        super(j, j.url("computer/%s/",name));
        this.name = name;
    }

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
        ensureConfigPage();
        find(by.input("_.numExecutors")).sendKeys(String.valueOf(n));
        // in my chrome, I need to move the focus out from the control to have it recognize the value entered
        // perhaps it's related to the way input type=number is emulated?
        find(by.input("_.remoteFS")).click();
    }

    public void setRemoteFs(String s) {
        ensureConfigPage();
        find(by.input("_.remoteFS")).sendKeys(s);
    }

    public void setLabels(String l) {
        ensureConfigPage();
        find(by.input("_.labelString")).sendKeys(l);
    }

    /**
     * Set up this slave as a local slave that launches slave on the same host as Jenkins
     * call this in the context of the config UI
     */
    public void asLocal() {
        ensureConfigPage();

        File jar = new File("/tmp/"+name+"/slave"+createRandomName()+".jar");
        find(by.option("hudson.slaves.CommandLauncher")).click();
        find(by.input("_.command")).sendKeys(String.format(
                "sh -c 'curl -s -o %1$s %2$s/jnlpJars/slave.jar && java -jar %1$s'",
                jar, url("../..")
        ));

    }
}
