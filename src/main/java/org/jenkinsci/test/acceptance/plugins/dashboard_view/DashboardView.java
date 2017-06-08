package org.jenkinsci.test.acceptance.plugins.dashboard_view;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.View;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * PageObject of DashboardView Configuration Page.
 *
 * @author Kohsuke Kawaguchi
 * @author Rene Zarwel
 */
@Describable("Dashboard")
public class DashboardView extends View {

    /**
     * List control for top portlets.
     **/
    public final Control topPortlet = new Control(this, "/hetero-list-add[topPortlet]");
    /**
     * List control for left portlets.
     **/
    public final Control leftPortlet = new Control(this, "/hetero-list-add[leftPortlet]");
    /**
     * List control for right portlets.
     **/
    public final Control rightPortlet = new Control(this, "/hetero-list-add[rightPortlet]");
    /**
     * List control for bottom portlets.
     **/
    public final Control bottomPortlet = new Control(this, "/hetero-list-add[bottomPortlet]");

    private List<AbstractDashboardViewPortlet> topPortlets = new ArrayList<>();
    private List<AbstractDashboardViewPortlet> leftPortlets = new ArrayList<>();
    private List<AbstractDashboardViewPortlet> rightPortlets = new ArrayList<>();
    private List<AbstractDashboardViewPortlet> bottomPortlets = new ArrayList<>();

    /**
     * Constructs a new {@link DashboardView}.
     *
     * @param injector Injector to use.
     * @param url      URL of view.
     */
    public DashboardView(Injector injector, URL url) {
        super(injector, url);
    }

    /**
     * Adds a new top portlet.
     *
     * @param portletClass The class of the portlet.
     * @param <T>          The type constraint for portlets.
     * @return The new portlet.
     */
    public <T extends AbstractDashboardViewPortlet> T addTopPortlet(final Class<T> portletClass) {
        String path = createPageArea("/topPortlet", () -> topPortlet.selectDropdownMenu(portletClass));
        T portlet = newInstance(portletClass, this, path);
        topPortlets.add(portlet);
        return portlet;
    }

    /**
     * Adds a new left portlet.
     *
     * @param portletClass The class of the portlet.
     * @param <T>          The type constraint for portlets.
     * @return The new portlet.
     */
    public <T extends AbstractDashboardViewPortlet> T addLeftPortlet(final Class<T> portletClass) {
        String path = createPageArea("/leftPortlet", () -> leftPortlet.selectDropdownMenu(portletClass));
        T portlet = newInstance(portletClass, this, path);
        leftPortlets.add(portlet);
        return portlet;
    }

    /**
     * Adds a new right portlet.
     *
     * @param portletClass The class of the portlet.
     * @param <T>          The type constraint for portlets.
     * @return The new portlet.
     */
    public <T extends AbstractDashboardViewPortlet> T addRightPortlet(final Class<T> portletClass) {
        String path = createPageArea("/rightPortlet", () -> rightPortlet.selectDropdownMenu(portletClass));
        T portlet = newInstance(portletClass, this, path);
        rightPortlets.add(portlet);
        return portlet;
    }

    /**
     * Adds a new bottom portlet.
     *
     * @param portletClass The class of the portlet.
     * @param <T>          The type constraint for portlets.
     * @return The new portlet.
     */
    public <T extends AbstractDashboardViewPortlet> T addBottomPortlet(final Class<T> portletClass) {
        String path = createPageArea("/bottomPortlet", () -> bottomPortlet.selectDropdownMenu(portletClass));
        T portlet = newInstance(portletClass, this, path);
        bottomPortlets.add(portlet);
        return portlet;
    }

    private <T extends AbstractDashboardViewPortlet> T getPortlet(List<AbstractDashboardViewPortlet> portlets, Class<T> portletClass) {
        for (AbstractDashboardViewPortlet p : portlets) {
            if (portletClass.isInstance(p)) {
                return portletClass.cast(p);
            }
        }
        throw new java.util.NoSuchElementException();
    }

