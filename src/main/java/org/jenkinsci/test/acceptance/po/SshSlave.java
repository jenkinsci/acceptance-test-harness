package org.jenkinsci.test.acceptance.po;

import java.util.concurrent.Callable;

/**
 * @author Vivek Pandey
 */
public class SshSlave extends Slave {
    protected SshSlave(Jenkins j, String name) {
        super(j, name);
    }

    public static Slave create(Jenkins j, String host){
        return create(j,createRandomName(),host);
    }

    public static Slave create(Jenkins j, String name, String host) {
        j.visit("computer/new");

        j.find(by.input("name")).sendKeys(name);
        j.find(by.input("hudson.slaves.DumbSlave")).click();
        j.clickButton("OK");
        // This form submission will drop us on the configure page

        // Just to make sure the dumb slave is set up properly, we should seed it
        // with a FS root and executors
        final SshSlave s = new SshSlave(j,name);
        s.setExecutors(1);
        s.setRemoteFs("/tmp/"+name);
        s.find(by.input("_.host")).sendKeys(host);
        s.save();

        // Fire the slave up before we move on
        s.waitForCond(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return s.isOnline();
            }
        });

        return s;

    }
}
