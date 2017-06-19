package plugins;

import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.*;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.BuildStatisticsPortlet.JobType;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.controls.ColumnsArea;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.controls.JobFiltersArea;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.LatestBuildsPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.TestStatisticsChartPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.UnstableJobsPortlet;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JUnitPublisher;
import org.jenkinsci.test.acceptance.po.Node;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openqa.selenium.NoSuchElementException;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.net.MalformedURLException;

import static org.hamcrest.Matchers.*;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@WithPlugins("dashboard-view")
public class DashboardViewPluginTest extends AbstractJobRelatedTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
    public void jobsGridPortlet_fillColumnsFirst() throws MalformedURLException {
        createFreeStyleJob();
        createFreeStyleJob();
        createFreeStyleJob();
        createFreeStyleJob();

        DashboardView v = createDashboardView();
        JobsGridPortlet jobsGridPortlet = v.addBottomPortlet(JobsGridPortlet.class);
        jobsGridPortlet.setNumberOfColumns(3);
        jobsGridPortlet.setFillColumnFirst(true);
        v.save();

        assertThat(jobsGridPortlet.getJob(1, 3), nullValue());
        assertThat(jobsGridPortlet.getJob(2, 2), notNullValue());
    }

    @Test
    public void jobsGridPortlet_notFillColumnsFirst() throws MalformedURLException {
        createFreeStyleJob();
        createFreeStyleJob();
        createFreeStyleJob();
        createFreeStyleJob();

        DashboardView v = createDashboardView();
        JobsGridPortlet jobsGridPortlet = v.addBottomPortlet(JobsGridPortlet.class);
        jobsGridPortlet.setNumberOfColumns(3);
        jobsGridPortlet.setFillColumnFirst(false);
        v.save();
        assertThat(jobsGridPortlet.getJob(1, 3), notNullValue());
        assertThat(jobsGridPortlet.getJob(2, 2), nullValue());
    }

    @Test
    public void jobsGridPortlet_numberOfColumns() throws MalformedURLException {
        // One job is required for the portlet to be displayed
        createFreeStyleJob();

        DashboardView v = createDashboardView();
        JobsGridPortlet jobsGridPortlet = v.addBottomPortlet(JobsGridPortlet.class);

        jobsGridPortlet.setNumberOfColumns(10);
        v.save();
        assertThat(jobsGridPortlet.getJob(1, 10), nullValue());
    }

    @Test
    public void jobsGridPortlet_invalidNumberOfColumn() throws MalformedURLException {
        // One job is required for the portlet to be displayed
        createFreeStyleJob();

        DashboardView v = createDashboardView();
        JobsGridPortlet jobsGridPortlet = v.addBottomPortlet(JobsGridPortlet.class);

        jobsGridPortlet.setNumberOfColumns(2);
        v.save();

        expectedException.expect(NoSuchElementException.class);
        jobsGridPortlet.getJob(1, 3);
    }

    @Test
    public void unstableJobsPortlet_notShowOnlyFailedJobs() {
        DashboardView v = createDashboardView();
        UnstableJobsPortlet unstableJobsPortlet = v.addBottomPortlet(UnstableJobsPortlet.class);
        unstableJobsPortlet.setShowOnlyFailedJobs(false);
        v.save();

        FreeStyleJob unstableJob = createUnstableFreeStyleJob();
        buildUnstableJob(unstableJob);
        assertJobInUnstableJobsPortlet(unstableJobsPortlet, unstableJob.name, true);

        FreeStyleJob failingJob = createFailingFreeStyleJob();
        buildFailingJob(failingJob);
        assertJobInUnstableJobsPortlet(unstableJobsPortlet, failingJob.name, true);
    }

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
    private void assertJobInUnstableJobsPortlet(UnstableJobsPortlet portlet, String jobName, boolean shouldContain) throws AssertionError {
        portlet.getPage().open();
        assertThat(portlet.hasJob(jobName), is(shouldContain));

        if (shouldContain) {
            portlet.openJob(jobName);

            assertThat(driver, hasContent("Project " + jobName));
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

    @Test
    public void testStatisticsChart_success() throws IOException {
        DashboardView v = createDashboardView();
        TestStatisticsChartPortlet chart = v.addBottomPortlet(TestStatisticsChartPortlet.class);
        v.save();

        FreeStyleJob successJob = createFreeStyleJob(job -> {
            String resultFileName = "status.xml";
            job.addShellStep(
                "echo '<testsuite><testcase classname=\"\">" +
                    "</testcase></testsuite>'>" + resultFileName
            );
            job.addPublisher(JUnitPublisher.class).testResults.set(resultFileName);
        });

        buildSuccessfulJob(successJob);

        v.open();

        Resource testImageResource = resource("/dashboardview_plugin/test_statistics_chart/success.png");
        BufferedImage testImage = ImageIO.read(testImageResource.asFile());

        checkImages(chart.getImage(), testImage);
    }

    @Test
    public void testStatisticsChart_failure() throws IOException {
        DashboardView v = createDashboardView();
        TestStatisticsChartPortlet chart = v.addBottomPortlet(TestStatisticsChartPortlet.class);
        v.save();

        FreeStyleJob unstableFreeStyleJob = createUnstableFreeStyleJob();

        buildUnstableJob(unstableFreeStyleJob);

        v.open();

        Resource testImageResource = resource("/dashboardview_plugin/test_statistics_chart/failure.png");
        BufferedImage testImage = ImageIO.read(testImageResource.asFile());

        checkImages(chart.getImage(), testImage);
    }

    @Test
    public void testStatisticsChart_failureAndSuccess() throws IOException {
        DashboardView v = createDashboardView();
        TestStatisticsChartPortlet chart = v.addBottomPortlet(TestStatisticsChartPortlet.class);
        v.save();

        FreeStyleJob unstableFreeStyleJob = createUnstableFreeStyleJob();
        FreeStyleJob successJob = createFreeStyleJob(job -> {
            String resultFileName = "status.xml";
            job.addShellStep(
                "echo '<testsuite><testcase classname=\"\">" +
                    "</testcase></testsuite>'>" + resultFileName
            );
            job.addPublisher(JUnitPublisher.class).testResults.set(resultFileName);
        });

        buildSuccessfulJob(successJob);
        buildUnstableJob(unstableFreeStyleJob);

        v.open();

        Resource testImageResource = resource("/dashboardview_plugin/test_statistics_chart/success_failure.png");
        BufferedImage testImage = ImageIO.read(testImageResource.asFile());

        checkImages(chart.getImage(), testImage);
    }

    @Test
    public void testPortletPositioning_topPortlets(){
        DashboardView v = createDashboardView();
        v.addTopPortlet(TestStatisticsChartPortlet.class);
        v.save();

        createFreeStyleJob();

        assertThat(v.getPortletInTopTable(TestStatisticsChartPortlet.TEST_STATISTICS_CHART), notNullValue());
        assertThat(v.getPortletInLeftTable(TestStatisticsChartPortlet.TEST_STATISTICS_CHART), nullValue());
        assertThat(v.getPortletInRightTable(TestStatisticsChartPortlet.TEST_STATISTICS_CHART), nullValue());
        assertThat(v.getPortletInBottomTable(TestStatisticsChartPortlet.TEST_STATISTICS_CHART), nullValue());
    }

    @Test
    public void testPortletPositioning_leftPortlets(){
        DashboardView v = createDashboardView();
        v.addLeftPortlet(TestStatisticsChartPortlet.class);
        v.addRightPortlet(BuildStatisticsPortlet.class);
        v.save();

        createFreeStyleJob();

        assertThat(v.getPortletInTopTable(TestStatisticsChartPortlet.TEST_STATISTICS_CHART), nullValue());
        assertThat(v.getPortletInLeftTable(TestStatisticsChartPortlet.TEST_STATISTICS_CHART), notNullValue());
        assertThat(v.getPortletInRightTable(TestStatisticsChartPortlet.TEST_STATISTICS_CHART), nullValue());
        assertThat(v.getPortletInBottomTable(TestStatisticsChartPortlet.TEST_STATISTICS_CHART), nullValue());
    }

    @Test
    public void testPortletPositioning_rightPortlets(){
        DashboardView v = createDashboardView();
        v.addRightPortlet(TestStatisticsChartPortlet.class);
        v.addLeftPortlet(BuildStatisticsPortlet.class);
        v.save();

        createFreeStyleJob();

        assertThat(v.getPortletInTopTable(TestStatisticsChartPortlet.TEST_STATISTICS_CHART), nullValue());
        assertThat(v.getPortletInLeftTable(TestStatisticsChartPortlet.TEST_STATISTICS_CHART), nullValue());
        assertThat(v.getPortletInRightTable(TestStatisticsChartPortlet.TEST_STATISTICS_CHART), notNullValue());
        assertThat(v.getPortletInBottomTable(TestStatisticsChartPortlet.TEST_STATISTICS_CHART), nullValue());
    }

    @Test
    public void testPortletPositioning_bottomPortlets(){
        DashboardView v = createDashboardView();
        v.addBottomPortlet(TestStatisticsChartPortlet.class);
        v.save();

        createFreeStyleJob();

        assertThat(v.getPortletInTopTable(TestStatisticsChartPortlet.TEST_STATISTICS_CHART), nullValue());
        assertThat(v.getPortletInLeftTable(TestStatisticsChartPortlet.TEST_STATISTICS_CHART), nullValue());
        assertThat(v.getPortletInRightTable(TestStatisticsChartPortlet.TEST_STATISTICS_CHART), nullValue());
        assertThat(v.getPortletInBottomTable(TestStatisticsChartPortlet.TEST_STATISTICS_CHART), notNullValue());
    }

    /**
     * Checks an image pixel by pixel if it's the same as the other image.
     *
     * @param actualImage image to test
     * @param testImage   image to test against
     */
    private void checkImages(BufferedImage actualImage, BufferedImage testImage) {
        assertThat(actualImage.getHeight(), is(testImage.getHeight()));
        assertThat(actualImage.getWidth(), is(testImage.getWidth()));

        for (int x = 0; x < testImage.getWidth(); x++) {
            for (int y = 0; y < testImage.getHeight(); y++) {
                assertThat(actualImage.getRGB(x, y), is(testImage.getRGB(x, y)));
            }
        }
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
