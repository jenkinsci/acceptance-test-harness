package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.slave.SlaveController;

import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Built-in standard slave type.
 *
 * To create a new slave into a test, use {@link SlaveController}.
 *
 * @author Kohsuke Kawaguchi
 */
public class DumbSlave extends Slave {
    // config page elements
    public final Control description = control("/nodeDescription");
    public final Control executors = control("/numExecutors");
    public final Control remoteFS = control("/remoteFS");
    public final Control labels = control("/labelString");
    public final Control launchMethod = control("/");   // TODO: this path looks rather buggy to me

    public DumbSlave(Jenkins j, String name) {
        super(j, name);
    }

    /**
     * Selects the specified launcher, and return the page object to bind to it.
     */
    public <T extends ComputerLauncher> T setLauncher(Class<T> type) {
        String sut_type = type.getAnnotation(ComputerLauncherPageObject.class).value();

        launchMethod.select(sut_type);

        try {
            return type.getConstructor(PageObject.class,String.class)
                    .newInstance(this,"/launcher");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
