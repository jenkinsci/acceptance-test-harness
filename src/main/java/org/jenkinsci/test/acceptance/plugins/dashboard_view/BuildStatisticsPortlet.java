package org.jenkinsci.test.acceptance.plugins.dashboard_view;

import org.jenkinsci.test.acceptance.po.Describable;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * The basic build statistics portlet shipped with the dashboard view plugin.
 *
 * @author Rene Zarwel
 */
@Describable("Build statistics")
public class BuildStatisticsPortlet extends AbstractDashboardViewPortlet {

    /**
     * Different types of jobs and related default row number in statistics table.
     */
    public enum JobType {
        FAILED(2), UNSTABLE(3), SUCCESS(4), PENDING(5),
        DISABLED(6), ABORTED(7), NOT_BUILT(8), TOTAL(9);

        private final int row;

        JobType(int r) {
            row = r;
        }
    }

    /**
     * Constructs a new build statistics portlet.
     *
     * @param parent Parent dashboard view this portlet is scoped to.
     * @param path   Absolute path to the area.
     */
    public BuildStatisticsPortlet(DashboardView parent, String path) {
        super(parent, path);
    }

    /**
     * Gets the build statistics table as {@link WebElement}.
     *
     * @return build statistics table
     */
    public WebElement getTable() {
        return find(By.id("statistics"));
    }

    /**
     * Gets the number of builds of a specific {@link JobType}.
     *
     * @param type type of job
     * @return number of builds
     */
    public int getNumberOfBuilds(JobType type) {
        return Integer.valueOf(getTable().findElement(By.xpath(".//tbody/tr[" + type.row + "]/td[3]")).getText().trim());
    }

    /**
     * Gets the percentage of builds of a specific {@link JobType}.
     *
     * @param type Type of Job
     * @return percentage of builds
     */
    public String getPercentageOfBuilds(JobType type) {
        return getTable().findElement(By.xpath(".//tbody/tr[" + type.row + "]/td[4]")).getText().trim();
    }
}
