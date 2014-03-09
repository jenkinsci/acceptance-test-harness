package org.jenkinsci.test.acceptance.po;

import java.util.concurrent.Callable;

/**
 * @author Kohsuke Kawaguchi
 */
public class DumbSlave extends Slave {
    public DumbSlave(Jenkins j, String name) {
        super(j, name);
    }

    public static DumbSlave create(Jenkins j) {
        return create(j,createRandomName());
    }

    public static DumbSlave create(Jenkins j, String name) {
        j.visit("computer/new");

        j.find(by.input("name")).sendKeys(name);
        j.find(by.input("hudson.slaves.DumbSlave")).click();
        j.clickButton("OK");
        // This form submission will drop us on the configure page

        // Just to make sure the dumb slave is set up properly, we should seed it
        // with a FS root and executors
        final DumbSlave s = new DumbSlave(j,name);
        s.configure();
        s.setExecutors(1);
        s.setRemoteFs("/tmp/"+name);
        s.asLocal();
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
