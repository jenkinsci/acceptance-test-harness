package plugins;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisCollectorAction;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisCollectorColumn;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisCollectorFreestyleBuildSettings;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisPlugin;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.WarningsPerProjectPortlet;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisConfigurator;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckStyleFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindBugsFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.tasks.TasksFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsBuildSettings;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.ListView;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;
import static org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisPlugin.*;
import static org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView.*;
import static org.jenkinsci.test.acceptance.po.PageObject.createRandomName;
import static org.junit.Assert.*;

/**
 * Acceptance tests for the Static Code Analysis Collector (analysis-collector) plug-in.
 *
 * @author Michael Prankl
 * @author Ullrich Hafner
 */
@WithPlugins({"analysis-collector", "checkstyle", "pmd", "findbugs", "tasks", "warnings"})
public class AnalysisCollectorPluginTest extends AbstractAnalysisTest<AnalysisCollectorAction> {
    private static final String ANALYSIS_COLLECTOR_PLUGIN_RESOURCES = "/analysis_collector_plugin";
    private static final String XPATH_LISTVIEW_WARNING_TD = "//table[@id='projectstatus']/tbody/tr[2]/td[last()-1]";

    private static final int CHECKSTYLE_ALL = 776;
    private static final int FINDBUGS_ALL = 6;
    private static final int PMD_ALL = 9;
    private static final int TASKS_ALL = 8;
    private static final int WARNINGS_ALL = 154;
    private static final int TOTAL = CHECKSTYLE_ALL + FINDBUGS_ALL + PMD_ALL + TASKS_ALL + WARNINGS_ALL;

    private static final int CHECKSTYLE_HIGH = CHECKSTYLE_ALL;
    private static final int FINDBUGS_HIGH = 2;
    private static final int PMD_HIGH = 0;
    private static final int TASKS_HIGH = 2;
    private static final int WARNINGS_HIGH = 3;

    private static final int CHECKSTYLE_LOW = 0;
    private static final int FINDBUGS_LOW = 0;
    private static final int PMD_LOW = 6;
    private static final int TASKS_LOW = 2;
    private static final int WARNINGS_LOW = 0;

    @Override
    protected AnalysisCollectorAction createProjectAction(final FreeStyleJob job) {
        return new AnalysisCollectorAction(job);
    }

    @Override
    protected AnalysisCollectorAction createResultAction(final Build build) {
        return new AnalysisCollectorAction(build);
    }

    @Override
    protected FreeStyleJob createFreeStyleJob() {
        return createJob(ANALYSIS_COLLECTOR_PLUGIN_RESOURCES, true);
    }

    @Override
    protected int getNumberOfWarnings() {
        return TOTAL;
    }

    /**
     * Verifies that the plugin correctly collects and aggregates the warnings of all participating plugins.
     */
    @Test
    public void should_collect_warnings_of_all_tools() {
        FreeStyleJob job = createFreeStyleJob();

        Build build = buildSuccessfulJob(job);

        assertThat(job, hasAction("Static Analysis Warnings"));
        assertThat(build, hasAction("Static Analysis Warnings"));

        AnalysisCollectorAction action = new AnalysisCollectorAction(build);
        action.open();

        assertThat(action.getNumberOfWarnings(), is(TOTAL));
        assertThat(action.getNumberOfNewWarnings(), is(TOTAL));

        int high = CHECKSTYLE_HIGH + FINDBUGS_HIGH + PMD_HIGH + TASKS_HIGH + WARNINGS_HIGH;
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(high));

