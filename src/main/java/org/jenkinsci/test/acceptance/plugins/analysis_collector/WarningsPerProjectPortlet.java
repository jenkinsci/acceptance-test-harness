package org.jenkinsci.test.acceptance.plugins.analysis_collector;

import org.jenkinsci.test.acceptance.plugins.dashboard_view.AbstractDashboardViewPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * Portlet "Warnings per project" of Analysis Collector Plugin.
 *
 * @author Michael Prankl
 */
@Describable("Warnings per project")
public class WarningsPerProjectPortlet extends AbstractDashboardViewPortlet {
    private AnalysisCollectorPluginArea pluginArea;

    private Control hideZeroWarningsProjects = control("canHideZeroWarningsProjects");
    private Control showImagesInTableHeader = control("useImages");

    public WarningsPerProjectPortlet(DashboardView parent, String path) {
        super(parent, path);
        this.pluginArea = new AnalysisCollectorPluginArea(parent, path);
    }

    public WarningsPerProjectPortlet hideZeroWarningsProjects(boolean checked) {
        hideZeroWarningsProjects.check(checked);
        return this;
    }

    public WarningsPerProjectPortlet showImagesInTableHeader(boolean checked) {
        showImagesInTableHeader.check(checked);
        return this;
    }

    /**
     * Select if the warnings of given plugin should be included in the portlet.
     *
     * @param plugin  the Plugin
     * @param checked true or false
     */
    public void checkCollectedPlugin(AnalysisPlugin plugin, boolean checked) {
        plugin.check(this.pluginArea, checked);
    }

}
