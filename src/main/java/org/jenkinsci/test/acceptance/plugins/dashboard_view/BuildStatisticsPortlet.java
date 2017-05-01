package org.jenkinsci.test.acceptance.plugins.dashboard_view;

import org.jenkinsci.test.acceptance.po.Describable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * The basic Build statistics portlet shipped with the dashboard view plugin.
 *
 * @author Rene Zarwel
 */
@Describable("Build statistics")
public class BuildStatisticsPortlet extends AbstractDashboardViewPortlet {

    public enum Jobtype {
        FAILED(2), UNSTABLE(3), SUCCESS(4), PENDING(5),
        DISABLED(6), ABORTED(7), NOT_BUILT(8), TOTAL(9);

        private final int row;

        Jobtype(int r) {
            row = r;
        }
    }

    public BuildStatisticsPortlet(DashboardView parent, String path) {
        super(parent, path);
    }

    /**
     * Gets the Buildstatistics table as {@link WebElement}
     *
     * @return table
     */
    public WebElement getTable() {
        return find(By.id("statistics"));
    }

    /**
     * Get the number of builds of a specific {@link Jobtype}
     *
     * @param type Type of Job
     * @return int
     */
    public int getNumberOfBuilds(Jobtype type) {
        return Integer.valueOf(getTable().findElement(By.xpath(".//tbody/tr[" + type.row + "]/td[3]")).getText().trim());
    }

    /**
     * Get the percentage of builds of a specific {@link Jobtype}
     *
     * @param type Type of Job
     * @return int
     */
    public String getPercentageOfBuilds(Jobtype type) {
        return getTable().findElement(By.xpath(".//tbody/tr[" + type.row + "]/td[4]")).getText().trim();
    }
}
