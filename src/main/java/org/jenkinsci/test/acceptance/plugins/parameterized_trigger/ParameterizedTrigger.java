package org.jenkinsci.test.acceptance.plugins.parameterized_trigger;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Trigger parameterized build on other projects")
public class ParameterizedTrigger extends AbstractStep implements PostBuildStep {

    public ParameterizedTrigger(Job parent, String path) {
        super(parent, path);
    }

    public TriggerConfig getTriggerConfig(int index) {
        WebElement e = self().findElements(by.name("configs")).get(index);
        return wrap(e);
    }

    /**
     * Adds a new trigger setting.
     *
     * Note that newly added trigger has one entry in there by default.
     */
    public TriggerConfig addTriggerConfig() {
        find(by.button("Add trigger...")).click();

        List<WebElement> all = self().findElements(by.name("configs"));
        return wrap(all.get(all.size()-1));
    }

    private TriggerConfig wrap(WebElement e) {
        return new TriggerConfig(this,e.getAttribute("path").substring(getPath().length()+1));
    }
}
