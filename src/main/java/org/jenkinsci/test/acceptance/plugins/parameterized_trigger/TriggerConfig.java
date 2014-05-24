package org.jenkinsci.test.acceptance.plugins.parameterized_trigger;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageArea;

/**
 * @author Kohsuke Kawaguchi
 */
public class TriggerConfig extends PageArea {
    public final Control projects = control("projects");
    public final Control block = control("block");

    public TriggerConfig(ParameterizedTrigger parent, String relativePath) {
        super(parent, relativePath);
    }
}
