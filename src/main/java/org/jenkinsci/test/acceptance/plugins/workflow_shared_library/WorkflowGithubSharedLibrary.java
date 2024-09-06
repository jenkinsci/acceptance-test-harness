package org.jenkinsci.test.acceptance.plugins.workflow_shared_library;

import org.jenkinsci.test.acceptance.plugins.workflow_multibranch.GithubBranchSource;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Base type for {@link PageAreaImpl} for Pipeline Shared Library using Github as SCM.
 */
public class WorkflowGithubSharedLibrary extends WorkflowSharedLibrary {

    public final Control modernScm = control("/");
    public final Control githubSourceCodeManagement = control("/retriever");

    public WorkflowGithubSharedLibrary(WorkflowSharedLibraryGlobalConfig config, String path) {
        super(config, path);
    }

    @Override
    public GithubBranchSource selectSCM() {
        modernScm.select("0");
        githubSourceCodeManagement.select("1");

        return new GithubBranchSource(this, this.getPath() + "/retriever/scm");
    }
}
