package org.jenkinsci.test.acceptance.plugins.pmd;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisAction;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;

/**
 * Page object for Pmd action.
 */
public class PmdAction extends AnalysisAction {

    public PmdAction(ContainerPageObject parent) {
        super(parent, "pmd");
    }

}
