package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import java.net.URL;
import org.openqa.selenium.WebElement;

/**
 * A freestyle multi-branch job (requires installation of multi-branch-project-plugin).
 *
 * @author Ullrich Hafner
 */
@Describable("com.github.mjdetullio.jenkins.plugins.multibranch.FreeStyleMultiBranchProject")
public class FreeStyleMultiBranchJob extends Job {
    public FreeStyleMultiBranchJob(Injector injector, URL url, String name) {
        super(injector, url, name);
    }

    @Override
    public <T extends BuildStep> T addBuildStep(Class<T> type) {
        return addStep(type, "builder");
    }

    @Override
    public <T extends PostBuildStep> T addPublisher(Class<T> type) {
        T p = addStep(type, "publisher");

        publishers.add(p);
        return p;
    }

    private <T extends Step> T addStep(Class<T> type, String section) {
        ensureConfigPage();

        control(by.path("/projectFactory/hetero-list-add[%s]", section)).selectDropdownMenu(type);
        waitFor(by.xpath("//div[@name='%s']", section), 3); // it takes some time until the element is visible
        WebElement last = last(by.xpath("//div[@name='%s']", section));
        String path = last.getAttribute("path");

        return newInstance(type, this, path);
    }
}
