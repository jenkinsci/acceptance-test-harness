package org.jenkinsci.test.acceptance.plugins.gitlab_plugin;

import java.time.Duration;
import org.jenkinsci.test.acceptance.plugins.workflow_multibranch.BranchSource;
import org.jenkinsci.test.acceptance.plugins.workflow_shared_library.WorkflowSharedLibrary;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.WorkflowMultiBranchJob;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;

@Describable("GitLab Project")
public class GitLabBranchSource extends BranchSource {

    public GitLabBranchSource(WorkflowMultiBranchJob job, String path) {
        super(job, path);
    }

    public GitLabBranchSource(WorkflowSharedLibrary sharedLibrary, String path) {
        super(sharedLibrary, path);
    }

    public void setOwner(String owner) {
        control("projectOwner").set(owner);
    }

    public void enableTagDiscovery() {
        control("/hetero-list-add[traits]").selectDropdownMenu("Discover tags");
    }

    public void setCheckoutCredentials(String credentialId) {
        control("credentialsId").select(credentialId);
    }

    public void setProject(String project) {
        waitFor(this)
                .withMessage("Waiting for GitLab project '%s' to appear in dropdown", project)
                .withTimeout(Duration.ofSeconds(10))
                .ignoring(NoSuchElementException.class, StaleElementReferenceException.class)
                .until(() -> {
                    control("projectPath").select(project);
                    return true;
                });
    }
}