    /**
     * Gets a portlet from the bottom of a specific class.
     *
     * @param <T>          A Portlet of type {@link AbstractDashboardViewPortlet}.
     * @param portletClass Class of portlet to get.
     * @return the bottom portlet to the corresponding type
     */
    public <T extends AbstractDashboardViewPortlet> T getTopPortlet(Class<T> portletClass) {
        return getPortlet(topPortlets, portletClass);
    }

    /**
     * Gets a portlet from the bottom of a specific class.
     *
     * @param <T>          A Portlet of type {@link AbstractDashboardViewPortlet}.
     * @param portletClass Class of portlet to get.
     * @return the bottom portlet to the corresponding type
     */
    public <T extends AbstractDashboardViewPortlet> T getLeftPortlet(Class<T> portletClass) {
        return getPortlet(leftPortlets, portletClass);
    }

    /**
     * Gets a portlet from the bottom of a specific class.
     *
     * @param <T>          A Portlet of type {@link AbstractDashboardViewPortlet}.
     * @param portletClass Class of portlet to get.
     * @return the bottom portlet to the corresponding type
     */
    public <T extends AbstractDashboardViewPortlet> T getRightPortlet(Class<T> portletClass) {
        return getPortlet(rightPortlets, portletClass);
    }

    /**
     * Gets a portlet from the bottom of a specific class.
     *
     * @param <T>          A Portlet of type {@link AbstractDashboardViewPortlet}.
     * @param portletClass Class of portlet to get.
     * @return the bottom portlet to the corresponding type
     */
    public <T extends AbstractDashboardViewPortlet> T getBottomPortlet(Class<T> portletClass) {
        return getPortlet(bottomPortlets, portletClass);
    }

    /**
     * Gets the main panel of this dashboard containing all portlets.
     *
     * @return main panel of this dashboard.
     */
    public WebElement getPanel() {
        if(!Objects.equals(getCurrentUrl(), url.toString())) {
            open();
        }
        return find(By.id("main-panel"));
    }

    /**
     * Gets the web element of a portlet in the top area of this dashboard view.
     *
     * @param name name of portlet.
     * @return web element of the portlet or null if not available.
     */
    public WebElement getPortletInTopTable(String name) {
        try {
            return getPanel().findElement(By.xpath("//table[1]/tbody/tr/td/div[contains(.,'" + name + "')]"));
        } catch (NoSuchElementException ex) {
            return null;
        }

    }

    /**
     * Gets the web element of a portlet in the left area of this dashboard view.
     * If there are no portlets in the left area but in the right area, this
     * method searches in the right area.
     *
     * @param name name of portlet.
     * @return web element of the portlet or null if not available.
     */
    public WebElement getPortletInLeftTable(String name) {
        try {
            return getPanel().findElement(By.xpath("//table[2]//td[1]/div[contains(.,'" + name + "')]"));
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    /**
     * Gets the web element of a portlet in the top area of this dashboard view.
     * If there are no portlets in the right area but in the left area, this
     * method searches in the left area.
     *
     * @param name name of portlet.
     * @return web element of the portlet or null if not available.
     */
    public WebElement getPortletInRightTable(String name) {
        try {
            if (getPanel().findElements(By.xpath("//table[2]/tbody/tr/td")).size() > 1) {
                return getPanel().findElement(By.xpath("//table[2]/tbody/tr/td[2]/div[contains(.,'" + name + "')]"));
            } else {
                return getPanel().findElement(By.xpath("//table[2]/tbody/tr/td[1]/div[contains(.,'" + name + "')]"));
            }
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    /**
     * Gets the web element of a portlet in the bottom area of this dashboard view.
     *
     * @param name name of portlet.
     * @return web element of the portlet or null if not available.
     */
    public WebElement getPortletInBottomTable(String name) {
        try {
            return getPanel().findElement(By.xpath("//table[3]/tbody/tr/td/div[contains(.,'" + name + "')]"));
        } catch (NoSuchElementException ex) {
            return null;
        }
    }
}
