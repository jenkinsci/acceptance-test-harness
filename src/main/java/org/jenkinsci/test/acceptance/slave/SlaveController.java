package org.jenkinsci.test.acceptance.slave;

import com.cloudbees.sdk.extensibility.ExtensionPoint;
import com.google.inject.ProvidedBy;
import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Future;
import org.apache.http.concurrent.BasicFuture;
import org.jenkinsci.test.acceptance.guice.AutoCleaned;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Slave;
import org.openqa.selenium.WebDriver;

/**
 * Controls where/how to run slaves.
 * <p>
 * Jenkins supports different ways of launching slaves, and sometimes this affects behaviours of plugins. This
 * abstraction hides the details of where the slaves are running (local machine? EC2? Docker?) and how slaves are hooked
 * up to Jenkins.
 * <p>
 * Test authors write tests by injecting {@link SlaveController} and calling {@link #install}.
 *
 * @author Kohsuke Kawaguchi
 * @see SlaveProvider
 */
@ExtensionPoint
@ProvidedBy(SlaveProvider.class)
public abstract class SlaveController extends CapybaraPortingLayerImpl implements Closeable, AutoCleaned {
    protected SlaveController() {
        super(null);
    }

    /**
     * Uses the given page object to create a new node and connect the slave to that Jenkins instance.
     * <p>
     * Since the actual launch of slave can take some time and it often involves a busy loop until the slave gets fully
     * launched, this method returns {@link Future} and it can return before the slave is fully connected.
     * <p>
     * However, this does not mean the setup process can run entirely in another thread, as {@link WebDriver} do not
     * support concurrent use by multiple threads.
     * <p>
     * For example, SSH slaves might synchronously interact with Jenkins to create a slave, and let Jenkins begin
     * connecting to it, but this method would return without waiting for the slave to fully come online. Then later
     * when {@link Future#get()} method is invoked, it'll check back the slave status and block until the slave becomes
     * online.
     * <p>
     * This design improves the speed of connecting multiple slaves.
     * <p>
     * TODO: for EC2 based providers where there's also another initial delay of allocating a new machine, this
     * abstraction doesn't hide all the latencies sufficiently.
     * <p>
     * When the {@link Future#get()} method returns successfully, the slave is fully online and ready to use.
     */
    public abstract Future<Slave> install(Jenkins jenkinsToInstallTo);

    /**
     * Stops the slave from the slave side, for those slave launch methods that support it.
     * <p>
     * Most notably JNLP slaves can control their own lifecycles, and for those slaves this method lets you disconnect
     * this slave. A stopped slave can be later reconnected via {@link #start()}.
     */
    public void stop() {
        // default implementation is no-op
    }

    /**
     * Reconnects a previously stopped slave.
     */
    public Future<?> start() {
        // The default implementation is no-op.
        BasicFuture<Object> f = new BasicFuture<>(null);
        f.completed(null);
        return f;
    }

    /**
     * Just a convenience method for stop+start.
     */
    public final void restart() {
        stop();
        start();
    }

    /**
     * Convey the intention that this machine is no longer needed. The implementation will releases this machine /
     * recycle the machine, etc.
     * <p>
     * Once this method is called, no other methods should be called.
     */
    @Override
    public abstract void close() throws IOException;
}
