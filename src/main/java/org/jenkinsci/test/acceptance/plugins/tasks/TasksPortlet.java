package org.jenkinsci.test.acceptance.plugins.tasks;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AbstractTablePortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * A task scanner portlet for {@link DashboardView}.
 *
 * @author Fabian Trampusch
 */
@Describable("Open tasks per project")
public class TasksPortlet extends AbstractTablePortlet {
    public TasksPortlet(final DashboardView parent, final String path) {
        super(parent, path);
    }

}
