package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.junit.Assert.assertEquals;

import jakarta.inject.Inject;
import java.util.List;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.BuildStatisticsPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.BuildStatisticsPortlet.JobType;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.LatestBuildsPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.UnstableJobsPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.controls.JobFiltersArea;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Node;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Test;
import org.openqa.selenium.WebElement;

@WithPlugins("dashboard-view")
public class DashboardViewPluginTest extends AbstractJobRelatedTest {

    @Test
    public void unstableJobsPortlet_showOnlyFailedJobs() {
        DashboardView v = createDashboardView();
        UnstableJobsPortlet unstableJobsPortlet = v.addBottomPortlet(UnstableJobsPortlet.class);
        unstableJobsPortlet.setShowOnlyFailedJobs(true);
        v.save();

        FreeStyleJob unstableJob = createUnstableFreeStyleJob();
        buildUnstableJob(unstableJob);
        assertJobInUnstableJobsPortlet(unstableJobsPortlet, unstableJob.name, false);

        FreeStyleJob failingJob = createFailingFreeStyleJob();
        buildFailingJob(failingJob);
        assertJobInUnstableJobsPortlet(unstableJobsPortlet, failingJob.name, true);
    }

    @Test
    public void unstableJobsPortlet_success() {
        DashboardView v = createDashboardView();
        UnstableJobsPortlet unstableJobsPortlet = v.addBottomPortlet(UnstableJobsPortlet.class);
        unstableJobsPortlet.setShowOnlyFailedJobs(true);
        v.save();

        FreeStyleJob successfulJob = createFreeStyleJob();
        buildSuccessfulJob(successfulJob);
        assertJobInUnstableJobsPortlet(unstableJobsPortlet, successfulJob.name, false);
    }

    /**
     * Asserts, if the given portlet contains the given job and correctly links to it.
     *
     * @param portlet       that should or shouldn't contain the given job.
     * @param jobName       that should or shouldn't be contained.
     * @param shouldContain whether the portlet should or shouldn't contain the job.
     * @throws AssertionError If shouldContain is true and the Portlet doesn't contain the job.
     *                        Or if shouldContain is false and the Portlet does contain the job.
     */
    private void assertJobInUnstableJobsPortlet(UnstableJobsPortlet portlet, String jobName, boolean shouldContain)
            throws AssertionError {
        portlet.getPage().open();
        assertThat(portlet.hasJob(jobName), is(shouldContain));

        if (shouldContain) {
            portlet.openJob(jobName);

            assertThat(driver, hasContent(jobName));
            assertThat(getCurrentUrl().contains(jobName), is(true));
        }
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
    public void latestsBuildsPortlet_onlyLatest() {
        DashboardView v = createDashboardView();
        LatestBuildsPortlet latestBuilds = v.addBottomPortlet(LatestBuildsPortlet.class);
        v.save();

        FreeStyleJob job = createFreeStyleJob();

        for (int i = 0; i <= LatestBuildsPortlet.NUMBER_OF_BUILDS + 1; i++) {
            buildSuccessfulJob(job);
        }

        v.open();
        assertThat(latestBuilds.hasBuild(1), is(false));
    }

    @Test
    public void configureDashboardNameAndDescription() {
        final String name = "MyDashboard";
        final String description = "My Description";

        DashboardView v = jenkins.views.create(DashboardView.class, name);
        v.configure(() -> {
            v.mainArea.setName(name);
            v.mainArea.setDescription(description);
        });

        createFreeStyleJob();
        v.open();

        final String url = getCurrentUrl();
        assertThat(url, endsWith(name + "/"));

        final List<String> breadCrumbs = v.breadCrumbs.getBreadCrumbs();
        assertThat(breadCrumbs, hasSize(2));
        final String nameCrumb = breadCrumbs.get(breadCrumbs.size() - 1);
        assertThat(nameCrumb, is(name));

        assertThat(v.mainPanel.getTabName(), equalToIgnoringCase(name));
        assertThat(v.mainPanel.getDescription(), is(description));
    }

    @Inject
    SlaveController slave1;

    @Test
    public void filterByRegex() {
        jenkins.jobs.create(FreeStyleJob.class, "a");
        jenkins.jobs.create(FreeStyleJob.class, "aa");
        jenkins.jobs.create(FreeStyleJob.class, "b");

        final DashboardView view = createDashboardView();
        view.configure(() -> view.dashboardPortlets.checkIncludeStdJobList(true));
        view.open();

        final List<String> jobIDs = view.projectStatus.getJobIDs();
        assertEquals(List.of("a", "aa", "b"), jobIDs);

        view.configure(() -> view.jobFilters.setIncludeRegex("a*"));
        view.open();

        final List<String> jobIDsFiltered = view.projectStatus.getJobIDs();
        assertEquals(List.of("a", "aa"), jobIDsFiltered);
    }

    @Test
    public void configureDashboardFilterBuildExecutors() throws Exception {
        final boolean filterBuildExecutors = true;
        Node s = slave1.install(jenkins).get();
        s.configure();
        s.setLabels("test");
        s.save();

        FreeStyleJob job = jenkins.jobs.create();

        DashboardView v = createDashboardView();
        v.configure(() -> {
            v.mainArea.setFilterBuildExecutors(filterBuildExecutors);
        });
        v.open();

        final List<WebElement> headers = v.buildExecutorStatus.getHeaders();
        assertThat(headers.size(), is(2));

        job.configure(() -> {
            job.setLabelExpression("test");
        });
        v.open();

        final List<WebElement> headers2 = v.buildExecutorStatus.getHeaders();
        // If only one node, the title header is not shown.
        assertThat(headers2.size(), is(0));
    }

    @Test
    public void configureDashboardFilterOnlyActivatedJobs() {
        DashboardView v = createDashboardView();
        BuildStatisticsPortlet stats = v.addBottomPortlet(BuildStatisticsPortlet.class);
        v.configure(() -> {
            v.jobFilters.setStatusFilter(JobFiltersArea.StatusFilter.ENABLED);
        });

        final FreeStyleJob active = createFreeStyleJob();
        final FreeStyleJob disabled = createFreeStyleJob();

        buildSuccessfulJob(active);
        buildSuccessfulJob(disabled);

        v.open();
        assertThat(stats.getNumberOfBuilds(JobType.TOTAL), is(2));
        assertThat(stats.getNumberOfBuilds(JobType.DISABLED), is(0));

        disabled.configure(disabled::disable);

        v.open();
        assertThat(stats.getNumberOfBuilds(JobType.TOTAL), is(1));
        assertThat(stats.getNumberOfBuilds(JobType.DISABLED), is(0));
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
