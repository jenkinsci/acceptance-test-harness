package org.jenkinsci.test.acceptance.plugins.parameterized_trigger;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Configuration section of a trigger/call build step.
 *
 * @author Ullrich Hafner
 */
public class BuildTriggerConfig extends PageAreaImpl {
    public final Control projects = control("projects");
    public final Control block = control("block");

    public BuildTriggerConfig(TriggerCallBuildStep parent, String relativePath) {
        super(parent, relativePath);
    }
}
