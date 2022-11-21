package org.jenkinsci.test.acceptance.plugins.parameterized_trigger;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.BuildStep;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * Trigger/call build step of the parameterized trigger plug-in. Starts downstream projects.
 *
 * @author Ullrich Hafner
 */
@Describable("Trigger/call builds on other projects")
public class TriggerCallBuildStep extends AbstractStep implements BuildStep {
    public TriggerCallBuildStep(Job parent, String path) {
        super(parent, path);
    }

    public BuildTriggerConfig getBuildTriggerConfig(int index) {
        return new BuildTriggerConfig(this, getPath("configs", index));
    }

    /**
     * Adds a new trigger setting.
     * <p>
     * Note that newly added trigger has one entry in there by default.
     */
    public BuildTriggerConfig addTriggerConfig() {
        String path = createPageArea("configs", () -> clickButton("Add trigger..."));
        return new BuildTriggerConfig(this, path);
    }
}
