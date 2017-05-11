package plugins;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.BuildStatisticsPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.BuildStatisticsPortlet.JobType;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.LatestBuildsPortlet;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.junit.Assert.assertThat;

@WithPlugins("dashboard-view")
public class DashboardViewPluginTest extends AbstractJobRelatedTest {
    @Test
    public void configure_dashboard() {
        DashboardView v = jenkins.views.create(DashboardView.class);
        v.configure();
        {
            v.topPortlet.click();
            clickLink("Build statistics");

            v.bottomPortlet.click();
            clickLink("Jenkins jobs list");
        }
        v.save();

        FreeStyleJob j = v.jobs.create(FreeStyleJob.class, "job_in_view");

        v.open();
        v.build(j.name);
        j.getLastBuild().shouldSucceed();

    }

    @Test
    public void buildStatisticsPortlet_success() {
        DashboardView v = createDashboardView();
        BuildStatisticsPortlet stats = v.addBottomPortlet(BuildStatisticsPortlet.class);
        v.save();

        buildSuccessfulJob(createFreeStyleJob());

        v.open();

        assertThat(stats.getNumberOfBuilds(JobType.SUCCESS), is(1));
    }

    @Test
    public void buildStatisticsPortlet_Percentage() {
        DashboardView v = createDashboardView();
        BuildStatisticsPortlet stats = v.addBottomPortlet(BuildStatisticsPortlet.class);
        v.save();

        FreeStyleJob success = createFreeStyleJob();
        FreeStyleJob failing = createFailingFreeStyleJob();
        FreeStyleJob unstable = createUnstableFreeStyleJob();

        buildSuccessfulJob(success);
        v.open();
        assertThat(stats.getPercentageOfBuilds(JobType.SUCCESS), is("100.0"));


        buildFailingJob(failing);
        v.open();
        assertThat(stats.getPercentageOfBuilds(JobType.SUCCESS), is("50.0"));
        assertThat(stats.getPercentageOfBuilds(JobType.FAILED), is("50.0"));

        buildFailingJob(failing);
        v.open();
        assertThat(stats.getPercentageOfBuilds(JobType.SUCCESS), is("33.33"));
        assertThat(stats.getPercentageOfBuilds(JobType.FAILED), is("66.67"));

        buildUnstableJob(unstable);
        v.open();
        assertThat(stats.getPercentageOfBuilds(JobType.SUCCESS), is("25.0"));
        assertThat(stats.getPercentageOfBuilds(JobType.FAILED), is("50.0"));
        assertThat(stats.getPercentageOfBuilds(JobType.UNSTABLE), is("25.0"));
    }

    @Test
    public void buildStatisticsPortlet_failedNr() {
        DashboardView v = createDashboardView();
        BuildStatisticsPortlet stats = v.addBottomPortlet(BuildStatisticsPortlet.class);
        v.save();

        buildFailingJob(createFailingFreeStyleJob());

        v.open();

        assertThat(stats.getNumberOfBuilds(JobType.FAILED), is(1));
    }

    @Test
    public void buildStatisticsPortlet_unstableNr() {
        DashboardView v = createDashboardView();
        BuildStatisticsPortlet stats = v.addBottomPortlet(BuildStatisticsPortlet.class);
        v.save();

        buildUnstableJob(createUnstableFreeStyleJob());

        v.open();

        assertThat(stats.getNumberOfBuilds(JobType.UNSTABLE), is(1));
    }

    @Test
    public void buildStatisticsPortlet_totalBuilds() {
        DashboardView v = createDashboardView();
        BuildStatisticsPortlet stats = v.addBottomPortlet(BuildStatisticsPortlet.class);
        v.save();

        FreeStyleJob successJob = createFreeStyleJob();
        FreeStyleJob failingJob = createFailingFreeStyleJob();
        FreeStyleJob unstableJob = createUnstableFreeStyleJob();

        buildUnstableJob(unstableJob);
        buildSuccessfulJob(successJob);
        buildSuccessfulJob(successJob);
        buildFailingJob(failingJob);

        v.open();

        assertThat(stats.getNumberOfBuilds(JobType.TOTAL), is(4));
    }

    @Test
    public void latestsBuildsPortlet_correctJobAndBuild() {
        DashboardView v = createDashboardView();
        LatestBuildsPortlet latestBuilds = v.addBottomPortlet(LatestBuildsPortlet.class);
        v.save();


        FreeStyleJob job = createFreeStyleJob();

        v.open();
        assertThat(latestBuilds.hasJob(job.name), is(false));

        Build build1 = buildSuccessfulJob(job);
        Build build2 = buildSuccessfulJob(job);


        v.open();
        assertThat(latestBuilds.hasJob(job.name), is(true));
        assertThat(latestBuilds.hasBuild(build1.getNumber()), is(true));
        assertThat(latestBuilds.hasBuild(build2.getNumber()), is(true));
    }

    @Test
    public void latestsBuildsPortlet_correctJobLink() {
        DashboardView v = createDashboardView();
        LatestBuildsPortlet latestBuilds = v.addBottomPortlet(LatestBuildsPortlet.class);
        v.save();


        FreeStyleJob job = createFreeStyleJob();
        buildSuccessfulJob(job);

        v.open();
        latestBuilds.openJob(job.name);

        assertThat(driver, hasContent("Project " + job.name));
    }

    @Test
    public void latestsBuildsPortlet_correctBuildLink() {
        DashboardView v = createDashboardView();
        LatestBuildsPortlet latestBuilds = v.addBottomPortlet(LatestBuildsPortlet.class);
        v.save();


        Build build = buildSuccessfulJob(createFreeStyleJob());

        v.open();
        latestBuilds.openBuild(build.getNumber());

        assertThat(driver, hasContent("Build #" + build.getNumber()));
    }

    @Test
    public void latestsBuildsPortlet_onlyLatest() {
        DashboardView v = createDashboardView();
        LatestBuildsPortlet latestBuilds = v.addBottomPortlet(LatestBuildsPortlet.class);
        v.save();

        FreeStyleJob job = createFreeStyleJob();

        for (int i = 0; i <= LatestBuildsPortlet.NUMBER_OF_BUILDS + 1; i++)
            buildSuccessfulJob(job);

        v.open();
        assertThat(latestBuilds.hasBuild(1), is(false));
    }

    /**
     * Creates a default dashboard view matching all jobs.
     *
     * @return default dashboard view
     */
    private DashboardView createDashboardView() {
        DashboardView v = jenkins.views.create(DashboardView.class);
        v.configure();
        v.matchAllJobs();
        return v;
    }
}
