package org.jenkinsci.test.acceptance.slave;

import javax.inject.Singleton;

/**
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class LocalSlaveProvider extends SlaveProvider {
    @Override
    public SlaveController create() {
        return new LocalSlaveController();
    }
}
