package org.jenkinsci.test.acceptance.plugins.findbugs;

import org.jenkinsci.test.acceptance.plugins.dashboard_view.AbstractDashboardViewPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * A Checkstyle portlet for {@link org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView}.
 *
 * @author Fabian Trampusch
 */
@Describable("FindBugs warnings per project")
public class FindbugsWarningsPerProjectDashboardViewPortlet extends AbstractDashboardViewPortlet {

    public FindbugsWarningsPerProjectDashboardViewPortlet(DashboardView parent, String path) {
        super(parent, path);
    }

}
