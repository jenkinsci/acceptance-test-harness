package org.jenkinsci.test.acceptance.plugins.analysis_core;

import org.jenkinsci.test.acceptance.plugins.dashboard_view.AbstractDashboardViewPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Control;

/**
 * A portlet that show the number of warnings in a table.
 *
 * @author Ullrich Hafner
 */
public class AbstractTablePortlet extends AbstractDashboardViewPortlet {
    private Control hideZeroWarningsProjects = control("canHideZeroWarningsProjects");

    protected AbstractTablePortlet(DashboardView parent, String path) {
        super(parent, path);
    }

    public void hideZeroWarningsProjects(final boolean checked) {
        hideZeroWarningsProjects.check(checked);
    }
}
