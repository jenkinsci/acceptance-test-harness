package org.jenkinsci.test.acceptance.slave;

import com.cloudbees.sdk.extensibility.ExtensionPoint;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Slave;

import java.io.Closeable;
import java.io.IOException;

/**
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
