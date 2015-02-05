package org.jenkinsci.test.acceptance.plugins.findbugs;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisAction;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;

/**
 * Page object for Findbugs action.
 *
 * @author Fabian Trampusch
 */
public class FindbugsAction extends AnalysisAction {
    public FindbugsAction(final ContainerPageObject parent) {
        super(parent, "findbugs");
    }
}
