package org.jenkinsci.test.acceptance.plugins.pmd;

import org.jenkinsci.test.acceptance.plugins.dashboard_view.AbstractDashboardViewPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * A PMD portlet for {@link org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView}.
 *
 * @author Fabian Trampusch
 */
@Describable("PMD warnings per project")
public class PmdWarningsPortlet extends AbstractDashboardViewPortlet {
    public PmdWarningsPortlet(final DashboardView parent, final String path) {
        super(parent, path);
    }
}
