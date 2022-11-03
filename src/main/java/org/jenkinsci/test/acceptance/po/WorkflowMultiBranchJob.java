package org.jenkinsci.test.acceptance.po;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.test.acceptance.plugins.workflow_multibranch.BranchSource;

import com.google.inject.Injector;
import org.openqa.selenium.WebElement;

/**
 * A pipeline multi-branch job (requires installation of multi-branch-project-plugin).
 *
 */
@Describable("org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject")
public class WorkflowMultiBranchJob extends Folder {

    public WorkflowMultiBranchJob(Injector injector, URL url, String name) {
        super(injector, url, name);
    }

    public <T extends BranchSource> T addBranchSource(final Class<T> type) {
        ensureConfigPage();

        final String path = createPageArea("/sources",
                () -> control(by.path("/hetero-list-add[sources]")).selectDropdownMenu(type));

        return newInstance(type, this, path + "/source");
    }

    public String getBranchIndexingLog() {
        try {
            return IOUtils.toString(url("indexing/console").openStream(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }

    public WorkflowMultiBranchJob waitForBranchIndexingFinished(final int timeout) {
        waitFor()
            .withTimeout(super.time.seconds(timeout), TimeUnit.MILLISECONDS)
            .until(() -> WorkflowMultiBranchJob.this.getBranchIndexingLog().contains("Finished: "));

        return this;
    }

    public WorkflowJob getJob(final String name) {
        return this.getJobs().get(WorkflowJob.class, name);
    }

    public void reIndex() {
        final List<WebElement> scanRepoNow = driver.findElements(by.xpath("//div[@class=\"task\"]//*[text()=\"Scan Repository Now\"]"));

        if (scanRepoNow.size() > 0) {
            // JENKINS-41416
            scanRepoNow.get(0).click();
        } else {
            // Previous versions
            find(by.xpath("//div[@class=\"task\"]//*[text()=\"Scan Repository\" or text()=\"Branch Indexing\"]")).click();
            find(by.xpath("//div[@class=\"subtasks\"]//*[text()=\"Run Now\"]")).click();
        }
    }

}
