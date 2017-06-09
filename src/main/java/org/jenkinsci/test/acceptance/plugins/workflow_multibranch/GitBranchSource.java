package org.jenkinsci.test.acceptance.plugins.workflow_multibranch;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.WorkflowMultiBranchJob;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

/**
 * Git Branch Source for the pipeline multi-branch plugin.
 *
 * @author Ullrich Hafner
 */
@Describable("Git")
// TODO: Remove duplicates with GitScm
public class GitBranchSource extends BranchSource {
    private final Control remote = control("remote");

    public GitBranchSource(WorkflowMultiBranchJob job, String path) {
        super(job, path);
    }

    public GitBranchSource setRemote(final String remoteUrl) {
        this.remote.set(remoteUrl);

        return this;
    }

    public GitBranchSource setCredentials(final String name) {
        Select select = new Select(control(By.className("credentials-select")).resolve());
        select.selectByVisibleText(name);

        return this;
    }
}