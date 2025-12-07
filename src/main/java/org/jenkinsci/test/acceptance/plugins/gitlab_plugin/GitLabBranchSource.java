package org.jenkinsci.test.acceptance.plugins.gitlab_plugin;

import org.jenkinsci.test.acceptance.plugins.workflow_multibranch.BranchSource;
import org.jenkinsci.test.acceptance.plugins.workflow_shared_library.WorkflowSharedLibrary;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.WorkflowMultiBranchJob;

@Describable("GitLab Project")
public class GitLabBranchSource extends BranchSource {

    public GitLabBranchSource(WorkflowMultiBranchJob job, String path) {
        super(job, path);
    }

    public GitLabBranchSource(WorkflowSharedLibrary sharedLibrary, String path) {
        super(sharedLibrary, path);
    }

    public void setOwner(String owner) {
        find(by.path("/sources/source/projectOwner")).sendKeys(owner);
    }

    public void enableTagDiscovery() {
        control("/hetero-list-add[traits]").selectDropdownMenu("Discover tags");
    }
}
