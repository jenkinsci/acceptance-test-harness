package org.jenkinsci.test.acceptance.plugins.dashboard_view;

import com.google.inject.Injector;
import org.hamcrest.Description;
import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisPlugin;
import org.jenkinsci.test.acceptance.po.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
    public <T extends AbstractDashboardViewPortlet> T addBottomPortlet(Class<T> portletClass) {
        bottomPortlet.selectDropdownMenu(portletClass);
        String path = last(by.css("[name='bottomPortlet']")).getAttribute("path");
        T portlet = newInstance(portletClass, this, path);
        bottomPortlets.add(portlet);
        return portlet;
    }

    /**
     * @return the bottom portlet to the corresponding type
     */
    public <T extends AbstractDashboardViewPortlet> T getBottomPortlet(Class<T> portletClass) {
        for (AbstractDashboardViewPortlet p : bottomPortlets) {
            if (portletClass.isAssignableFrom(p.getClass()))
                return (T) p;
        }
        throw new java.util.NoSuchElementException();
    }

    public static Matcher<DashboardView> hasWarningsFor(final Job job, final AnalysisPlugin plugin, final int warningsCount) {
        return new Matcher<DashboardView>(" shows %s warnings for plugin %s and job %s", warningsCount, plugin.getId(), job.name) {
            @Override
            public boolean matchesSafely(final DashboardView view) {
                view.open();
                try {
                    WebElement warningsLink = view.find(by.css("a[href='job/" + job.name + "/" + plugin.getId() + "']"));
                    String linkText = warningsLink.getText();
                    return Integer.parseInt(linkText) == warningsCount;
                } catch (NoSuchElementException e) {
                } catch (NumberFormatException e) {
                }
                return false;
            }

            @Override
            public void describeMismatchSafely(final DashboardView view, final Description desc) {
                desc.appendText("Portlet does not show expected warnings for plugin " + plugin.getId());
            }
        };
    }
}
