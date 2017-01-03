package org.jenkinsci.test.acceptance.plugins.findbugs;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AbstractTablePortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * A FindBugs portlet for {@link DashboardView}.
 *
 * @author Fabian Trampusch
 */
@Describable("FindBugs warnings per project")
public class FindBugsPortlet extends AbstractTablePortlet {
    public FindBugsPortlet(final DashboardView parent, final String path) {
        super(parent, path);
    }
}
