package org.jenkinsci.test.acceptance.slave;

import com.cloudbees.sdk.extensibility.ExtensionPoint;
import com.google.inject.Injector;
import com.google.inject.ProvidedBy;
import org.jenkinsci.test.acceptance.guice.AutoCleaned;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayer;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Slave;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Future;

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
@ProvidedBy(SlaveProvider.class)
public abstract class SlaveController extends CapybaraPortingLayer implements Closeable, AutoCleaned {
    protected SlaveController() {
        super(null);
    }

    public abstract Future<Slave> install(Jenkins jenkinsToInstallTo);
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
