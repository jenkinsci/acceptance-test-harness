package org.jenkinsci.test.acceptance.plugins.warnings;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisAction;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * Page object for Warnings Action.
 *
 * @author Ullrich Hafner
 */
public class WarningsAction extends AnalysisAction {
    private static final String PLUGIN = "warnings";

    public WarningsAction(final Build parent) {
        super(parent, PLUGIN);
    }

    public WarningsAction(final Job parent) {
        super(parent, PLUGIN);
    }

    @Override
    public String getResultUrl() {
        return super.getResultUrl().replace(PLUGIN, PLUGIN + ".*");
    }
}