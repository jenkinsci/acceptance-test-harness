package plugins;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.BuildStatisticsPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.BuildStatisticsPortlet.JobType;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.controls.ColumnsArea;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.controls.JobFiltersArea;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

import static org.hamcrest.Matchers.*;
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

    @Test
    @Ignore
    public void configureDashboardFilterBuildExecutors() {
        final boolean filterBuildExecutors = true;

        DashboardView v = jenkins.views.create(DashboardView.class, "Dashboard");
        v.configure();
        {
            v.mainArea.setFilterBuildExecutors(filterBuildExecutors);
        }
        v.save();
        v.open();

        final By executors = By.xpath("//div[@id=\"executors\"]/div[@class=\"row pane-content\"]/table");
        final List<WebElement> elements = find(executors).findElements(By.xpath("/tbody/tr"));
        assertThat(elements.size(), is(0));
    }

    @Test
    public void changeColumns() {

        DashboardView v = createDashboardView();
        v.configure();
        {
            v.columnsArea.removeAll();
            v.columnsArea.add(ColumnsArea.Column.NAME);
            v.columnsArea.add(ColumnsArea.Column.LAST_FAILURE);
            v.dashboardPortlets.checkIncludeStdJobList(true);
        }
        v.save();
        createFreeStyleJob();
        v.open();

        final By header = By.xpath("//table[@id=\"projectstatus\"]/tbody/tr[@class=\"header\"]");
        final List<WebElement> titles = find(header).findElements(By.xpath(".//a"));
        titles.remove(titles.size() - 1); // last is not a name

        assertThat(titles.size(), is(2));
        final String[] headers = titles.stream()
                .map(WebElement::getText)
                .toArray(String[]::new);

        assertThat(headers[0], containsString(ColumnsArea.Column.NAME.getText()));
        assertThat(headers[1], containsString(ColumnsArea.Column.LAST_FAILURE.getText()));
    }

    @Test
    @Ignore("There needs to be a better way to read the stats on the left side.")
    public void configureDashboardFilterOnlyActivatedJobs() {

        DashboardView v = createDashboardView();
        BuildStatisticsPortlet stats = v.addBottomPortlet(BuildStatisticsPortlet.class);
        v.configure();
        {
            v.jobFilters.setStatusFilter(JobFiltersArea.StatusFilter.ENABLED);
        }
        v.save();

        final FreeStyleJob active = createFreeStyleJob();
        final FreeStyleJob disabled = createFreeStyleJob();

        buildSuccessfulJob(active);
        buildSuccessfulJob(disabled);

        disabled.configure(disabled::disable);

        v.open();
        final int numberOfBuilds = stats.getNumberOfBuilds(JobType.TOTAL);
        assertThat(numberOfBuilds, is(1));
        final int numberOfDisabled = stats.getNumberOfBuilds(JobType.DISABLED);
        assertThat(numberOfDisabled, is(0));
    }

    @Test
    @Ignore("")
    public void configureDashboardFilterOnlyDisabledJobs() {

        DashboardView v = createDashboardView();
        BuildStatisticsPortlet stats = v.addBottomPortlet(BuildStatisticsPortlet.class);
        v.configure();
        {
            v.jobFilters.setStatusFilter(JobFiltersArea.StatusFilter.DISABLED);
        }
        v.save();

        final FreeStyleJob active = createFreeStyleJob();
        final FreeStyleJob disabled = createFreeStyleJob();

        buildSuccessfulJob(active);
        buildSuccessfulJob(disabled);

        disabled.configure(disabled::disable);

        v.open();
        final int numberOfBuilds = stats.getNumberOfBuilds(JobType.TOTAL);
        assertThat(numberOfBuilds, is(1));
        final int numberOfDisabled = stats.getNumberOfBuilds(JobType.DISABLED);
        assertThat(numberOfDisabled, is(1));
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
