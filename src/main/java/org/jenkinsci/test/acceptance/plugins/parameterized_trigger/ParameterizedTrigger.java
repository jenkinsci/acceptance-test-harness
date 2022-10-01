package org.jenkinsci.test.acceptance.plugins.parameterized_trigger;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Trigger parameterized build on other projects")
public class ParameterizedTrigger extends AbstractStep implements PostBuildStep {

    public ParameterizedTrigger(Job parent, String path) {
        super(parent, path);
    }

    public TriggerConfig getTriggerConfig(int index) {
        return new TriggerConfig(this, getPath("configs", index));
    }

    /**
     * Adds a new trigger setting.
     * <p>
     * Note that newly added trigger has one entry in there by default.
     */
    public TriggerConfig addTriggerConfig() {
        String path = createPageArea("configs", () -> clickButton("Add trigger..."));
        return new TriggerConfig(this, path);
    }
}
