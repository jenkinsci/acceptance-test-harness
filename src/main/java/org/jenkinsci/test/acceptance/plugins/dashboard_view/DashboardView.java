package org.jenkinsci.test.acceptance.plugins.dashboard_view;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.View;

import com.google.inject.Injector;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Dashboard")
public class DashboardView extends View {
    public final Control topPortlet = new Control(this, "/hetero-list-add[topPortlet]");
    public final Control bottomPortlet = new Control(this, "/hetero-list-add[bottomPortlet]");

    private List<AbstractDashboardViewPortlet> bottomPortlets = new ArrayList<>();

    public DashboardView(Injector injector, URL url) {
        super(injector, url);
    }

    /**
     * Adds a new bottom portlet.
     *
     * @param portletClass The class of the portlet.
     * @param <T>          The type constraint for portlets.
     * @return The new portlet.
     */
    public <T extends AbstractDashboardViewPortlet> T addBottomPortlet(final Class<T> portletClass) {
        String path = createPageArea("/bottomPortlet", new Runnable() {
            @Override public void run() {
                bottomPortlet.selectDropdownMenu(portletClass);
            }
        });
        T portlet = newInstance(portletClass, this, path);
        bottomPortlets.add(portlet);
        return portlet;
    }

    /**
     * @return the bottom portlet to the corresponding type
     */
    public <T extends AbstractDashboardViewPortlet> T getBottomPortlet(Class<T> portletClass) {
        for (AbstractDashboardViewPortlet p : bottomPortlets) {
            if (portletClass.isInstance(p))
                return portletClass.cast(p);
        }
        throw new java.util.NoSuchElementException();
    }
}
