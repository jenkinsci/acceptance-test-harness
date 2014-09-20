package org.jenkinsci.test.acceptance.plugins.checkstyle;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisAction;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;

/**
 * Page object for Checkstyle action.
 */
public class CheckstyleAction extends AnalysisAction {

    public CheckstyleAction(ContainerPageObject parent) {
        super(parent, "checkstyle");
    }

}
