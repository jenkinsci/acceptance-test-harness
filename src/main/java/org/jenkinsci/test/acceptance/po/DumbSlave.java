package org.jenkinsci.test.acceptance.po;

import org.jenkinsci.test.acceptance.slave.SlaveController;

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
        findCaption(type, new Resolver() {
            @Override protected void resolve(String caption) {
                launchMethod.select(caption);
            }
        });
        return newInstance(type, this,"/launcher");
    }
}
