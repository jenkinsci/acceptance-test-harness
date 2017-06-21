package plugins;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.test.acceptance.Matchers.*;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.mission_control.MissionControlView;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

@WithPlugins("mission-control-view")
public class MissionControlTest extends AbstractJUnitTest {

    /**
     * Test Case: Check the existence and size of the build history, as well as
     * the correct highlighting of the builds.
     */
    @Test
    public void testBuildHistory() {
        // Create new mission control view and configure it
        int historySize = 8;
        MissionControlView view = jenkins.views.create(MissionControlView.class, "mission-control-sample-view");
        view.configure();
        view.setHideBuildHistory(false);
        view.setHistorySize(historySize);
        view.save();

        String strSimpleJob = "simple-job";
        String strFailedJob = "simple-failed-job";
        // Create new freestyle job and build it n-times
        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class, strSimpleJob);
        for (int i = 0; i < historySize; ++i) {
            job.startBuild().waitUntilFinished();
        }
        job = jenkins.jobs.create(FreeStyleJob.class, strFailedJob);
        job.configure();
        // Add invalid shell build step to cause the job to fail
        job.addShellStep("sh <");
        job.save();
        job.startBuild().waitUntilFinished();

        // Open mission control view and assert that no build entries are being displayed
        // (after the creation of a new mission control view the configuration needs to be reloaded)
        view.open();
        assertThat(view.getBuildHistoryArea().getBuildHistorySize(), is(0));

        // Reload configuration (alternative: jenkins.restart(), very inefficient for this task)
        view.reloadConfiguration();

        // Open mission control view again and assert that the n-builds are being displayed now
        view.open();
        assertThat(view.getBuildHistoryArea().getBuildHistorySize(), is(historySize));

        // Check for correct highlighting of the builds
        // Also, according to the made settings, there should be 1 failed and historySize - 1 successful builds
        // in the build history
        assertThat(view.getBuildHistoryArea().getFailedBuildsOfJob(strFailedJob), hasSize(1));
        assertThat(view.getBuildHistoryArea().getSuccessfulBuildsOfJob(strSimpleJob), hasSize(historySize - 1));
    }

    /**
     * Test Case: Check the correct highlighting of different jobs statuses.
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
        // add invalid shell build step to cause the job to fail
        jobFailed.addShellStep("sh <");
        jobFailed.save();
        jobFailed.startBuild().waitUntilFinished();

        view.open();
        // check for the correct highlighting of the jobs
        assertThat(view.getJobStatusArea().getStatusOfJob(strJobNotBuild), containsString("invert-text-color"));
        assertThat(view.getJobStatusArea().getStatusOfJob(strBuildSuccess), containsString("success"));
        assertThat(view.getJobStatusArea().getStatusOfJob(strBuildFailed), containsString("danger"));
    }

    /**
     * Test the correct highlighting of jenkins nodes.
     */
    @Test
    public void testNodeStatuses() {
        MissionControlView view = jenkins.views.create(MissionControlView.class, "mission-control-sample-view");
        view.configure(() -> view.setHideNodes(true));

        view.open();
        assertThat(driver, not(hasContent("Nodes")));

        DumbSlave slave = jenkins.slaves.create(DumbSlave.class, "test");
        slave.configure(() -> slave.setExecutors(15));

        view.configure(() -> view.setHideNodes(false));

        view.open();
        assertThat(view.getNodeStatusArea().getNumberOfNodes(), is(2));
        assertThat(view.getNodeStatusArea().getStatusOfNode("test / 15"), containsString("danger"));
        assertThat(view.getNodeStatusArea().getStatusOfNode("master / 2"), containsString("success"));
    }
}
