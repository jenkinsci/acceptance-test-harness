package org.jenkinsci.test.acceptance.plugins.dashboard_view;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Abstract base class for Dashboard View Portlets.
 *
 * @author Fabian Trampusch
 */
public class AbstractDashboardViewPortlet extends PageAreaImpl {

    private Control name = control("name");

    protected AbstractDashboardViewPortlet(DashboardView parent, String path) {
        super(parent, path);
    }

    /**
     * Presses "delete" on the portlet.
     */
    public void delete() {
        control("repeatable-delete").click();
    }

    /**
     * @param name the name of the portlet
     */
    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * @return the name of the portlet
     */
    public String getName() {
        return this.name.resolve().getAttribute("value");
    }


}
