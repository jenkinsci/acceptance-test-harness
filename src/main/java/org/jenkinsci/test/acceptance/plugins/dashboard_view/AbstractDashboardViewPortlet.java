package org.jenkinsci.test.acceptance.plugins.dashboard_view;

import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Abstract base class for Dashboard View Portlets.
 *
 * @author Fabian Trampusch
 */
public class AbstractDashboardViewPortlet extends PageAreaImpl {

    protected AbstractDashboardViewPortlet(DashboardView parent, String path) {
        super(parent, path);
    }

    /**
     * Presses "delete" on the portlet.
     */
    public void delete() {
        control("repeatable-delete").click();
    }


}
