package org.jenkinsci.test.acceptance.po;

import java.util.concurrent.Callable;

/**
 * @author Vivek Pandey
 */
public class RemoteSshSlave extends Slave {
    protected RemoteSshSlave(Jenkins j, String name) {
        super(j, name);
    }

    public static Slave create(Jenkins j, String privKey, String host){
        return create(j,createRandomName(),privKey,host);
    }

    public static Slave create(Jenkins j, String name, String privKey, String host) {
        j.visit("computer/new");

        j.find(by.input("name")).sendKeys(name);
        j.find(by.input("hudson.slaves.DumbSlave")).click();
        j.clickButton("OK");
        // This form submission will drop us on the configure page

        // Just to make sure the dumb slave is set up properly, we should seed it
        // with a FS root and executors
        final RemoteSshSlave s = new RemoteSshSlave(j,name);
        s.setExecutors(1);
        s.setRemoteFs("/tmp/"+name);
        s.selectDropdownMenu("Launch slave agents on Unix machines via SSH", s.find(by.path("/")));
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
