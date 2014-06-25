package org.jenkinsci.test.acceptance.plugins.dashboard_view;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.View;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Dashboard")
public class DashboardView extends View {
    public final Control topPortlet = new Control(this,"/hetero-list-add[topPortlet]");
    public final Control bottomPortlet = new Control(this,"/hetero-list-add[bottomPortlet]");

    public DashboardView(Injector injector, URL url) {
        super(injector, url);
    }

    /**
     * Adds a new bottom portlet.
     * @param portletClass The class of the portlet.
     * @param <T> The type constraint for portlets.
     * @return The new portlet.
     */
    public <T extends AbstractDashboardViewPortlet> T addBottomPortlet(Class<T> portletClass) {
        bottomPortlet.selectDropdownMenu(portletClass);
        String path = last(by.css("[name='bottomPortlet']")).getAttribute("path");
        return newInstance(portletClass, this, path);
    }
}
