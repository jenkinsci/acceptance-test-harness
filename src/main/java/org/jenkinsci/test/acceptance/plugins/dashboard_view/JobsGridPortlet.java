package org.jenkinsci.test.acceptance.plugins.dashboard_view;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The basic jobs grid portlet shipped with the dashboard view plugin.
 *
 * @author Maximilian Zollbrecht
 */
@Describable("Jobs Grid")
public class JobsGridPortlet extends AbstractDashboardViewPortlet {

    /**
     * Default name of this Portlet.
     */
    public static final String PORTLET_NAME = "Jobs Grid";

    private Control numberOfColumns = control("columnCount");
    private Control fillColumnFirst = control("fillColumnFirst");

    /**
     * Constructs a new jobs grid portlet.
     *
     * @param parent Parent dashboard view this portlet is scoped to.
     * @param path   Absolute path to the area.
     */
    public JobsGridPortlet(DashboardView parent, String path) {
        super(parent, path);
    }

    public void setNumberOfColumns(int numberOfColumns) {
        this.numberOfColumns.set(numberOfColumns);
    }

    public void setFillColumnFirst(boolean fillColumnFirst) {
        this.fillColumnFirst.check(fillColumnFirst);
    }

    /**
     * Gets the table of unstable jobs as {@link WebElement}.
     *
     * @return The table-{@link WebElement} containing the unstable jobs.
     * @throws NoSuchElementException if the table is not found
     */
    public WebElement getTable() throws NoSuchElementException {
        return find(By.xpath("//div[contains(.,'" + PORTLET_NAME + "')]/following::table[1]"));
    }

    /**
     * Opens and returns the job at the given position in the grid.
     *
     * @param column the column of the job
     * @param row    the row of the job
     * @return The job at the given position. Null if the position exists, but is empty.
     * @throws NoSuchElementException if column or row are below 0 or higher than the column- or row-number of the grid.
     */
    @CheckForNull
    public Job getJob(int row, int column) throws NoSuchElementException {
        getPage().open();

        getTable().findElement(By.xpath("//tbody/tr[" + row + "]/td[" + column + "]"));
        try {
            WebElement link = getTable().findElement(By.xpath("//tbody/tr[" + row + "]/td[" + column + "]/a[2]"));
            String name = link.getText();
            URL url = new URL(link.getAttribute("href"));
            return new Job(injector, url, name);
        } catch (NoSuchElementException e) {
            // position exists in grid, but is empty
            return null;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
