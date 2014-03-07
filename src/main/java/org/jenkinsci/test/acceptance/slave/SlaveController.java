package org.jenkinsci.test.acceptance.slave;

import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Slave;

/**
 * @author Kohsuke Kawaguchi
 */
public abstract class SlaveController {
    public abstract Slave install(Jenkins jenkinsToInstallTo);
    public abstract void start();
    public abstract void stop();
}
