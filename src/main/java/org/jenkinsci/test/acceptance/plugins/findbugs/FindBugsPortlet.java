package org.jenkinsci.test.acceptance.plugins.findbugs;

import org.jenkinsci.test.acceptance.plugins.dashboard_view.AbstractDashboardViewPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * A FindBugs portlet for {@link org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView}.
 *
 * @author Fabian Trampusch
 */
@Describable("FindBugs warnings per project")
public class FindBugsPortlet extends AbstractDashboardViewPortlet {
    public FindBugsPortlet(final DashboardView parent, final String path) {
        super(parent, path);
    }
}
