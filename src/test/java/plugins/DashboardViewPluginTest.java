package plugins;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.BuildStatisticsPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.BuildStatisticsPortlet.JobType;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.controls.ColumnsArea;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.controls.JobFiltersArea;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Node;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Ignore;
import org.junit.Test;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
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


        assertThat(v.mainPanel.getTabName(), is(name));
        assertThat(v.mainPanel.getDescription(), is(description));
    }

    @Inject
    SlaveController slave1;

    @Test
    @Ignore("Test criteria still missing")
    public void filterByRegex() {
        jenkins.jobs.create(FreeStyleJob.class, "a");
        jenkins.jobs.create(FreeStyleJob.class, "aa");
        jenkins.jobs.create(FreeStyleJob.class, "b");

        final DashboardView view = createDashboardView();
        view.configure(() -> view.dashboardPortlets.checkIncludeStdJobList(true));
        view.open();

        final List<String> jobIDs = view.projectStatus.getJobIDs();
        assertEquals(Arrays.asList("a", "aa", "b"), jobIDs);

        view.configure(() -> view.jobFilters.setIncludeRegex("a*"));
        view.open();

        final List<String> jobIDsFiltered = view.projectStatus.getJobIDs();
        assertEquals(Arrays.asList("a", "aa"), jobIDsFiltered);
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

        final List<String> headers = v.buildExecutorStatus.getHeaders();
        final List<String> executors = v.buildExecutorStatus.getExecutors();
        assertThat(headers.size(), is(2));
        assertThat(executors.size(), greaterThan(1));

        job.configure(() -> {
            job.setLabelExpression("test");
        });
        v.open();

        final List<String> headers2 = v.buildExecutorStatus.getHeaders();
        final List<String> executors2 = v.buildExecutorStatus.getExecutors();
        // If only one node, the title header is not shown.
        assertThat(headers2.size(), is(0));
        assertThat(executors2.size(), is(1));
    }

    @Test
    public void changeColumns() {

        DashboardView v = createDashboardView();
        v.configure(() -> {

            v.columnsArea.removeAll();
            v.columnsArea.add(ColumnsArea.Column.NAME);
            v.columnsArea.add(ColumnsArea.Column.LAST_FAILURE);
            v.dashboardPortlets.checkIncludeStdJobList(true);
        });

        createFreeStyleJob();
        v.open();

        final List<String> titles = v.projectStatus.getHeaders();
        titles.remove(titles.size() - 1); // last is not a name

        assertThat(titles, hasSize(2));

        assertThat(titles.get(0), containsString(ColumnsArea.Column.NAME.getText()));
        assertThat(titles.get(1), containsString(ColumnsArea.Column.LAST_FAILURE.getText()));
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

    @Test
    @Ignore("The statistics portlet shows only one job in total (the disabled one). it is shown as successful (1) but disabled(0)." +
            "I found no way to get a disabled number other than zero when status filter is set to disabled. ")
    public void configureDashboardFilterOnlyDisabledJobs() {

        DashboardView v = createDashboardView();
        BuildStatisticsPortlet stats = v.addBottomPortlet(BuildStatisticsPortlet.class);
        v.configure(() -> {
            v.jobFilters.setStatusFilter(JobFiltersArea.StatusFilter.DISABLED);
        });

        final FreeStyleJob active = createFreeStyleJob();
        final FreeStyleJob disabled = createFreeStyleJob();

        buildSuccessfulJob(active);
        buildSuccessfulJob(disabled);
        disabled.configure(disabled::disable);

        v.open();
        assertThat(stats.getNumberOfBuilds(JobType.TOTAL), is(1));
        // When run the number of disabled jobs is zero.
        assertThat(stats.getNumberOfBuilds(JobType.DISABLED), is(1));
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
