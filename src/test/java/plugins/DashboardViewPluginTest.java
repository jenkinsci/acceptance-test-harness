package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.BuildStatisticsPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.BuildStatisticsPortlet.Jobtype;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@WithPlugins("dashboard-view")
public class DashboardViewPluginTest extends AbstractJUnitTest {
    @Test
    public void configure_dashboard() {
        DashboardView v = jenkins.views.create(DashboardView.class, "dashboard");
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
    public void buildStats_success() {
        DashboardView v = jenkins.views.create(DashboardView.class, "dashboard");
        v.configure();
        v.matchAllJobs();

        BuildStatisticsPortlet stats = v.addBottomPortlet(BuildStatisticsPortlet.class);

        v.save();

        FreeStyleJob j = v.jobs.create(FreeStyleJob.class);
        j.save();

        j.startBuild().shouldSucceed();
        v.open();

        assertThat(stats.getNumberOfBuilds(Jobtype.SUCCESS), is(1));
    }

    @Test
    public void buildStats_failed() {
        DashboardView v = jenkins.views.create(DashboardView.class, "dashboard");
        v.configure();
        v.matchAllJobs();

        BuildStatisticsPortlet stats = v.addBottomPortlet(BuildStatisticsPortlet.class);

        v.save();

        FreeStyleJob j = v.jobs.create(FreeStyleJob.class);
        j.configure();
        j.addShellStep("exit 1");
        j.save();

        j.startBuild().shouldFail();
        v.open();

        assertThat(stats.getNumberOfBuilds(Jobtype.FAILED), is(1));
    }
}
