package org.jenkinsci.test.acceptance.plugins.checkstyle;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisAction;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisSettings;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.AbstractDashboardViewPortlet;
import org.jenkinsci.test.acceptance.po.AbstractListViewColumn;
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

    @Override
    public String getPluginName() {
        return "Checkstyle";
    }

    @Override
    public Class<? extends AbstractDashboardViewPortlet> getTablePortlet() {
        return CheckStylePortlet.class;
    }

    @Override
    public Class<? extends AbstractListViewColumn> getViewColumn() {
        return CheckStyleColumn.class;
    }

    @Override
    public Class<? extends AnalysisSettings> getFreeStyleSettings() {
        return CheckStyleFreestyleSettings.class;
    }
}
