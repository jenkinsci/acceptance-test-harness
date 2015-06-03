package org.jenkinsci.test.acceptance.plugins.checkstyle;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisAction;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * Page object for CheckStyle action.
 *
 * @author Fabian Trampusch
 */
public class CheckStyleAction extends AnalysisAction {
    private static final String PLUGIN = "checkstyle";

    public CheckStyleAction(final Build parent) {
        super(parent, PLUGIN);
    }

    public CheckStyleAction(final Job parent) {
        super(parent, PLUGIN);
    }
}
