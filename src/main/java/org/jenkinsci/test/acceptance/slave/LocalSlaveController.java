package org.jenkinsci.test.acceptance.slave;

import org.apache.http.concurrent.BasicFuture;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Slave;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Launches slaves locally on the same box as the Jenkins master.
 *
 * @author Kohsuke Kawaguchi
 */
public class LocalSlaveController extends SlaveController {
    @Override
    public Future<Slave> install(Jenkins jenkins) {
        // Just to make sure the dumb slave is set up properly, we should seed it
        // with a FS root and executors
        final DumbSlave s = jenkins.slaves.create(DumbSlave.class);

        s.asLocal();
        s.save();

        // Fire the slave up before we move on
        waitForCond(new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return s.isOnline();
            }
        });

        BasicFuture<Slave> b = new BasicFuture<>(null);
        b.completed(s);
        return b;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void close() throws IOException {

    }
}
