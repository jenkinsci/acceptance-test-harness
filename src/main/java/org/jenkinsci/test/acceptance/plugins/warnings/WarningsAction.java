package org.jenkinsci.test.acceptance.plugins.warnings;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisAction;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;

/**
 * Page object for Warnings Action.
 */
public class WarningsAction extends AnalysisAction {
    public WarningsAction(ContainerPageObject parent) {
        super(parent, "warnings");
    }
}