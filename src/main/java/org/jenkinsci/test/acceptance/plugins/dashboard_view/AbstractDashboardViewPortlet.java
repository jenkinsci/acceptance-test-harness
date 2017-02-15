package org.jenkinsci.test.acceptance.plugins.dashboard_view;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * A portlet that is shown in the {@link DashboardView}.
 *
 * @author Fabian Trampusch
 */
public class AbstractDashboardViewPortlet extends PageAreaImpl {
    private Control name = control("name");

    protected AbstractDashboardViewPortlet(DashboardView parent, String path) {
        super(parent, path);
    }

    /**
     * Deletes the portlet, i.e. removes it from the dashboard.
     */
    public void delete() {
        control("repeatable-delete").click();
    }

    /**
     * Sets the name of the portlet.
     *
     * @param name the name of the portlet
     */
    public void setName(String name) {
        this.name.set(name);
    }

    /**
     * Returns the name of the portlet.
     *
     * @return the name of the portlet
     */
    public String getName() {
        return this.name.resolve().getAttribute("value");
    }
}
