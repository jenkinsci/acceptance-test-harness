package org.jenkinsci.test.acceptance.plugins.analysis_collector;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AbstractCodeStylePluginAction;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;

/**
 * PO for Static Analysis results (analysis-collector).
 * @author Michael Prankl
 */
public class AnalysisCollectorAction extends AbstractCodeStylePluginAction {
    public AnalysisCollectorAction(ContainerPageObject parent) {
        super(parent, "analysis");
    }
}