        int low = CHECKSTYLE_LOW + FINDBUGS_LOW + PMD_LOW + TASKS_LOW + WARNINGS_LOW;
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(low));

        int normal = TOTAL - low - high;
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(normal));
    }

    /**
     * Verifies that the plugin correctly identifies new open tasks. The first build contains 4 open tasks. The second
     * builds adds another 4 open tasks, summing up to a total of 8 open tasks. The second build should then contain 4
     * new warnings.
     */
    @Test
    public void should_compute_all_new_open_tasks() {
        FreeStyleJob job = createJob(ANALYSIS_COLLECTOR_PLUGIN_RESOURCES + "/Tasks.java", true);
        buildSuccessfulJob(job);

        job.configure();
        job.copyResource(ANALYSIS_COLLECTOR_PLUGIN_RESOURCES + "/Tasks2.java");
        job.save();

        Build build = buildSuccessfulJob(job);

        AnalysisCollectorAction action = new AnalysisCollectorAction(build);
        action.open();

        assertThat(action.getNumberOfWarnings(), is(8));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(2));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(4));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(2));
        assertThat(action.getNumberOfNewWarnings(), is(4));
    }

    /**
     * Verifies that a build should become status unstable when a warning threshold is exceeded.
     */
    @Test
    public void should_set_build_result_to_unstable() {
        FreeStyleJob job = jenkins.jobs.create();

        job.configure();
        job.copyResource(ANALYSIS_COLLECTOR_PLUGIN_RESOURCES + "/findbugs.xml");
        job.addPublisher(FindBugsFreestyleSettings.class);

        AnalysisCollectorFreestyleBuildSettings analysis = job.addPublisher(AnalysisCollectorFreestyleBuildSettings.class);
        AnalysisConfigurator<AnalysisCollectorFreestyleBuildSettings> configurator = new AnalysisConfigurator<AnalysisCollectorFreestyleBuildSettings>() {
            @Override
            public void configure(AnalysisCollectorFreestyleBuildSettings settings) {
                settings.setBuildUnstableTotalAll("5");
            }
        };
        configurator.configure(analysis);
        job.save();

        buildUnstableJob(job);
    }

    /**
     * Verifies that the plugin only collects warnings of the checked plugins. The test starts with a build that
     * collects the warnings of all available tools. Then subsequently a new build is started with one tool removed
     * until no tool is checked anymore.
     */
    @Test
    public void should_collect_warnings_of_selected_tools_only() {
        FreeStyleJob job = createFreeStyleJob();

        int remaining = TOTAL;

        AnalysisCollectorAction action = deselectPluginAndBuild(CHECKSTYLE, job);

        remaining -= CHECKSTYLE_ALL;
        assertThat(action.getNumberOfWarnings(), is(remaining));

        action = deselectPluginAndBuild(FINDBUGS, job);
        remaining -= FINDBUGS_ALL;
        assertThat(action.getNumberOfWarnings(), is(remaining));

        action = deselectPluginAndBuild(PMD, job);
        remaining -= PMD_ALL;
        assertThat(action.getNumberOfWarnings(), is(remaining));

        action = deselectPluginAndBuild(TASKS, job);
        remaining -= TASKS_ALL;
        assertThat(action.getNumberOfWarnings(), is(remaining));

        action = deselectPluginAndBuild(WARNINGS, job);
        assertThat(action.getNumberOfWarnings(), is(0));
    }

    /**
     * Verifies that the plugin shows on the job summary page a section with the individual results for each aggregated
     * warning type. Each result is shown on a separate line (HTML item) with the plugin icon and the number of warnings
     * per tool.
     */
    @Test
    public void should_show_job_summary_with_warnings_per_tool() {
        FreeStyleJob job = createFreeStyleJob();
        buildSuccessfulJob(job);

        assertThat(job,
                allOf(
                        hasAnalysisWarningsFor(CHECKSTYLE),
                        hasAnalysisWarningsFor(PMD),
                        hasAnalysisWarningsFor(FINDBUGS),
                        hasAnalysisWarningsFor(TASKS),
                        hasAnalysisWarningsFor(WARNINGS)
                )
        );
        assertThat(job, not(hasAnalysisWarningsFor(DRY)));
    }

    /**
     * Sets up a list view with a warnings column. Builds a job and checks if the column shows the correct number of
     * warnings and provides a direct link to the actual warning results. Also verifies that the mouse-over tooltip will
     * show the correct number of warnings per checked plugin.
     */
    @Test
    public void should_set_warnings_count_in_list_view_column() {
        FreeStyleJob job = createFreeStyleJob();
        buildSuccessfulJob(job);

        ListView view = jenkins.views.create(ListView.class, createRandomName());
        view.configure();
        view.matchAllJobs();
        view.addColumn(AnalysisCollectorColumn.class);
        view.save();

        view.open();
        WebElement warningsCell = view.find(by.xpath(XPATH_LISTVIEW_WARNING_TD));
        assertThat(warningsCell.getText(), is(String.valueOf(TOTAL)));

        String tooltip = warningsCell.getAttribute("tooltip");
        assertThat(tooltip,
                allOf(
                        containsString("<a href=\"job/" + job.name + "/checkstyle\">" + CHECKSTYLE_ALL + "</a>"),
                        containsString("<a href=\"job/" + job.name + "/findbugs\">" + FINDBUGS_ALL + "</a>"),
                        containsString("<a href=\"job/" + job.name + "/pmd\">" + PMD_ALL + "</a>"),
                        containsString("<a href=\"job/" + job.name + "/warnings\">" + WARNINGS_ALL + "</a>")
                )
        );

        view.configure();
        AnalysisCollectorColumn column = view.getColumn(AnalysisCollectorColumn.class);
        column.checkPlugin(PMD, false);
        view.save();

        view.open();
        // check that PMD warnings are not collected to total warning number and tooltip
        warningsCell = view.find(by.xpath(XPATH_LISTVIEW_WARNING_TD));
        assertThat(warningsCell.getText(), is(String.valueOf(TOTAL - PMD_ALL)));
        tooltip = warningsCell.getAttribute("tooltip");
        assertThat(tooltip, not(containsString("<a href=\"job/" + job.name + "/pmd\">" + PMD_ALL + "</a>")));
    }

    /**
     * Sets up a dashboard view with a warnings-per-project portlet. Builds a job and checks if the portlet shows the
     * correct number of warnings. Then one of the tools is deselected. The portlet should then show only the remaining
     * number of warnings.
     */
    @Test
    @WithPlugins("dashboard-view")
    public void should_aggregate_warnings_in_dashboard_portlet() {
        FreeStyleJob job = createFreeStyleJob();
        buildSuccessfulJob(job);

        DashboardView dashboard = jenkins.views.create(DashboardView.class, createRandomName());
        dashboard.configure();
        dashboard.matchAllJobs();
        WarningsPerProjectPortlet portlet = dashboard.addBottomPortlet(WarningsPerProjectPortlet.class);
        portlet.setName("My Warnings");
        portlet.hideZeroWarningsProjects(false).showImagesInTableHeader(true);
        dashboard.save();

        dashboard.open();
        assertThat(dashboard, hasWarningsFor(job, CHECKSTYLE, CHECKSTYLE_ALL));
        assertThat(dashboard, hasWarningsFor(job, PMD, PMD_ALL));
        assertThat(dashboard, hasWarningsFor(job, FINDBUGS, FINDBUGS_ALL));
        assertThat(dashboard, hasWarningsFor(job, TASKS, TASKS_ALL));
        assertThat(dashboard, hasWarningsFor(job, WARNINGS, WARNINGS_ALL));

        // uncheck Open Tasks
        dashboard.configure();
        portlet = dashboard.getBottomPortlet(WarningsPerProjectPortlet.class);
        portlet.checkCollectedPlugin(TASKS, false);
        dashboard.save();
        dashboard.open();
        assertThat(dashboard, not(hasWarningsFor(job, TASKS, TASKS_ALL)));
    }

    private AnalysisCollectorAction deselectPluginAndBuild(AnalysisPlugin plugin, Job job) {
        job.configure();
        AnalysisCollectorFreestyleBuildSettings publisher = job.getPublisher(AnalysisCollectorFreestyleBuildSettings.class);
        publisher.checkCollectedPlugin(plugin, false);
        job.save();
        Build build = buildSuccessfulJob(job);
        AnalysisCollectorAction action = new AnalysisCollectorAction(build);
        action.open();
        return action;
    }

    private FreeStyleJob createJob(final String resourceToCopy, final boolean addAnalysisPublisher) {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.copyResource(resourceToCopy);
        job.addPublisher(CheckStyleFreestyleSettings.class);
        job.addPublisher(PmdFreestyleSettings.class);
        job.addPublisher(FindBugsFreestyleSettings.class);
        addAndConfigureTasksPublisher(job);
        addAndConfigureWarningsPublisher(job);

        if (addAnalysisPublisher) {
            job.addPublisher(AnalysisCollectorFreestyleBuildSettings.class);
        }
        job.save();
        return job;
    }

    private void addAndConfigureWarningsPublisher(final FreeStyleJob job) {
        WarningsBuildSettings warningsSettings = job.addPublisher(WarningsBuildSettings.class);
        AnalysisConfigurator<WarningsBuildSettings> warningsConfigurator = new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
                settings.addWorkspaceFileScanner("JavaDoc Tool", "**/*");
                settings.addWorkspaceFileScanner("MSBuild", "**/*");
            }
        };
        warningsConfigurator.configure(warningsSettings);
    }

    private void addAndConfigureTasksPublisher(final FreeStyleJob job) {
        TasksFreestyleSettings taskScannerSettings = job.addPublisher(TasksFreestyleSettings.class);
        AnalysisConfigurator<TasksFreestyleSettings> configurator = new AnalysisConfigurator<TasksFreestyleSettings>() {
            @Override
            public void configure(TasksFreestyleSettings settings) {
                settings.setHighPriorityTags("PRIO1");
                settings.setNormalPriorityTags("PRIO2,TODO");
                settings.setLowPriorityTags("PRIO3");
            }
        };
        configurator.configure(taskScannerSettings);
    }
}
