package org.jenkinsci.test.acceptance.plugins.mission_control;

import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * A {@link PageAreaImpl} of the {@link MissionControlView} which offers specific methods to retrieve informations from it.
 */
public class JobStatusArea extends PageAreaImpl {

    // The parent mission control view of the build history area
    private MissionControlView parent;
    // The basic element of the page area
    public WebElement jobContainer;

    /**
     * Constructor.
     *
     * @param view The parent mission control view.
     */
    public JobStatusArea(MissionControlView view) {
        super(view, "");
        this.parent = view;
    }

    /**
     * Ensures that the parent {@link MissionControlView} is open. Subsequently, sets the internal jobStatuses
     * to eliminate the risk of {@link org.openqa.selenium.StaleElementReferenceException}.
     */
    private void setJobContainer() {
        parent.ensureViewIsOpen();
        jobContainer = driver.findElement(By.id("jenkinsJobStatuses"));
    }

    /**
     * Determines the current number of jobs.
     *
     * @return The current number of jobs.
     */
    public int getNumberOfJobs(){
        setJobContainer();
        return jobContainer.findElements(By.xpath("//button")).size();
    }

    /**
     * Retrieves a job by name from the job container
     *
     * @param jobname The name of the job.
     * @return A single job entry.
     */
    public WebElement getJobByName(String jobname){
        setJobContainer();
        return jobContainer.findElement(By.xpath("//button[text()='" + jobname + "']"));
    }

    /**
     * Retrieves the status of a job, which is indicated by the class-attribute.
     *
     * @param jobname The name of the job.
     * @return The class-attribute, which contains the current status of the job.
     */
    public String getStatusOfJob(String jobname){
        setJobContainer();
        WebElement e = jobContainer.findElement(By.xpath("//button[text()='" + jobname + "']"));
        return e.getAttribute("class");
    }

    /**
     * Retrieves all jobs that are not build yet. This is indicated by the class-attribute of the job-element.
     *
     * @return A {@link List} of all jobs that are not build yet.
     */
    public List<WebElement> getNotBuildJobs(){
        setJobContainer();
        return jobContainer.findElements(By.xpath("//button[@class='invert-text-color']"));
    }

    /**
     * Retrieves all jobs that are successfully build. This is indicated by the class-attribute of the job-element.
     *
     * @return A {@link List} of all successfully build jobs.
     */
    public List<WebElement> getSuccessfulJobs(){
        setJobContainer();
        return jobContainer.findElements(By.xpath("//button[@class='btn-success']"));
    }

    /**
     * Retrieves all jobs that failed to build. This is indicated by the class-attribute of the job-element.
     *
     * @return A {@link List} of all jobs that failed to build.
     */
    public List<WebElement> getFailedJobs(){
        setJobContainer();
        return jobContainer.findElements(By.xpath("//button[@class='btn-danger']"));
    }
}
