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
    private final String linkName;
    private final String parserName;

    public WarningsAction(final Build parent, final String linkName, final String actionName) {
        super(parent, PLUGIN);
        this.linkName = linkName;
        this.parserName = actionName;
    }

    public WarningsAction(final Job parent, final String linkName, final String parserName) {
        super(parent, PLUGIN);
        this.linkName = linkName;
        this.parserName = parserName;
    }

    @Override
    public String getPluginName() {
        return parserName;
    }

    @Override
    public String getName() {
        return linkName + " Warnings";
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