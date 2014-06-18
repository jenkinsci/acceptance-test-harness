package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisCollectorAction;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisCollectorColumn;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisCollectorPublisher;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisPlugin;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckstylePublisher;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindbugsPublisher;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdPublisher;
import org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.ListView;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.hasAction;
import static org.jenkinsci.test.acceptance.Matchers.hasAnalysisWarningsFor;
import static org.junit.Assert.assertThat;

/**
 * Feature: Tests for Static Code Analysis Collector (analysis-collector)
 *
 * @author Michael Prankl
 */
@WithPlugins({"analysis-collector", "checkstyle", "pmd", "findbugs", "tasks"})
public class AnalysisCollectorPluginTest extends AbstractJUnitTest {

    private static final String ANALYSIS_COLLECTOR_PLUGIN_RESOURCES = "/analysis_collector_plugin";

    /**
     * Scenario: First build with new warnings
     * Given I have job with artifacts of static analysis tools
     * And this artifacts are published by their corresponding plugins
     * When I add a post-build step to publish a combined static analyis result
     * Then the job and build will have a action "Static Analysis Warning"
     * And this action will show the combined static analysis result
     */
    @Test
    public void first_build_new_warnings() {
        FreeStyleJob job = setupJob(ANALYSIS_COLLECTOR_PLUGIN_RESOURCES, true);
        Build lastBuild = job.startBuild().waitUntilFinished();
        assertThat(job, hasAction("Static Analysis Warnings"));
        assertThat(lastBuild, hasAction("Static Analysis Warnings"));
        AnalysisCollectorAction result = new AnalysisCollectorAction(job);
        assertThat(result.getWarningNumber(), is(799));
        assertThat(result.getHighWarningNumber(), is(780));
        assertThat(result.getNormalWarningNumber(), is(11));
        assertThat(result.getLowWarningNumber(), is(8));
        assertThat(result.getNewWarningNumber(), is(799));
    }

    /**
     * Scenario: Workspace has more warnings than prior build
     * Given I have job with artifacts of static analysis tools
     * And this artifacts are published by their corresponding plugins
     * And the first build got 4 warnings in total
     * When I add a new resource that contains 4 more warnings
     * Then the second build will have 8 warnings in total
     * And the second build will have 4 new warnings
     */
    @Test
    public void more_warnings_in_second_build() {
        FreeStyleJob job = setupJob(ANALYSIS_COLLECTOR_PLUGIN_RESOURCES + "/Tasks.java", true);
        job.startBuild().waitUntilFinished();
        // copy new resource
        job.configure();
        job.copyResource(ANALYSIS_COLLECTOR_PLUGIN_RESOURCES + "/Tasks2.java");
        job.save();
        // start second build
        job.startBuild().waitUntilFinished();
        AnalysisCollectorAction result = new AnalysisCollectorAction(job);
        assertThat(result.getWarningNumber(), is(8));
        assertThat(result.getHighWarningNumber(), is(2));
        assertThat(result.getNormalWarningNumber(), is(4));
        assertThat(result.getLowWarningNumber(), is(2));
        assertThat(result.getNewWarningNumber(), is(4));
    }

    /**
     * Scenario: Build should become status unstable when warning threshold is exceeded.
     * Given I have job with artifacts of static analysis tools
     * And this artifacts are published by their corresponding plugins
     * And the resources of the job contain 6 warnings
     * And I set the unstable status threshold for all priorities to 5
     * When I start a build
     * Then the build should get status unstable
     */
    @Test
    public void warning_threshold_build_unstable() {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.copyResource(ANALYSIS_COLLECTOR_PLUGIN_RESOURCES + "/findbugs.xml");
        job.addPublisher(FindbugsPublisher.class);
        AnalysisCollectorPublisher analysis = job.addPublisher(AnalysisCollectorPublisher.class);
        analysis.advanced.click();
        analysis.warningThresholdUnstable.sendKeys("5");
        job.save();
        job.startBuild().waitUntilFinished().shouldBeUnstable();
    }

