package org.jenkinsci.test.acceptance.plugins.checkstyle;

import org.jenkinsci.test.acceptance.plugins.dashboard_view.AbstractDashboardViewPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * A Checkstyle portlet for {@link org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView}.
 *
 * @author Fabian Trampusch
 */
@Describable("Checkstyle warnings per project")
public class CheckstyleWarningsPerProjectDashboardViewPortlet extends AbstractDashboardViewPortlet {

    public CheckstyleWarningsPerProjectDashboardViewPortlet(DashboardView parent, String path) {
        super(parent, path);
    }

}
