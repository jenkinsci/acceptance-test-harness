package org.jenkinsci.test.acceptance.plugins.dashboard_view;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * The basic latest builds portlet shipped with the dashboard view plugin.
 *
 * @author Rene Zarwel
 */
@Describable("Latest builds")
public class LatestBuildsPortlet extends AbstractDashboardViewPortlet {

    private final Control numberOfBuilds = control("numBuilds");

    /**
     * Default name of this Portlet.
     */
    public static final String PORTLET_NAME = "Latest builds";
    /**
     * Default number of latest builds to show.
     */
    public static final int NUMBER_OF_BUILDS = 10;


    /**
     * Constructs a new latest builds portlet.
     *
     * @param parent Parent dashboard view this portlet is scoped to.
     * @param path   Absolute path to the area.
     */
    public LatestBuildsPortlet(DashboardView parent, String path) {
        super(parent, path);
    }

    /**
     * Gets the latest builds table as {@link WebElement}.
     *
     * @return latest builds table
     */
    public WebElement getTable() {
        WebElement portlet = find(By.xpath("//div[contains(.,'" + PORTLET_NAME + "')]/following::table[1]"));

        return portlet.findElement(By.id("statistics"));
    }

    private WebElement getRow(int row) {
        return getTable().findElement(By.xpath(".//tbody/tr[" + row + "]"));
    }

    /**
     * Sets the number of builds of the portlet.
     *
     * @param number the number of builds of the portlet
     */
    public void setNumberOfBuilds(int number) {
        this.numberOfBuilds.set(number);
    }

    /**
     * Returns the number of builds of the portlet.
     *
     * @return the number of builds of the portlet
     */
    public int getNumberOfBuilds() {
        return Integer.parseInt(this.numberOfBuilds.resolve().getAttribute("value"));
    }

    /**
     * Returns true if this Portlet contains a job with the given name.
     *
     * @param jobName Name of the job to look for.
     * @return True, if this Portlet contains a job with the given name.
     */
    public boolean hasJob(String jobName) {
        try {
            return !getTable().findElements(By.linkText(jobName)).isEmpty();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Returns true if this Portlet contains a build with the given number.
     *
     * @param buildNr Number of the build to look for.
     * @return True, if this Portlet contains a build with the given number.
     */
    public boolean hasBuild(int buildNr) {
        try {
            return !getTable().findElements(By.linkText("#" + buildNr)).isEmpty();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Opens the job with the given name, if it exists in the Portlet.
     *
     * @param jobName Name of the job to open.
     */
    public void openJob(String jobName) {
        clickLink(jobName);
    }

    /**
     * Opens the build with the given number, if it exists in the Portlet.
     *
     * @param buildNr number of the build to open.
     */
    public void openBuild(int buildNr) {
        clickLink("#" + buildNr);
    }
}
