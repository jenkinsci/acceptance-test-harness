package org.jenkinsci.test.acceptance.plugins.pmd;

import org.jenkinsci.test.acceptance.plugins.dashboard_view.AbstractDashboardViewPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * A Checkstyle portlet for {@link org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView}.
 *
 * @author Fabian Trampusch
 */
@Describable("PMD warnings per project")
public class PmdWarningsPerProjectDashboardViewPortlet extends AbstractDashboardViewPortlet {

    public PmdWarningsPerProjectDashboardViewPortlet(DashboardView parent, String path) {
        super(parent, path);
    }

}
