package org.jenkinsci.test.acceptance.plugins.parameterized_trigger;

import java.util.List;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.BuildStep;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.openqa.selenium.WebElement;

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
        WebElement e = self().findElements(by.name("configs")).get(index);
        return wrap(e);
    }

    /**
     * Adds a new trigger setting.
     *
     * Note that newly added trigger has one entry in there by default.
     */
    public BuildTriggerConfig addTriggerConfig() {
        find(by.button("Add trigger...")).click();

        List<WebElement> all = self().findElements(by.name("configs"));
        return wrap(all.get(all.size() - 1));
    }

    private BuildTriggerConfig wrap(WebElement e) {
        return new BuildTriggerConfig(this,
                e.getAttribute("path").substring(getPath().length() + 1));
    }
}
