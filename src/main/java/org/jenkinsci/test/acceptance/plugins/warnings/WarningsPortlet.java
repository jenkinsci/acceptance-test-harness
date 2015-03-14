package org.jenkinsci.test.acceptance.plugins.warnings;

import org.jenkinsci.test.acceptance.plugins.dashboard_view.AbstractDashboardViewPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * A compiler warnings portlet for {@link DashboardView}.
 *
 * @author Ullrich Hafner
 */
@Describable("Compiler warnings per project")
public class WarningsPortlet extends AbstractDashboardViewPortlet {
    private Control hideZeroWarningsProjects = control("canHideZeroWarningsProjects");
    private Control parserName = control("parserName");

    public WarningsPortlet(final DashboardView parent, final String path) {
        super(parent, path);
    }

    public void hideZeroWarningsProjects(final boolean checked) {
        hideZeroWarningsProjects.check(checked);
    }

    public void setParser(final String name) {
        parserName.select(name);
    }
}
