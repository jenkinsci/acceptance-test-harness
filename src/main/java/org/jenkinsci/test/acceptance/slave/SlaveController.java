package org.jenkinsci.test.acceptance.slave;

import com.cloudbees.sdk.extensibility.ExtensionPoint;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Slave;

import java.io.Closeable;
import java.io.IOException;

/**
 * Controls where/how to run slaves.
 *
 * <p>
 * Jenkins supports different ways of launching slaves, and sometimes this affects
 * behaviours of plugins. This abstraction hides the details of where the slaves
 * are running (local machine? EC2? Docker?) and how slaves are hooked up to Jenkins.
 *
 * <p>
 * Test authors write tests by injecting {@link SlaveController} and
 *
 *
 * @see SlaveProvider
 * @author Kohsuke Kawaguchi
 */
@ExtensionPoint
public abstract class SlaveController implements Closeable {
    public abstract Slave install(Jenkins jenkinsToInstallTo);
    public abstract void start();
    public abstract void stop();

    /**
     * Convey the intention that this machine is no longer needed.
     * The implementation will releases this machine / recycle the machine, etc.
     *
     * Once this method is called, no other methods should be called.
     */
    public abstract void close() throws IOException;
}
