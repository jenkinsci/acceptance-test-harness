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

    /**
     * Constructs a new unstable jobs portlet.
     *
     * @param parent Dashboard view this portlet is scoped to.
     * @param path   Absolute path to the area.
     */
    public UnstableJobsPortlet(DashboardView parent, String path) {
        super(parent, path);
    }

    /**
     * Sets the "Recurse within folders"-parameter for this Portlet.
     *
     * @param showOnlyFailedJobs New value to set.
     */
    public void setShowOnlyFailedJobs(boolean showOnlyFailedJobs) {
        this.showOnlyFailedJobs.check(showOnlyFailedJobs);
    }

    /**
     * Gets the table of unstable jobs as {@link WebElement}.
     *
     * @return The table-{@link WebElement} containing the unstable jobs.
     * @throws NoSuchElementException if the table is not found
     */
    public WebElement getTable() throws NoSuchElementException {
        return find(By.xpath("//div[contains(.,'Unstable Jobs')]/following::table[1]"));
    }

    /**
     * Returns true if this Portlet contains a job with the given name.
     *
     * @param jobName Name of the job to look for.
     * @return True, if this Portlet contains a job with the given name.
     */
    public boolean hasJob(String jobName) {
        try {
            // Underscores in job names are prefixed with <wbr> to represent a word breaking opportunity,
            // so we need to add these new lines to te job name when filtering
            jobName = jobName.replace("_", "\n_");
            return !getTable().findElements(By.partialLinkText(jobName)).isEmpty();
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
}
