package plugins;

import java.util.List;
import java.util.Map;

import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisCollectorAction;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisCollectorColumn;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisCollectorSettings;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisGraphConfigurationView;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisPlugin;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.WarningsPerProjectPortlet;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisAction;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisConfigurator;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckStyleFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindBugsFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixAuthorizationStrategy;
import org.jenkinsci.test.acceptance.plugins.mock_security_realm.MockSecurityRealm;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.tasks.TasksFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsBuildSettings;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.Matchers.*;
import static org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisPlugin.*;
import static org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView.*;

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
     * Builds a freestyle job. Verifies that afterwards a trend graph exists for each of the participating plug-ins.
     * Finally, the collector trend graph is verified that contains 6 relative links to the
     * plug-in results (one for each priority and build).
     */
    @Test
    @Issue("JENKINS-30304") @Since("1.640")
    public void should_have_clickable_trend_details() {
        FreeStyleJob job = createFreeStyleJob();
        buildJobAndWait(job);
        buildSuccessfulJob(job);
        job.open();

        AnalysisAction action = createProjectAction(job);

        List<WebElement> graphLinks = job.all(By.linkText("Enlarge"));
        assertThat(graphLinks.size(), is(8));

        // Last link is the summary
        graphLinks.get(graphLinks.size() - 1).click();
        assertThatProjectPageTrendIsCorrect(job, action, "../../");
    }

    /**
     * Verifies that no other trend graphs are shown if configured in the graph configuration screen per user.
     */
    @Test @Issue("JENKINS-30270")
    public void should_deactivate_all_other_trend_graphs() {
        FreeStyleJob job = createFreeStyleJob();

        buildSuccessfulJob(job);
        buildSuccessfulJob(job);

        job.open();
        elasticSleep(500);

        assertThatNumberOfGraphsIs(job, 48);

        AnalysisCollectorAction action = new AnalysisCollectorAction(job);
        AnalysisGraphConfigurationView view = action.configureTrendGraphForUser();

        deactivateOtherTrendGraphs(view, true);
        // Give some time to JS to work
        elasticSleep(500);

        assertThatNumberOfGraphsIs(job, 6);

        deactivateOtherTrendGraphs(view, false);
        // Give some time to JS to work
        elasticSleep(500);

        assertThatNumberOfGraphsIs(job, 48);
    }

    private void deactivateOtherTrendGraphs(final AnalysisGraphConfigurationView view, final boolean shouldDisable) {
        view.open();
        view.deactiveOtherTrendGraphs(shouldDisable);
        view.save();
    }

    private void assertThatNumberOfGraphsIs(final FreeStyleJob job, final int expectedCount) {
        Map<String, Integer> trends = job.getTrendGraphContent(".*");
        assertThat(trends.size(), is(expectedCount));
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

        AnalysisCollectorSettings analysis = job.addPublisher(AnalysisCollectorSettings.class);
        AnalysisConfigurator<AnalysisCollectorSettings> configurator = new AnalysisConfigurator<AnalysisCollectorSettings>() {
            @Override
            public void configure(AnalysisCollectorSettings settings) {
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

    @Test
    @WithPlugins({"workflow-aggregator", "mock-security-realm", "matrix-auth", "groovy-postbuild"})
    public void should_compute_annotations_on_workflow() {
        // execute as admin so we don't have problems with script-security in the future
        final String ADMIN = "admin";
        final GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.open();
        {
            MockSecurityRealm realm = security.useRealm(MockSecurityRealm.class);
            realm.configure(ADMIN);
            MatrixAuthorizationStrategy mas = security.useAuthorizationStrategy(MatrixAuthorizationStrategy.class);
            mas.addUser(ADMIN).admin();
        }
        security.save();
        jenkins.login().doLogin(ADMIN);

        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(
            "node {\n" +
               job.copyResourceStep(ANALYSIS_COLLECTOR_PLUGIN_RESOURCES + "/checkstyle-result.xml") +
               job.copyResourceStep(ANALYSIS_COLLECTOR_PLUGIN_RESOURCES +"/findbugs.xml") +
            "  step([$class: 'FindBugsPublisher', pattern: '**/findbugs.xml'])\n" +
            "  step([$class: 'CheckStylePublisher'])\n" +
            "  step([$class: 'AnalysisPublisher'])\n" +
            "}");
        job.save();
        final Build build = job.startBuild();
        build.shouldSucceed();

        assertThat(build, hasAction("Static Analysis Warnings"));

        AnalysisCollectorAction action = new AnalysisCollectorAction(build);
        action.open();

        assertThat(action.getNumberOfWarnings(), is(FINDBUGS_ALL + CHECKSTYLE_ALL));
        assertThat(action.getNumberOfNewWarnings(), is(FINDBUGS_ALL + CHECKSTYLE_ALL));
    }

    private AnalysisCollectorAction deselectPluginAndBuild(AnalysisPlugin plugin, Job job) {
        job.configure();
        AnalysisCollectorSettings publisher = job.getPublisher(AnalysisCollectorSettings.class);
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
            job.addPublisher(AnalysisCollectorSettings.class);
        }
        job.save();
        return job;
    }

    private void addAndConfigureWarningsPublisher(final FreeStyleJob job) {
        WarningsBuildSettings warningsSettings = job.addPublisher(WarningsBuildSettings.class);
        AnalysisConfigurator<WarningsBuildSettings> warningsConfigurator = new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceScanner("Java Compiler (javac)", "**/*");
                settings.addWorkspaceScanner("JavaDoc Tool", "**/*");
                settings.addWorkspaceScanner("MSBuild", "**/*");
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
