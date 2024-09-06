package org.jenkinsci.test.acceptance.plugins.workflow_shared_library;

import org.jenkinsci.test.acceptance.plugins.workflow_multibranch.BranchSource;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Base type for {@link PageAreaImpl} for Pipeline Shared Library.
 */
public abstract class WorkflowSharedLibrary extends PageAreaImpl {

    public final Control name = control("name");

    public WorkflowSharedLibrary(WorkflowSharedLibraryGlobalConfig config, String path) {
        super(config, path);
    }

    public abstract <T extends BranchSource> T selectSCM();
}
