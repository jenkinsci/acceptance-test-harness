package org.jenkinsci.test.acceptance.plugins.dashboard_view;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * The Unstable builds Portlet shipped with the dashboard view plugin.
 *
 * @author Maximilian Zollbrecht
 */
@Describable("Unstable Jobs")
public class UnstableJobsPortlet extends AbstractDashboardViewPortlet {

    private Control showOnlyFailedJobs = control("showOnlyFailedJobs");
    private Control recurseWithinFolders = control("recurse");

    public UnstableJobsPortlet(DashboardView parent, String path) {
        super(parent, path);
    }

    public void setShowOnlyFailedJobs(boolean showOnlyFailedJobs) {
        this.showOnlyFailedJobs.check(showOnlyFailedJobs);
    }

    public void setRecurseWithinFolders(boolean recurseWithinFolders) {
        this.recurseWithinFolders.check(recurseWithinFolders);
    }

    /**
     * Gets the table of unstable jobs as {@link WebElement}
     *
     * @return table
     */
    public WebElement getTable() throws NoSuchElementException {
        return find(By.xpath("//div[contains(.,'Unstable Jobs')]/following::table[1]"));
    }

    public boolean hasJob(String jobName) {
        try {
            return !getTable().findElements(By.partialLinkText(jobName)).isEmpty();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void openJob(String jobName) {
        clickLink(jobName);
    }
}
