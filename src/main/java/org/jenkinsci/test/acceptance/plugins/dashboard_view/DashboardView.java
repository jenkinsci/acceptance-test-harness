package org.jenkinsci.test.acceptance.plugins.dashboard_view;

import com.google.inject.Injector;
import org.hamcrest.Description;
import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisPlugin;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.controls.ColumnsArea;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.controls.DashboardPortlets;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.controls.JobFiltersArea;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.controls.MainArea;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.read.BreadCrumbs;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.read.BuildExecutorStatus;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.read.MainPanel;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.read.ProjectStatusStdJobList;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.View;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

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

    public final JobFiltersArea jobFilters = new JobFiltersArea(this, "");
    public final MainArea mainArea = new MainArea(this, "");
    public final ColumnsArea columnsArea = new ColumnsArea(this, "");
    public final DashboardPortlets dashboardPortlets = new DashboardPortlets(this, "");


    public final BreadCrumbs breadCrumbs = new BreadCrumbs(this, "");
    public final BuildExecutorStatus buildExecutorStatus = new BuildExecutorStatus(this, "");
    public final ProjectStatusStdJobList projectStatus = new ProjectStatusStdJobList(this, "");
    public final MainPanel mainPanel = new MainPanel(this, "");

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
            @Override
            public void run() {
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
