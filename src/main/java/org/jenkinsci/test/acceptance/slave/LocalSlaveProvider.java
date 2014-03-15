package org.jenkinsci.test.acceptance.slave;

import javax.inject.Singleton;

/**
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class LocalSlaveProvider implements SlaveProvider {
    @Override
    public SlaveController get() {
        return new LocalSlaveController();
    }
}
