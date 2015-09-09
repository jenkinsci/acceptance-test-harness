package org.jenkinsci.test.acceptance.plugins.warnings;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisAction;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisSettings;
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
    public String getName() {
        return "Java Compiler (javac)"; // TODO: check if this needs to be adaptable
    }

    @Override
    public Class<? extends AnalysisSettings> getFreeStyleSettings() {
        return WarningsBuildSettings.class;
    }

    @Override
    public String getUrl() {
        return super.getUrl().replace(PLUGIN, PLUGIN + ".*");
    }
}