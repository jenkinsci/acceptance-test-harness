package org.jenkinsci.test.acceptance.plugins.mission_control;

import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * A {@link PageAreaImpl} of the {@link MissionControlView} which offers specific methods to retrieve informations from it.
 */
public class BuildHistoryArea extends PageAreaImpl {

    // The parent mission control view of the build history area
    private MissionControlView parent;
    // The basic element of the page area
    private WebElement buildHistory;

    /**
     * Constructor.
     *
     * @param view The parent mission control view.
     */
    public BuildHistoryArea(MissionControlView view) {
        super(view, "");
        this.parent = view;
    }

    /**
     * Ensures that the parent {@link MissionControlView} is open. Subsequently, sets the internal buildHistory
     * to eliminate the risk of a {@link org.openqa.selenium.StaleElementReferenceException}.
     */
    private void setBuildHistory() {
        parent.ensureViewIsOpen();
        buildHistory = driver.findElement(By.id("jenkinsBuildHistory"));
    }

    /**
     * Determines the size of the build history.
     *
     * @return The size of the build history.
     */
    public int getBuildHistorySize() {
        setBuildHistory();
        return buildHistory.findElements(By.xpath(".//tbody/tr")).size();
    }

    /**
     * Retrieves all builds as {@link WebElement} of a specific job.
     *
     * @param jobname The name of the job.
     * @return A {@link List} of all builds of the job.
     */
    public List<WebElement> getBuildsByJobName(String jobname) {
        setBuildHistory();
        return buildHistory.findElements(By.xpath(".//tbody/tr[td='" + jobname + "']"));
    }

    /**
     * Retrieves all failed builds as {@link WebElement} of all jobs.
     *
     * @return A {@link List} of all failed builds of all jobs.
     */
    public List<WebElement> getFailedBuilds() {
        setBuildHistory();
        return buildHistory.findElements(By.xpath(".//tbody/tr[@class='danger']"));
    }

    /**
     * Retrieves all failed builds as {@link WebElement} of a specific job.
     *
     * @return A {@link List} of all failed builds of the job.
     */
    public List<WebElement> getFailedBuildsOfJob(String jobname) {
        setBuildHistory();
        return buildHistory.findElements(By.xpath(".//tbody/tr[td='" + jobname + "' and @class='danger']"));
    }

    /**
     * Retrieves all successful builds as {@link WebElement} of all jobs.
     *
     * @return A {@link List} of all successful builds of all jobs.
     */
    public List<WebElement> getSuccessfulBuilds() {
        setBuildHistory();
        return buildHistory.findElements(By.xpath(".//tbody/tr[@class='']"));
    }

    /**
     * Retrieves all successful builds as {@link WebElement} of a specific job.
     *
     * @return A {@link List} of all successful builds of the job.
     */
    public List<WebElement> getSuccessfulBuildsOfJob(String jobname) {
        setBuildHistory();
        return buildHistory.findElements(By.xpath(".//tbody/tr[td='" + jobname + "' and @class='']"));
    }
}