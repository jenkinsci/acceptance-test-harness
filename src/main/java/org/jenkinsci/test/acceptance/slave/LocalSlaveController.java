package org.jenkinsci.test.acceptance.slave;

import java.io.IOException;
import java.util.concurrent.Future;
import org.apache.http.concurrent.BasicFuture;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Slave;

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

        s.waitUntilOnline();

        BasicFuture<Slave> b = new BasicFuture<>(null);
        b.completed(s);
        return b;
    }

    @Override
    public void close() throws IOException {

    }
}
