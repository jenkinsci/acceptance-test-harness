package org.jenkinsci.test.acceptance.plugins.findbugs;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisAction;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisSettings;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * Page object for FindBugs action.
 *
 * @author Fabian Trampusch
 */
public class FindBugsAction extends AnalysisAction {
    private static final String PLUGIN = "findbugs";

    public FindBugsAction(final Build parent) {
        super(parent, PLUGIN);
    }

    public FindBugsAction(final Job parent) {
        super(parent, PLUGIN);
    }

    @Override
    public String getPluginName() {
        return "FindBugs";
    }

    @Override
    public Class<? extends AnalysisSettings> getFreeStyleSettings() {
        return FindBugsFreestyleSettings.class;
    }
}
