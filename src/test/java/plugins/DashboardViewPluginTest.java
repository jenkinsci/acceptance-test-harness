package plugins;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.BuildStatisticsPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.BuildStatisticsPortlet.Jobtype;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
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
    public void buildStatisticsPortlet_successNr() {
        DashboardView v = createDashboardView();
        BuildStatisticsPortlet stats = v.addBottomPortlet(BuildStatisticsPortlet.class);
        v.save();

        buildSuccessfulJob(createFreeStyleJob());

        v.open();

        assertThat(stats.getNumberOfBuilds(Jobtype.SUCCESS), is(1));
    }

    @Test
    public void buildStatisticsPortlet_successPercentage() {
        DashboardView v = createDashboardView();
        BuildStatisticsPortlet stats = v.addBottomPortlet(BuildStatisticsPortlet.class);
        v.save();

        FreeStyleJob success = createFreeStyleJob();
        FreeStyleJob failing = createFailingFreeStyleJob();

        buildSuccessfulJob(success);
        v.open();
        assertThat(stats.getPercentageOfBuilds(Jobtype.SUCCESS), is("100.0"));


        buildFailingJob(failing);
        v.open();
        assertThat(stats.getPercentageOfBuilds(Jobtype.SUCCESS), is("50.0"));

        buildFailingJob(failing);
        v.open();
        assertThat(stats.getPercentageOfBuilds(Jobtype.SUCCESS), is("33.33"));
    }

    @Test
    public void buildStatisticsPortlet_failedNr() {
        DashboardView v = createDashboardView();
        BuildStatisticsPortlet stats = v.addBottomPortlet(BuildStatisticsPortlet.class);
        v.save();

        buildFailingJob(createFailingFreeStyleJob());

        v.open();

        assertThat(stats.getNumberOfBuilds(Jobtype.FAILED), is(1));
    }

    @Test
    public void buildStatisticsPortlet_unstableNr() {
        DashboardView v = createDashboardView();
        BuildStatisticsPortlet stats = v.addBottomPortlet(BuildStatisticsPortlet.class);
        v.save();

        buildUnstableJob(createUnstableFreeStyleJob());

        v.open();

        assertThat(stats.getNumberOfBuilds(Jobtype.UNSTABLE), is(1));
    }

    private DashboardView createDashboardView() {
        DashboardView v = jenkins.views.create(DashboardView.class);
        v.configure();
        v.matchAllJobs();
        return v;
    }
}