    /**
     * Scenario: Analysis Collector Plugin should only collect warnings of the checked plugins.
     * Given I have job with artifacts of static analysis tools
     * And this artifacts are published by their corresponding plugins
     * And analysis plugin XYZ gets deselected
     * When I start a build
     * Then the warnings of plugin XYZ will not be collected
     * <br>
     * The test will perform one build for every deselected plugin
     * and will check if the warnings of the deselected plugin haven't been collected.
     */
    @Test
    public void deselect_plugins() {
        FreeStyleJob job = setupJob(ANALYSIS_COLLECTOR_PLUGIN_RESOURCES, true);
        // no checkstyle
        AnalysisCollectorAction result = deselectPluginAndBuild(AnalysisPlugin.CHECKSTYLE, job);
        assertThat(result.getWarningNumber(), is(23));
        // no checkstyle, no findbugs
        result = deselectPluginAndBuild(AnalysisPlugin.FINDBUGS, job);
        assertThat(result.getWarningNumber(), is(17));
        // no checkstyle, no findbugs, no pmd
        result = deselectPluginAndBuild(AnalysisPlugin.PMD, job);
        assertThat(result.getWarningNumber(), is(8));
        // no checkstyle, no findbugs, no pmd, no tasks => zero warnings
        result = deselectPluginAndBuild(AnalysisPlugin.TASKS, job);
        assertThat(result.getWarningNumber(), is(0));
    }

    /**
     * Scenario: Job should show analysis results of selected plugins
     * Given I have job with artifacts of static analysis tools
     * And this artifacts are published by their corresponding plugins
     * And the resources of the job contain warnings
     * When I start a build
     * Then the job should show the warnings of each selected plugin
     */
    @Test
    public void check_analysis_results_of_job() {
        FreeStyleJob job = setupJob(ANALYSIS_COLLECTOR_PLUGIN_RESOURCES, true);
        job.startBuild().waitUntilFinished();
        // check if results for checked plugins are visible
        assertThat(job,
                allOf(
                        hasAnalysisWarningsFor(AnalysisPlugin.CHECKSTYLE),
                        hasAnalysisWarningsFor(AnalysisPlugin.PMD),
                        hasAnalysisWarningsFor(AnalysisPlugin.FINDBUGS),
                        hasAnalysisWarningsFor(AnalysisPlugin.TASKS)
                )
        );
        // check if results for unchecked/not installed plugins are NOT visible
        assertThat(job,
                not(
                        anyOf(
                                hasAnalysisWarningsFor(AnalysisPlugin.WARNINGS),
                                hasAnalysisWarningsFor(AnalysisPlugin.DRY)
                        )
                )
        );
    }

    /**
     * Scenario: Custom list view column shows number of warnings
     * Given I have job with artifacts of static analysis tools
     * And this artifacts are published by their corresponding plugins
     * And the resources of the job contain warnings
     * And this job is included in a custom list view with added column "Number of warnings"
     * When I start a build
     * Then the list view will show the correct number of total warnings
     * And the mouse-over will show the correct number of warnings per checked plugin
     */
    @Test
    public void check_warnings_column(){
        FreeStyleJob job = setupJob(ANALYSIS_COLLECTOR_PLUGIN_RESOURCES, true);
        job.startBuild().waitUntilFinished();
        ListView view = jenkins.views.create(ListView.class, jenkins.createRandomName());
        view.configure();
        view.matchAllJobs();
        AnalysisCollectorColumn analysisCollectorColumn = view.addColumn(AnalysisCollectorColumn.class);
        analysisCollectorColumn.checkPlugin(AnalysisPlugin.CHECKSTYLE, false);
        view.save();
    }

    /**
     * Configures the given job, deselects the given plugin and performs a build.
     *
     * @param plugin the plugin
     * @param job    the job
     * @return the result action for asserts etc.
     */
    private AnalysisCollectorAction deselectPluginAndBuild(AnalysisPlugin plugin, Job job) {
        job.configure();
        AnalysisCollectorPublisher publisher = job.getPublisher(AnalysisCollectorPublisher.class);
        publisher.checkCollectedPlugin(plugin, false);
        job.save();
        job.startBuild().waitUntilFinished();
        return new AnalysisCollectorAction(job);
    }

    /**
     * Setup a job with given resources and needed publishers.
     *
     * @param resourceToCopy Resource to copy to build (Directory or File path)
     * @return the made job
     */
    public FreeStyleJob setupJob(String resourceToCopy, boolean addAnalysisPublisher) {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.copyResource(resourceToCopy);
        job.addPublisher(CheckstylePublisher.class);
        job.addPublisher(PmdPublisher.class);
        job.addPublisher(FindbugsPublisher.class);
        TaskScannerPublisher taskScannerPublisher = job.addPublisher(TaskScannerPublisher.class);
        taskScannerPublisher.highPriorityTags.sendKeys("PRIO1");
        taskScannerPublisher.normalPriorityTags.sendKeys("PRIO2,TODO");
        taskScannerPublisher.lowPriorityTags.sendKeys("PRIO3");
        if (addAnalysisPublisher) {
            job.addPublisher(AnalysisCollectorPublisher.class);
        }
        job.save();
        return job;
    }

}
