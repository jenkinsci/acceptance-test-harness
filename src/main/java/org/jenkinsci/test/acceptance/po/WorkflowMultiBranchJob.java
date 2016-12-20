package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.test.acceptance.plugins.workflow_multibranch.BranchSource;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * A pipeline multi-branch job (requires installation of multi-branch-project-plugin).
 *
 */
@Describable("org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject")
public class WorkflowMultiBranchJob extends FolderItem {

    public WorkflowMultiBranchJob(Injector injector, URL url, String name) {
        super(injector, url, name);
    }

    public <T extends BranchSource> T addBranchSource(final Class<T> type) {
        ensureConfigPage();

        final String path = createPageArea("/sources", new Runnable() {
            @Override public void run() {
                control(by.path("/hetero-list-add[sources]")).selectDropdownMenu(type);
            }
        });

        return newInstance(type, this, path + "/source");
    }

    public String getBranchIndexingLog() {
        try {
            return IOUtils.toString(url("indexing/console").openStream());
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }

    public WorkflowMultiBranchJob waitForBranchIndexingFinished(final int timeout) {
        waitFor()
            .withTimeout(timeout, TimeUnit.SECONDS)
            .until(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    return WorkflowMultiBranchJob.this.getBranchIndexingLog().contains("Finished: ");
                }
            });

        return this;
    }

    public WorkflowJob getJob(final String name) {
        return this.jobs.get(WorkflowJob.class, name);
    }

}
