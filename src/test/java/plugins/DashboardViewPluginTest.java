package plugins;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.UnstableJobsPortlet;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JUnitPublisher;
import org.junit.Test;

import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@WithPlugins("dashboard-view")
public class DashboardViewPluginTest extends AbstractJobRelatedTest {
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
    public void unstableJobsPortlet_failedJob() {
        DashboardView v = jenkins.views.create(DashboardView.class, "dashboard");
        v.configure();
        UnstableJobsPortlet unstableJobsPortlet = v.addBottomPortlet(UnstableJobsPortlet.class);
        v.save();

        FreeStyleJob j = v.jobs.create(FreeStyleJob.class);
        j.configure();
        j.addShellStep("exit 1");
        j.save();
        buildFailingJob(j);

        v.open();

        assertTrue(unstableJobsPortlet.hasJob(j.name));

        unstableJobsPortlet.openJob(j.name);

        assertThat(driver, hasContent("Project " + j.name));
        assertTrue(getCurrentUrl().contains(j.name));
    }

    @Test
    public void unstableJobsPortlet_successfulJob() {
        DashboardView v = jenkins.views.create(DashboardView.class, "dashboard");
        v.configure();
        UnstableJobsPortlet unstableJobsPortlet = v.addBottomPortlet(UnstableJobsPortlet.class);
        v.save();

        FreeStyleJob j = v.jobs.create(FreeStyleJob.class);
        buildSuccessfulJob(j);

        v.open();

        assertTrue(!unstableJobsPortlet.hasJob(j.name));
    }

    @Test
    public void unstableJobsPortlet_unstableJob() {
        DashboardView v = jenkins.views.create(DashboardView.class, "dashboard");
        v.configure();
        UnstableJobsPortlet unstableJobsPortlet = v.addBottomPortlet(UnstableJobsPortlet.class);
        v.save();

        FreeStyleJob j = v.jobs.create(FreeStyleJob.class);
        j.configure();
        String resultFileName = "status.xml";
        j.addShellStep(
                "echo '<testsuite><testcase classname=\"\"><failure>\n" +
                        "</failure></testcase></testsuite>'>" + resultFileName
        );
        j.addPublisher(JUnitPublisher.class).testResults.set(resultFileName);
        j.save();
        buildUnstableJob(j);

        v.open();

        assertTrue(unstableJobsPortlet.hasJob(j.name));

        unstableJobsPortlet.openJob(j.name);

        assertThat(driver, hasContent("Project " + j.name));
        assertTrue(getCurrentUrl().contains(j.name));
    }
}
