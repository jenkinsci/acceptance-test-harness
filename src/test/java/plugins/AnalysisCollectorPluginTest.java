package plugins;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hamcrest.Description;
import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.GitContainer;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithCredentials;
import org.jenkinsci.test.acceptance.junit.WithDocker;
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
import org.jenkinsci.test.acceptance.plugins.git.GitRepo;
import org.jenkinsci.test.acceptance.plugins.nested_view.NestedView;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.tasks.TasksFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsBuildSettings;
import org.jenkinsci.test.acceptance.plugins.workflow_multibranch.GitBranchSource;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Container;
import org.jenkinsci.test.acceptance.po.Folder;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.jenkinsci.test.acceptance.po.WorkflowMultiBranchJob;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.containsString;
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
    private static final int HIGH_COUNT = CHECKSTYLE_HIGH + FINDBUGS_HIGH + PMD_HIGH + TASKS_HIGH + WARNINGS_HIGH;

    private static final int CHECKSTYLE_LOW = 0;
    private static final int FINDBUGS_LOW = 0;
    private static final int PMD_LOW = 6;
    private static final int TASKS_LOW = 2;
    private static final int WARNINGS_LOW = 0;
    private static final int LOW_COUNT = CHECKSTYLE_LOW + FINDBUGS_LOW + PMD_LOW + TASKS_LOW + WARNINGS_LOW;
    private static final int NORMAL_COUNT = TOTAL - LOW_COUNT - HIGH_COUNT;
    private static final List<AnalysisPlugin> ANALYSIS_PLUGINS = Arrays.asList(CHECKSTYLE, PMD, FINDBUGS, TASKS, WARNINGS);

    @Inject
    DockerContainerHolder<GitContainer> gitForMultiBranch;

    /**
     * Builds a freestyle job. Verifies that afterwards a trend graph exists for each of the participating plug-ins.
     * Finally, the collector trend graph is verified that contains 6 relative links to the
     * plug-in results (one for each priority and build).
     */
    @Test @Issue("JENKINS-30304") @Since("1.640")
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
        assertThatProjectPageTrendIsCorrect(job, action, "../../", getNumberOfWarnings());
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

        assertThatNumberOfGraphsIs(job, 6);

        deactivateOtherTrendGraphs(view, false);

        assertThatNumberOfGraphsIs(job, 48);
    }

    private void deactivateOtherTrendGraphs(final AnalysisGraphConfigurationView view, final boolean shouldDisable) {
        view.open();
        view.deactiveOtherTrendGraphs(shouldDisable);
        view.save();
        // Give some time to JS to work
        elasticSleep(500);
    }

    private void assertThatNumberOfGraphsIs(final FreeStyleJob job, final int expectedCount) {
        Map<String, Integer> trends = getTrendGraphContent(".*");
        assertThat(trends.size(), is(expectedCount));
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
        AnalysisConfigurator<AnalysisCollectorSettings> configurator = settings -> settings.setBuildUnstableTotalAll("5");
        configurator.accept(analysis);
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

        ListView view = jenkins.views.create(ListView.class);
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
     * Sets up a nested view that contains a dashboard view with a warnings-per-project portlet.
     * Creates a folder in this view and a multi-branch job in this folder.
     * The multi-branch job is based on a git repository with two branches (master and branch).
     * Each branch contains a Jenkinsfile and several warnings results files.
     * Builds the jobs and verifies that the portlet is correctly filled and that all links open the correct page.
     */
    @Test @Issue({"JENKINS-39950"}) @WithPlugins({"dashboard-view", "nested-view", "git", "workflow-job", "workflow-cps", "workflow-basic-steps", "workflow-durable-task-step", "workflow-multibranch"})
    @WithDocker @WithCredentials(credentialType = WithCredentials.SSH_USERNAME_PRIVATE_KEY, values = {"warnings", "/org/jenkinsci/test/acceptance/docker/fixtures/GitContainer/unsafe"})
    public void should_open_links_in_folder_dashboard_and_nested_views() {
        // Given
        NestedView nested = jenkins.getViews().create(NestedView.class);

        DashboardView dashboard = nested.getViews().create(DashboardView.class);
        dashboard.configure( () -> {
            dashboard.matchAllJobs();
            dashboard.checkRecurseIntoFolders();

            addWarningsPortlet(dashboard);
        });

        Folder folder = dashboard.jobs.create(Folder.class);
        folder.save();
        folder.open();

        String repoUrl = createGitRepositoryInDockerContainer();

        WorkflowMultiBranchJob job = folder.getJobs().create(WorkflowMultiBranchJob.class);
        GitBranchSource branchSource = job.addBranchSource(GitBranchSource.class);
        branchSource.setRemote(repoUrl);
        branchSource.setCredentials("warnings");

        job.save();
        job.waitForBranchIndexingFinished(20);

        // When
        List<String> jobs = Arrays.asList("master", "branch");
        for (String name : jobs) {
            buildBranch(job, name);
        }

        // Then
        dashboard.open();

        for (String name : jobs) {
            verifyWarningsCountInPortlet(name, dashboard);
        }

        for (AnalysisPlugin plugin : ANALYSIS_PLUGINS) {
            for (String name : jobs) {
                dashboard.open();
                findPortletLink(dashboard, name, plugin.getId()).click();
                assertThat(driver, hasContent(plugin.getName()));
            }
        }
    }

    private WorkflowJob buildBranch(WorkflowMultiBranchJob job, String branchName) {
        WorkflowJob master = job.getJob(branchName);
        master.build(1).waitUntilFinished().shouldSucceed();
        return master;
    }

    private String createGitRepositoryInDockerContainer() {
        GitRepo repo = new GitRepo();
        repo.addFilesIn(getClass().getResource(ANALYSIS_COLLECTOR_PLUGIN_RESOURCES));
        repo.commit("Initial commit in master");
        repo.createBranch("branch");

        GitContainer container = gitForMultiBranch.get();
        repo.transferToDockerContainer(container.host(), container.port());

        return container.getRepoUrl();
    }

    private WarningsPerProjectPortlet addWarningsPortlet(DashboardView dashboard) {
        WarningsPerProjectPortlet portlet = dashboard.addBottomPortlet(WarningsPerProjectPortlet.class);
        portlet.setName("My Warnings");
        portlet.hideZeroWarningsProjects(false);
        portlet.showImagesInTableHeader(true);
        return portlet;
    }

    /**
     * Sets up a dashboard view with a warnings-per-project portlet. Builds a job and checks if the portlet shows the
     * correct number of warnings. Then one of the tools is deselected. The portlet should then show only the remaining
     * number of warnings.
     */
    @Test @WithPlugins("dashboard-view")
    public void should_aggregate_warnings_in_dashboard_portlet() {
        FreeStyleJob job = createFreeStyleJob();
        buildSuccessfulJob(job);

        DashboardView dashboard = jenkins.views.create(DashboardView.class, createRandomName());
        dashboard.configure();
        dashboard.matchAllJobs();
        WarningsPerProjectPortlet portlet = addWarningsPortlet(dashboard);
        dashboard.save();

        dashboard.open();
        verifyWarningsCountInPortlet(job.name, dashboard);

        // uncheck Open Tasks
        dashboard.configure(() -> portlet.checkCollectedPlugin(TASKS, false));
        dashboard.open();

        assertThat(dashboard, not(hasWarnings(job.name, TASKS.getId(), TASKS_ALL)));
    }

    private void verifyWarningsCountInPortlet(String name, DashboardView dashboard) {
        for (AnalysisPlugin plugin : ANALYSIS_PLUGINS) {
            assertThat(dashboard, hasWarnings(name, CHECKSTYLE.getId(), CHECKSTYLE_ALL));

        }
        assertThat(dashboard, hasWarnings(name, PMD.getId(), PMD_ALL));
        assertThat(dashboard, hasWarnings(name, FINDBUGS.getId(), FINDBUGS_ALL));
        assertThat(dashboard, hasWarnings(name, TASKS.getId(), TASKS_ALL));
        assertThat(dashboard, hasWarnings(name, WARNINGS.getId(), WARNINGS_ALL));
    }

    @Test @WithPlugins("workflow-aggregator")
    public void should_compute_annotations_on_workflow() {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(
                "node {\n" +
                        copyFileToWorkspace(job, "checkstyle-result.xml") +
                        copyFileToWorkspace(job, "findbugs.xml") +
                        "  step([$class: 'FindBugsPublisher', pattern: '**/findbugs.xml'])\n" +
                        "  step([$class: 'CheckStylePublisher'])\n" +
                        "  step([$class: 'AnalysisPublisher'])\n" +
                        "}");
        job.sandbox.check();
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

    @Override
    protected AnalysisCollectorAction createProjectAction(final Job job) {
        return new AnalysisCollectorAction(job);
    }

    @Override
    protected AnalysisCollectorAction createResultAction(final Build build) {
        return new AnalysisCollectorAction(build);
    }

    @Override
    protected FreeStyleJob createFreeStyleJob(final Container owner) {
        return createJob(ANALYSIS_COLLECTOR_PLUGIN_RESOURCES, true, owner);
    }

    @Override
    protected WorkflowJob createPipeline() {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set("node {\n"
                + copyFileToWorkspace(job, "findbugs.xml")
                + "  step([$class: 'FindBugsPublisher', pattern: '**/findbugs.xml'])\n"
                + copyFileToWorkspace(job, "pmd.xml")
                + "  step([$class: 'PmdPublisher'])\n"
                + copyFileToWorkspace(job, "checkstyle-result.xml")
                + "  step([$class: 'CheckStylePublisher'])\n"
                + copyFileToWorkspace(job, "Tasks.java")
                + copyFileToWorkspace(job, "Tasks2.java")
                + "  step([$class: 'TasksPublisher', high: 'PRIO1', normal: 'PRIO2,TODO', low :'PRIO3'])\n"
                + copyFileToWorkspace(job, "warnings.txt")
                + "  step([$class: 'WarningsPublisher', "
                + "     parserConfigurations: ["
                + "             [parserName: 'Java Compiler (javac)', pattern: '**/warnings.txt'],"
                + "             [parserName: 'JavaDoc Tool', pattern: '**/warnings.txt'],"
                + "             [parserName: 'MSBuild', pattern: '**/warnings.txt']"
                + "     ]])\n"
                + "  step([$class: 'AnalysisPublisher'])\n}");
        job.sandbox.check();
        job.save();
        return job;
    }

    private String copyFileToWorkspace(final WorkflowJob job, final String fileName) {
        return job.copyResourceStep(ANALYSIS_COLLECTOR_PLUGIN_RESOURCES + "/" + fileName);
    }

    @Override
    protected int getNumberOfWarnings() {
        return TOTAL;
    }

    @Override
    protected int getNumberOfHighPriorityWarnings() {
        return HIGH_COUNT;
    }

    @Override
    protected int getNumberOfNormalPriorityWarnings() {
        return NORMAL_COUNT;
    }

    @Override
    protected int getNumberOfLowPriorityWarnings() {
        return LOW_COUNT;
    }

    private FreeStyleJob createFreeStyleJob() {
        return createFreeStyleJob(jenkins);
    }

    private FreeStyleJob createJob(final String resourceToCopy, final boolean addAnalysisPublisher) {
        return createJob(resourceToCopy, addAnalysisPublisher, jenkins);
    }

    private FreeStyleJob createJob(final String resourceToCopy, final boolean addAnalysisPublisher,
            final Container owner) {
        FreeStyleJob job = owner.getJobs().create();
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
        AnalysisConfigurator<WarningsBuildSettings> warningsConfigurator = settings -> {
            settings.addWorkspaceScanner("Java Compiler (javac)", "**/*");
            settings.addWorkspaceScanner("JavaDoc Tool", "**/*");
            settings.addWorkspaceScanner("MSBuild", "**/*");
        };
        warningsConfigurator.accept(warningsSettings);
    }

    private void addAndConfigureTasksPublisher(final FreeStyleJob job) {
        TasksFreestyleSettings taskScannerSettings = job.addPublisher(TasksFreestyleSettings.class);
        AnalysisConfigurator<TasksFreestyleSettings> configurator = settings -> {
            settings.setHighPriorityTags("PRIO1");
            settings.setNormalPriorityTags("PRIO2,TODO");
            settings.setLowPriorityTags("PRIO3");
        };
        configurator.accept(taskScannerSettings);
    }

    public static Matcher<DashboardView> hasWarnings(final String jobName, final String pluginId, final int warningsCount) {
        return new Matcher<DashboardView>(" shows %s warnings for plugin %s and job %s", warningsCount, pluginId, jobName) {
            @Override
            public boolean matchesSafely(final DashboardView view) {
                view.open();
                try {
                    WebElement warningsLink = findPortletLink(view, jobName, pluginId);
                    String linkText = warningsLink.getText();
                    return Integer.parseInt(linkText) == warningsCount;
                } catch (NoSuchElementException | NumberFormatException e) {
                    return false;
                }
            }

            @Override
            public void describeMismatchSafely(final DashboardView view, final Description desc) {
                desc.appendText("Portlet does not show expected warnings for plugin " + pluginId);
            }
        };
    }

    private static WebElement findPortletLink(DashboardView view, String jobName, String pluginId) {
        return view.find(by.css("a[href*='%s'][href$='%s']", jobName, pluginId));
    }
}
