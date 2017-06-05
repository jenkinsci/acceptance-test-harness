package plugins;


import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.mission_control.MissionControlView;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

@WithPlugins("mission-control")
public class MissionControlTest extends AbstractJUnitTest {

    /**
     * Test Case:
     * Check the existence and size of the build history, as well as the correct highlighting of the builds.
     */
    @Test
    public void testBuildHistory() {
        // create new mission control view and configure it
        int historySize = 8;
        MissionControlView view = jenkins.views.create(MissionControlView.class, "mission-control-sample-view");
        view.configure();
        view.setHideBuildHistory(false);
        view.setHistorySize(historySize);
        view.save();

        String strSimpleJob = "simple-job";
        String strFailedJob = "simple-failed-job";
        // create new freestyle job and build it n-times
        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class, strSimpleJob);
        for (int i = 0; i < historySize + 2; ++i) {
            job.startBuild().waitUntilFinished();
        }
        job = jenkins.jobs.create(FreeStyleJob.class, strFailedJob);
        job.configure();
        // add shell build step to cause the job to fail
        job.addShellStep("sh <");
        job.save();
        job.startBuild().waitUntilFinished();

        // open mission control view and assert that no build entries are being displayed
        // (after the creation of a new mission control view the configuration needs to be reloaded)
        view.open();
        WebElement buildHistory = driver.findElement(By.id("jenkinsBuildHistory"));
        Assert.assertEquals(0, buildHistory.findElements(By.xpath(".//tbody/tr")).size(), 0);

        // reload configuration (alternative: jenkins.restart(), very inefficient for this task)
        view.reloadConfiguration();

        // open mission control view again and assert that the n-builds are being displayed
        view.open();
        buildHistory = driver.findElement(By.id("jenkinsBuildHistory"));
        Assert.assertEquals(historySize, buildHistory.findElements(By.xpath(".//tbody/tr")).size(), 0);
        // check for correct highlighting of the builds
        WebElement failedBuild = buildHistory.findElement(By.xpath(".//tbody/tr[td='" + strFailedJob + "']"));
        assertThat(failedBuild.getAttribute("class"), containsString("danger"));
        for (WebElement successfulBuild : buildHistory.findElements(By.xpath(".//tbody/tr[td='" + strSimpleJob + "']"))) {
            assertThat(successfulBuild.getAttribute("class"), containsString(""));
        }
    }

    /**
     * Test Case:
     * Check the correct highlighting of different jobs statuses.
     */
    @Test
    public void testJobStatuses() {
        // create new mission control view and configure it
        MissionControlView view = jenkins.views.create(MissionControlView.class, "mission-control-sample-view");
        view.configure();
        view.setHideBuildHistory(false);
        view.setHideJobs(false);
        view.save();
        view.reloadConfiguration();

        // create freestyle jobs with different statuses
        String strJobNotBuild = "simple-job-not-build";
        String strBuildSuccess = "simple-job-success";
        String strBuildFailed = "simple-job-failed";
        FreeStyleJob jobNotBuild = jenkins.jobs.create(FreeStyleJob.class, strJobNotBuild);
        FreeStyleJob jobSuccess = jenkins.jobs.create(FreeStyleJob.class, strBuildSuccess);
        jobSuccess.startBuild().waitUntilFinished();
        FreeStyleJob jobFailed = jenkins.jobs.create(FreeStyleJob.class, strBuildFailed);
        jobFailed.configure();
        // add shell build step to cause the job to fail
        jobFailed.addShellStep("sh <");
        jobFailed.save();
        jobFailed.startBuild().waitUntilFinished();

        view.open();
        // check for the correct highlighting of the jobs
        WebElement jobContainer = driver.findElement(By.id("jenkinsJobStatuses"));
        WebElement btnNotBuild = jobContainer.findElement(By.xpath("//button[text()='" + strJobNotBuild + "']"));
        assertThat(btnNotBuild.getAttribute("class"), containsString("invert-text-color"));
        WebElement btnBuildSuccess = jobContainer.findElement(By.xpath("//button[text()='" + strBuildSuccess + "']"));
        assertThat(btnBuildSuccess.getAttribute("class"), containsString("btn-success"));
        WebElement btnBuildFailed = jobContainer.findElement(By.xpath("//button[text()='" + strBuildFailed + "']"));
        assertThat(btnBuildFailed.getAttribute("class"), containsString("btn-danger"));
    }
}
