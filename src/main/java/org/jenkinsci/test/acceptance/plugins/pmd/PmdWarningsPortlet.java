package org.jenkinsci.test.acceptance.plugins.pmd;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AbstractTablePortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * A PMD portlet for {@link DashboardView}.
 *
 * @author Fabian Trampusch
 */
@Describable("PMD warnings per project")
public class PmdWarningsPortlet extends AbstractTablePortlet {
    public PmdWarningsPortlet(final DashboardView parent, final String path) {
        super(parent, path);
    }
}
