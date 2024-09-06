package org.jenkinsci.test.acceptance.plugins.workflow_shared_library;

import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Global config page for Pipeline Shared Libraries plugin.
 */
public class WorkflowSharedLibraryGlobalConfig extends PageAreaImpl {

    private static final String GLOBAL_LIBRARIES_PATH = "/org-jenkinsci-plugins-workflow-libs-GlobalLibraries";

    public WorkflowSharedLibraryGlobalConfig(final Jenkins jenkins) {
        super(jenkins, GLOBAL_LIBRARIES_PATH);
    }

    public <T extends WorkflowSharedLibrary> T addSharedLibrary(final Class<T> type) {
        final String path =
                createPageArea("libraries", () -> control(by.path(GLOBAL_LIBRARIES_PATH + "/repeatable-add"))
                        .click());

        return newInstance(type, this, path);
    }
}
