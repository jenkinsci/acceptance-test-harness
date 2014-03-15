package org.jenkinsci.test.acceptance.plugins.dashboard_view;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.View;
import org.jenkinsci.test.acceptance.po.ViewPageObject;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
@ViewPageObject("Dashboard")
public class DashboardView extends View {
    public final Control topPortlet = new Control(this,"/hetero-list-add[topPortlet]");
    public final Control bottomPortlet = new Control(this,"/hetero-list-add[bottomPortlet]");

    public DashboardView(Injector injector, URL url) {
        super(injector, url);
    }
}
