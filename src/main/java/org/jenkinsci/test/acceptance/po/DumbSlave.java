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
    public DumbSlave(Jenkins j, String name) {
        super(j, name);
    }
}
