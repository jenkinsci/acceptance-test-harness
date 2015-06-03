package org.jenkinsci.test.acceptance.plugins.analysis_collector;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisAction;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * Page object for static analysis collector action.
 *
 * @author Michael Prankl
 */
public class AnalysisCollectorAction extends AnalysisAction {
    private static final String PLUGIN = "analysis";

    public AnalysisCollectorAction(final Build parent) {
        super(parent, PLUGIN);
    }

    public AnalysisCollectorAction(final Job parent) {
        super(parent, PLUGIN);
    }
}
