package org.jenkinsci.test.acceptance.plugins.findbugs;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisAction;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;

/**
 * Page object for FindBugs action.
 *
 * @author Fabian Trampusch
 */
public class FindBugsAction extends AnalysisAction {
    public FindBugsAction(final ContainerPageObject parent) {
        super(parent, "findbugs");
    }
}
