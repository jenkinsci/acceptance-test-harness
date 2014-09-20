package org.jenkinsci.test.acceptance.plugins.findbugs;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisAction;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;

/**
 * Page object for Findbugs action.
 */
public class FindbugsAction extends AnalysisAction {
    
    public FindbugsAction(ContainerPageObject parent) {
        super(parent, "findbugs");
    }

}
