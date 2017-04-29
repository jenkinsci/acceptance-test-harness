package plugins;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisConfigurator;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction;
import org.jenkinsci.test.acceptance.plugins.tasks.TasksFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.tasks.TasksMavenSettings;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Container;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Acceptance tests for the open tasks plugin.
 *
 * @author Martin Ende
 * @author Ullrich Hafner
 */
@WithPlugins("tasks")
public class TaskScannerPluginTest extends AbstractAnalysisTest<TaskScannerAction> {
    private static final String TASKS_PLUGIN_PREFIX = "/tasks_plugin/";
    private static final String TASKS_FILES = TASKS_PLUGIN_PREFIX + "fileset1";
    private static final String NO_GOAL = "";

    @Override
    protected void assertThatDetailsAreFilled(final TaskScannerAction action) {
        assertXmlApiMatchesExpected(action.getBuild(), "tasksResult/api/xml?depth=0",
                TASKS_PLUGIN_PREFIX + "api_depth_0-2_x.xml", false);

        // The file set consists of 9 files, whereof
        //   - 2 file names match the exclusion pattern
        //   - 7 files are to be scanned for tasks
        //   - 5 files actually contain tasks with the specified tags (with case sensitivity)
        assertThatOpenTaskCountLinkIs(action, 6, 7);

        assertFilesTabFS1E1(action);
        assertTypesTabFS1E1(action);
        assertWarningsTabFS1E1(action);

        assertWarningExtraction(action, "TSRDockerImage.java", 84, "TODO",
                "properly wait for either cidfile to appear or process to exit");
        assertWarningExtraction(action, "TSRCleaner.java", 40, "@Deprecated", "");

        verifySourceLine(action, "TSRDockerImage.java", 84,
                "084         // TODO: properly wait for either cidfile to appear or process to exit",
                "Normal Priority");
    }

    /**
     * Verifies that different number of open tasks are found depending on the configured case sensitivity option.
     */
    @Test
    public void shouldFindMoreWarningsWhenIgnoringCase() {
        FreeStyleJob job = createFreeStyleJob();
        buildSuccessfulJob(job);

        // now disable case sensitivity and build again. Then the publisher shall also
        // find the high priority task in Ec2Provider.java:133.

        editJob(false, job, TasksFreestyleSettings.class, settings -> settings.setIgnoreCase(true));

        Build build = buildSuccessfulJob(job);

        build.open();

        TaskScannerAction action = new TaskScannerAction(build);

        assertThatOpenTaskCountLinkIs(action, 7, 7);
        assertThatNewOpenTaskCountLinkIs(action, 1);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(7));
        assertThat(action.getNumberOfNewWarnings(), is(1));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(2));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(4));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(1));

        action.openNew();

        assertThat(action.getResultLinkByXPathText("TSREc2Provider.java:133"), startsWith("source"));
    }

    private void assertThatTasksResultExists(final Job job, final Build lastBuild) {
        String actionName = "Open Tasks";
        assertThat(lastBuild, hasAction(actionName));
        assertThat(job, hasAction(actionName));
        assertThat(job.getLastBuild(), hasAction(actionName));
    }

    private void assertThatOpenTaskCountLinkIs(final TaskScannerAction action, final int numberOfTasks, final int numberOfFiles) {
        String linkText = numberOfTasks + " open task" + plural(numberOfTasks);
        assertThat(action.getResultLinkByXPathText(linkText),
                is("tasksResult"));
        assertThat(action.getResultTextByXPathText(linkText),
                endsWith("in " + numberOfFiles+ " workspace file" + plural(numberOfFiles) + "."));
    }

    private void assertThatNewOpenTaskCountLinkIs(final TaskScannerAction action, final int numberOfNewTasks) {
        String linkText = numberOfNewTasks + " new open task" + plural(numberOfNewTasks);
        assertThat(action.getResultLinkByXPathText(linkText), is("tasksResult/new"));
    }

    private void assertThatClosedTaskCountLinkIs(final TaskScannerAction action, final int numberOfClosedTasks) {
        String linkText = numberOfClosedTasks + " closed task" + plural(numberOfClosedTasks);
        assertThat(action.getResultLinkByXPathText(linkText), is("tasksResult/fixed"));
    }

    /**
     * Verifies that the plugin correctly works in freestyle jobs for tags that are treated as regular expression.
     */
    @Test
    public void should_detect_regular_expression_in_freestyle_job() throws Exception {
        FreeStyleJob job = createFreeStyleJob("/tasks_plugin/regexp",
                settings -> {
                    settings.setPattern("**/*.txt");
                    settings.setNormalPriorityTags("^.*(TODO(?:[0-9]*))(.*)$");
                    settings.setAsRegexp(true);
                });

        verifyRegularExpressionScannerResult(job);
    }

    /**
     * Verifies that the plugin correctly works in maven jobs for tags that are treated as regular expression.
     */
    @Test
    public void should_detect_regular_expression_in_maven_job() throws Exception {
        MavenModuleSet job = createMavenJob("/tasks_plugin/regexp",
                settings -> {
                    settings.setPattern("**/*.txt");
                    settings.setNormalPriorityTags("^.*(TODO(?:[0-9]*))(.*)$");
                    settings.setAsRegexp(true);
                });

        verifyRegularExpressionScannerResult(job);
    }

    private void verifyRegularExpressionScannerResult(final Job job) {
        Build build = buildSuccessfulJob(job);
        build.open();

        TaskScannerAction action = new TaskScannerAction(build);

        assertThatOpenTaskCountLinkIs(action, 5, 1);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(5));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(5));
    }

    /**
     * Verifies that the plugin correctly works for multiple tags per priority. In the first step the task scanner is
     * configured with two tags for high priority tasks. Prior to the second build also the normal and low priority tag
     * list is extended.
     */
    @Test
    public void should_find_multiple_task_tags() throws Exception {
        FreeStyleJob job = createFreeStyleJob(settings -> {
            settings.setPattern("**/*.java");
            settings.setExcludePattern("**/*Test.java");
            settings.setHighPriorityTags("FIXME,BUG");
            settings.setNormalPriorityTags("TODO");
            settings.setLowPriorityTags("@Deprecated");
            settings.setIgnoreCase(true);
        });

        Build build = buildSuccessfulJob(job);

        build.open();

        TaskScannerAction action = new TaskScannerAction(build);

        // The file set consists of 9 files, whereof
        //   - 2 file names match the exclusion pattern
        //   - 7 files are to be scanned for tasks
        //   - 6 files actually contain tasks with the specified tags
        //
        // The expected task priorities are:
        //   - 3x high
        //   - 4x medium
        //   - 1x low

        assertThatOpenTaskCountLinkIs(action, 8, 7);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(8));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(3));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(4));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(1));

        // now add further task tags. Then the publisher shall also
        // find the second priority task in TSRDockerImage.java (line 102) amd
        // a low priority task in TSRDockerImage.java (line 56).

        editJob(false, job, TasksFreestyleSettings.class,
                settings -> {
                    settings.setNormalPriorityTags("TODO,XXX");
                    settings.setLowPriorityTags("@Deprecated,\\?\\?\\?");
                });

        build = buildSuccessfulJob(job);

        build.open();

        action = new TaskScannerAction(build);

        assertThatOpenTaskCountLinkIs(action, 10, 7);
        assertThatNewOpenTaskCountLinkIs(action, 2);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(10));
        assertThat(action.getNumberOfNewWarnings(), is(2));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(3));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(5));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(2));

        assertFilesTabFS1E2(action);
        assertTypesTabFS1E2(action);
        assertWarningsTabFS1E2(action);
    }

    /**
     * Verifies the detection of closed tasks. Therefore two runs of the same job with the same task scanner setup are
     * conducted but the fileset in the workspace will be replaced by the same files containing less warnings for the
     * second run. The tasks that have been removed shall be correctly listed as closed tasks.
     */
    @Test
    public void should_report_closed_tasks() throws Exception {
        FreeStyleJob job = createFreeStyleJob(settings -> {
            settings.setPattern("**/*.java");
            settings.setExcludePattern("**/*Test.java");
            settings.setHighPriorityTags("FIXME");
            settings.setNormalPriorityTags("TODO");
            settings.setLowPriorityTags("@Deprecated");
            settings.setIgnoreCase(false);
        });
        buildSuccessfulJob(job);

        // this time we do not check the task scanner output as the result is the same
        // as for single_task_tags_and_exclusion_pattern
        // So we proceed directly with the preparation of build #2

        editJob("/tasks_plugin/fileset1_less", false, job, TasksFreestyleSettings.class);

        Build build = buildSuccessfulJob(job);

        build.open();

        TaskScannerAction action = new TaskScannerAction(build);

        // In the first build the task priorities were
        //   - 1x high
        //   - 4x medium
        //   - 1x low
        //
        // For the second build (reduced warnings) the expected priorities are
        //   - 3x medium
        //
        // --> we expect 3 closed tasks (1x high, 1x normal, 1x low)

        assertThatOpenTaskCountLinkIs(action, 3, 7);
        assertThatClosedTaskCountLinkIs(action, 3);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(3));
        assertThat(action.getNumberOfFixedWarnings(), is(3));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(0));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(3));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(0));

        assertFixedTab(action);
    }

    /**
     * Check the "Run always" option of the publisher, i.e whether the task scanner activity is skipped in case the main
     * build step has already failed and the option "run always" is not activated. The option is activated for the
     * second part to also scan for tasks in this failed job
     */
    @Test
    public void should_run_on_failed_builds_if_configured() throws Exception {
        FreeStyleJob job = createFreeStyleJob(settings -> {
            settings.setPattern("**/*.java");
            settings.setExcludePattern("**/*Test.java");
            settings.setHighPriorityTags("FIXME");
            settings.setIgnoreCase(true);
            settings.setCanRunOnFailed(false);
        });

        job.configure();
        job.addShellStep("exit 1"); //ensures the FAILURE status of the main build
        job.save();

        Build build = buildFailingJob(job);

        // the task scanner activity shall be skipped due to the failed main build
        // so we have to search for the particular console output
        assertThatConsoleContains(build, ".*\\[TASKS\\] Skipping publisher since build result is FAILURE");

        // now activate "Run always"
        editJob(false, job, TasksFreestyleSettings.class,
                settings -> settings.setCanRunOnFailed(true));

        build = buildFailingJob(job);

        build.open();

        TaskScannerAction action = new TaskScannerAction(build);

        // as the failed result is now ignored, we expect 2 open tasks, both
        // of high priority and both considered as new warnings.
        assertThatOpenTaskCountLinkIs(action, 2, 7);
        assertThatNewOpenTaskCountLinkIs(action, 2);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(2));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(2));
    }

    private void assertThatConsoleContains(final Build lastBuild, final String regexp) {
        assertThat(lastBuild.getConsole(), containsRegexp(regexp, Pattern.MULTILINE));
    }

    /**
     * Check the correct treatment and display of tasks in files with windows-1251 (a.k.a. cp1251) encoding. Reproduces
     * the observations described in JENKINS-22744.
     */
    // Note: In order to run this test in IntelliJ the encoding of the source needs to be set to windows-1251
    @Test @Issue("JENKINS-22744")
    public void should_use_file_encoding_windows1251_when_parsing_files() throws Exception {
        FreeStyleJob job = createFreeStyleJob("/tasks_plugin/cp1251_files", settings -> {
            settings.setPattern("**/*.java");
            settings.setExcludePattern("**/*Test.java");
            settings.setHighPriorityTags("FIXME");
            settings.setNormalPriorityTags("TODO");
            settings.setIgnoreCase(true);
            settings.setDefaultEncoding("windows-1251");
        });

        Build build = buildSuccessfulJob(job);

        assertThatTasksResultExists(job, build);

        build.open();

        TaskScannerAction action = new TaskScannerAction(build);

        assertThatOpenTaskCountLinkIs(action, 2, 1);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(2));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(1));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(1));

        assertWarningExtraction(action, "TestTaskScanner.java", 5, "TODO", "пример комментария на русском");

        verifySourceLine(action, "TestTaskScanner.java", 4,
                "4   //FIXME тестирование Jenkins", "High Priority");
        verifySourceLine(action, "TestTaskScanner.java", 5,
                "5   //TODO пример комментария на русском", "Normal Priority");
    }

    /**
     * Verifies several task scanner functions for a maven project such as - treatment of exclusion pattern - correct
     * counting of warnings - correct prioritisation of warnings - changing build status based on thresholds.
     */
    @Test
    public void should_find_tasks_in_maven_project() throws Exception {
        MavenModuleSet job = createMavenJob("/tasks_plugin/sample_tasks_project", "clean package test",
                settings -> {
                    settings.setPattern("**/*.java");
                    settings.setExcludePattern("**/*Test.java");
                    settings.setHighPriorityTags("FIXME");
                    settings.setNormalPriorityTags("TODO");
                    settings.setLowPriorityTags("@Deprecated");
                    settings.setIgnoreCase(false);
                });

        // as one of the unit tests fail, the build should be unstable
        Build build = buildUnstableJob(job);

        assertThatTasksResultExists(job, build);

        build.open();

        TaskScannerAction action = new TaskScannerAction(build);

        // The expected task priorities are:
        //   - 1x high
        //   - 1x medium
        //   - 1x low

        assertThatOpenTaskCountLinkIs(action, 3, 1);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(3));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(1));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(1));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(1));

        // re-configure the job and set a threshold to mark the build as failed
        editJob(false, job, TasksMavenSettings.class, settings -> {
            settings.setBuildFailedTotalHigh("0");
            settings.setBuildFailedTotalNormal("5");
            settings.setBuildFailedTotalLow("10");
        });

        // as the threshold for high priority warnings is exceeded, the build should be marked as failed
        build = buildFailingJob(job);

        assertThatTasksResultExists(job, build);

        build.open();

        //check build result text
        assertThat(action.getPluginResult(build),
                is("Plug-in Result: FAILED - 1 warning of priority High exceeds the threshold of 0 by 1 (Reference build: #1)"));

        action.open();

        //no change in warning counts expected
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(1));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(1));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(1));
    }

    /**
     * Verifies the correct treatment of the status thresholds (totals). Therefore a more complex test case has been
     * created which modifies files and task tags to scan for multiple times to create appropriate scenarios for
     * different thresholds.
     * <p/>
     * It shall also check whether the determination / justification of the build status is done based on the highest
     * priority threshold that has been exceeded.
     * <p/>
     * The test case consists of 6 steps: 1 - create reference build (SUCCESS) -> no threshold exceeded 2 - exceed the
     * UNSTABLE LOW threshold 3 - exceed the UNSTABLE NORMAL threshold but do not exceed UNSTABLE LOW 4 - exceed
     * UNSTABLE LOW, NORMAL and HIGH -> build status justified with HIGH priority tasks 5 - further exceed the UNSTABLE
     * TOTAL threshold -> new justification for build status 6 - further exceed the FAILURE TOTAL threshold -> build
     * failed 7 - remove most of the task tags -> build is stable again
     */
    @Test
    public void should_set_build_result_based_on_status_thresholds() {
        FreeStyleJob job = createFreeStyleJob("/tasks_plugin/fileset1_less", settings -> {
            settings.setPattern("**/*.java");
            settings.setExcludePattern("**/*Test.java");
            settings.setHighPriorityTags(""); //no high prio tags
            settings.setNormalPriorityTags("TODO");
            settings.setLowPriorityTags("@Deprecated");
            settings.setIgnoreCase(false);
            //setup thresholds
            settings.setBuildUnstableTotalLow("1");
            settings.setBuildUnstableTotalNormal("4");
            settings.setBuildUnstableTotalHigh("0");
            settings.setBuildUnstableTotalAll("10");
            settings.setBuildFailedTotalAll("15");
        });

        TaskScannerAction action = new TaskScannerAction(job.getLastBuild());

        // In order to increase readability each step has been placed in a separate
        // private function

        job = status_thresholds_step1(job, action);
        job = status_thresholds_step2(job, action);
        job = status_thresholds_step3(job, action);
        job = status_thresholds_step4(job, action);
        job = status_thresholds_step5(job, action);
        job = status_thresholds_step6(job, action);
        status_thresholds_step7(job, action);
    }

    /**
     * This method does special configurations for test step 1 of test {@link TaskScannerPluginTest#should_set_build_result_based_on_status_thresholds()}.
     * The scenario is that the file set consists of 9 files, whereof - 7 files are to be scanned for tasks
     * <p/>
     * The expected task priorities are: - 0x high - 3x medium - 0x low
     * <p/>
     * So, the build status shall be SUCCESS as no threshold will be exceeded.     *
     *
     * @param job    the {@link org.jenkinsci.test.acceptance.po.FreeStyleJob} created in the Test
     * @param action a the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the current
     *               job
     * @return The modified {@link org.jenkinsci.test.acceptance.po.FreeStyleJob}.
     */
    private FreeStyleJob status_thresholds_step1(final FreeStyleJob job, final TaskScannerAction action) {
        Build lastBuild = buildSuccessfulJob(job);

        lastBuild.open();

        assertThatOpenTaskCountLinkIs(action, 3, 7);
        assertThatNewOpenTaskCountLinkIs(action, 3);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(3));
        // Note: high warning is omitted in summary table because no high prio tag is defined.
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(3));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(0));
        assertThat(action.getPluginResult(lastBuild), is("Plug-in Result: SUCCESS - no threshold has been exceeded"));

        return job;
    }

    /**
     * This method does special configurations for test step 2 of test {@link TaskScannerPluginTest#should_set_build_result_based_on_status_thresholds()}.
     * The scenario is that the file set consists of 9 files, whereof - 7 files are to be scanned for tasks
     * <p/>
     * The expected task priorities are: - 0x high - 4x medium - 2x low
     * <p/>
     * So, the build status shall be UNSTABLE due to low priority threshold is exceeded by 1.
     *
     * @param job    the {@link FreeStyleJob} created in the Test
     * @param action a the {@link TaskScannerAction} object for the current job
     * @return The modified {@link org.jenkinsci.test.acceptance.po.FreeStyleJob}.
     */
    private FreeStyleJob status_thresholds_step2(final FreeStyleJob job, final TaskScannerAction action) {
        editJob("/tasks_plugin/fileset1", false, job, TasksFreestyleSettings.class, settings -> {
            settings.setLowPriorityTags("@Deprecated,\\?\\?\\?"); // add tag "???"
        });

        Build lastBuild = buildUnstableJob(job);

        lastBuild.open();

        assertThatOpenTaskCountLinkIs(action, 6, 7);
        assertThatNewOpenTaskCountLinkIs(action, 3);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(6));
        assertThat(action.getNumberOfNewWarnings(), is(3));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(4));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(2));
        assertThat(action.getPluginResult(lastBuild),
                is("Plug-in Result: UNSTABLE - 2 warnings of priority Low exceed the threshold of 1 by 1 (Reference build: #1)"));

        return job;
    }

    /**
     * This method does special configurations for test step 3 of test {@link TaskScannerPluginTest#should_set_build_result_based_on_status_thresholds()}.
     * The scenario is that the file set consists of 9 files, whereof - 7 files are to be scanned for tasks
     * <p/>
     * The expected task priorities are: - 0x high - 5x medium - 1x low
     * <p/>
     * So, the build status shall be UNSTABLE due to normal priority threshold is exceeded by 1.
     *
     * @param job    the {@link FreeStyleJob} created in the Test
     * @param action a the {@link TaskScannerAction} object for the current job
     * @return The modified {@link org.jenkinsci.test.acceptance.po.FreeStyleJob}.
     */
    private FreeStyleJob status_thresholds_step3(final FreeStyleJob job, final TaskScannerAction action) {
        editJob(false, job, TasksFreestyleSettings.class, settings -> {
            settings.setNormalPriorityTags("TODO,XXX"); // add tag "XXX"
            settings.setLowPriorityTags("@Deprecated"); // remove tag "???"
        });

        Build lastBuild = buildUnstableJob(job);

        lastBuild.open();

        assertThatOpenTaskCountLinkIs(action, 6, 7);
        assertThatNewOpenTaskCountLinkIs(action, 3);

        // Note:
        //   As the previous build was unstable the determination which warnings have changed is
        //   done based on the reference buil (#1)!!
        //   The same applies to step 4 to 6
        assertThat(action.getResultLinkByXPathText("3 new open tasks"), is("tasksResult/new"));

        action.open();

        assertThat(action.getNumberOfWarnings(), is(6));
        assertThat(action.getNumberOfNewWarnings(), is(3));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(5));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(1));
        assertThat(action.getPluginResult(lastBuild),
                is("Plug-in Result: UNSTABLE - 5 warnings of priority Normal exceed the threshold of 4 by 1 (Reference build: #1)"));

        return job;
    }

    /**
     * This method does special configurations for test step 4 of test {@link TaskScannerPluginTest#should_set_build_result_based_on_status_thresholds()}.
     * The scenario is that the file set consists of 9 files, whereof - 7 files are to be scanned for tasks
     * <p/>
     * The expected task priorities are: - 1x high - 5x medium - 2x low
     * <p/>
     * So, the build status shall be UNSTABLE due to high priority threshold is exceeded by 1.
     *
     * @param job    the {@link FreeStyleJob} created in the Test
     * @param action a the {@link TaskScannerAction} object for the current job
     * @return The modified {@link org.jenkinsci.test.acceptance.po.FreeStyleJob}.
     */
    private FreeStyleJob status_thresholds_step4(final FreeStyleJob job, final TaskScannerAction action) {
        editJob(false, job, TasksFreestyleSettings.class, settings -> {
            settings.setLowPriorityTags("@Deprecated,\\?\\?\\?"); // add tag "???"
            settings.setHighPriorityTags("FIXME"); // add tag "FIXME"
        });

        Build lastBuild = buildUnstableJob(job);

        lastBuild.open();

        assertThatOpenTaskCountLinkIs(action, 8, 7);
        assertThatNewOpenTaskCountLinkIs(action, 5);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(8));
        assertThat(action.getNumberOfNewWarnings(), is(5));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(1));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(5));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(2));
        assertThat(action.getPluginResult(lastBuild),
                is("Plug-in Result: UNSTABLE - 1 warning of priority High exceeds the threshold of 0 by 1 (Reference build: #1)"));

        return job;
    }

    /**
     * This method does special configurations for test step 5 of test {@link TaskScannerPluginTest#should_set_build_result_based_on_status_thresholds()}.
     * The scenario is that the file set consists of 19 files, whereof - 17 files are to be scanned for tasks
     * <p/>
     * The expected task priorities are: -  1x high - 11x medium -  3x low
     * <p/>
     * So, the build status shall be UNSTABLE due to total warnings threshold is exceeded by 5.
     *
     * @param job    the {@link FreeStyleJob} created in the Test
     * @param action a the {@link TaskScannerAction} object for the current job
     * @return The modified {@link org.jenkinsci.test.acceptance.po.FreeStyleJob}.
     */
    private FreeStyleJob status_thresholds_step5(final FreeStyleJob job, final TaskScannerAction action) {
        // add a second shell step to copy another folder
        editJob("/tasks_plugin/fileset2", true, job, TasksFreestyleSettings.class, settings -> {
            settings.setNormalPriorityTags("TODO"); //remove tag "XXX"
        });

        Build lastBuild = buildUnstableJob(job);

        lastBuild.open();

        assertThatOpenTaskCountLinkIs(action, 15, 17);
        assertThatNewOpenTaskCountLinkIs(action, 12);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(15));
        assertThat(action.getNumberOfNewWarnings(), is(12));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(1));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(11));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(3));
        assertThat(action.getPluginResult(lastBuild),
                is("Plug-in Result: UNSTABLE - 15 warnings exceed the threshold of 10 by 5 (Reference build: #1)"));

        return job;
    }

    /**
     * This method does special configurations for test step 6 of test
     * {@link TaskScannerPluginTest#should_set_build_result_based_on_status_thresholds()}.
     * The scenario is that the file set consists of 19 files, whereof - 17 files are to be scanned for tasks
     * <p/>
     * The expected task priorities are: -  2x high - 11x medium -  3x low
     * <p/>
     * So, the build status shall be FAILED due to total warnings threshold is exceeded by 1.
     *
     * @param job    the {@link FreeStyleJob} created in the Test
     * @param action a the {@link TaskScannerAction} object for the current job
     * @return The modified {@link org.jenkinsci.test.acceptance.po.FreeStyleJob}.
     */
    private FreeStyleJob status_thresholds_step6(final FreeStyleJob job, final TaskScannerAction action) {
        editJob(false, job, TasksFreestyleSettings.class,
                settings -> settings.setIgnoreCase(true));

        Build lastBuild = buildFailingJob(job);

        lastBuild.open();

        assertThatOpenTaskCountLinkIs(action, 16, 17);
        assertThatNewOpenTaskCountLinkIs(action, 13);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(16));
        assertThat(action.getNumberOfNewWarnings(), is(13));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(2));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(11));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(3));
        assertThat(action.getPluginResult(lastBuild),
                is("Plug-in Result: FAILED - 16 warnings exceed the threshold of 15 by 1 (Reference build: #1)"));

        return job;
    }

    /**
     * This method does special configurations for test step 6 of test {@link TaskScannerPluginTest#should_set_build_result_based_on_status_thresholds()}.
     * Another shell step is added which consists of a small script to replace all TODO, todo, FIXME, fixme, XXX,
     * Deprecated occurences in the workspace files by the string "CLOSED".
     * <p/>
     * The scenario is that the file set consists of 19 files, whereof - 17 files are to be scanned for tasks
     * <p/>
     * The expected task priorities are: -  0x high -  0x medium -  1x low
     * <p/>
     * So, the build status shall be SUCCESS as no threshold will be exceeded.
     *
     * @param job    the {@link org.jenkinsci.test.acceptance.po.FreeStyleJob} created in the Test
     * @param action a the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the current
     *               job
     * @return The modified {@link org.jenkinsci.test.acceptance.po.FreeStyleJob}.
     */
    private FreeStyleJob status_thresholds_step7(final FreeStyleJob job, final TaskScannerAction action) {
        job.configure();
        job.addShellStep("NEW=\"CLOSED\"\n" +
                "for t in \"todo\" \"TODO\" \"XXX\" \"fixme\" \"FIXME\" \"Deprecated\"\n" +
                "do\n" +
                "  OLD=$t\n" +
                "  for f in `ls`\n" +
                "  do\n" +
                "    if [ -f $f -a -r $f ]; then\n" +
                "      if file --mime-type $f | grep -q \"^${f}: text/\"; then\n" +
                "        sed \"s/$OLD/$NEW/\" \"$f\" > \"${f}.new\"\n" +
                "        mv \"${f}.new\" \"$f\"\n" +
                "      else\n" +
                "        echo \"Info: $f is not a text file. Skipped.\"\n" +
                "      fi" +
                "    else\n" +
                "      echo \"Error: Cannot read $f\"\n" +
                "    fi\n" +
                "  done\n" +
                "done");
        job.save();

        Build lastBuild = buildSuccessfulJob(job);

        lastBuild.open();

        assertThatOpenTaskCountLinkIs(action, 1, 17);
        assertThatNewOpenTaskCountLinkIs(action, 1);
        assertThatClosedTaskCountLinkIs(action, 3);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(1));
        assertThat(action.getNumberOfNewWarnings(), is(1));
        assertThat(action.getNumberOfFixedWarnings(), is(3));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(0));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(0));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(1));
        assertThat(action.getPluginResult(lastBuild),
                is("Plug-in Result: SUCCESS - no threshold has been exceeded (Reference build: #1)"));

        return job;
    }

    /**
     * This method asserts the correct content of the files tab for the files in fileset 1 with the TaskScanner scanning
     * for FIXME, TODO and @Deprecated (case sensitive). +
     *
     * @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the current job
     */
    private void assertFilesTabFS1E1(TaskScannerAction tsa) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();

        expectedContent.put("TSRCleaner.java", 1);
        expectedContent.put("TSRDockerImage.java", 1);
        expectedContent.put("TSRGitRepo.java", 2);
        expectedContent.put("TSRJenkinsAcceptanceTestRule.java", 1);
        expectedContent.put("TSRWinstoneDockerController.java", 1);

        assertThat(tsa.getFileTabContents(), is(expectedContent));
    }

    /**
     * This method asserts the correct content of the files tab for the files in fileset 1 with the TaskScanner scanning
     * for FIXME, BUG, TODO, XXX, @Deprecated and ??? (not case sensitive). +
     *
     * @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the current job
     */
    private void assertFilesTabFS1E2(TaskScannerAction tsa) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();

        expectedContent.put("TSRCleaner.java", 1);
        expectedContent.put("TSRDockerImage.java", 3);
        expectedContent.put("TSRGitRepo.java", 3);
        expectedContent.put("TSREc2Provider.java", 1);
        expectedContent.put("TSRJenkinsAcceptanceTestRule.java", 1);
        expectedContent.put("TSRWinstoneDockerController.java", 1);

        assertThat(tsa.getFileTabContents(), is(expectedContent));
    }

    /**
     * This method asserts the correct content of the Types tab for the files in fileset 1 with the TaskScanner scanning
     * for FIXME, TODO and @Deprecated (case sensitive). +
     *
     * @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the current job
     */
    private void assertTypesTabFS1E1(TaskScannerAction tsa) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();

        expectedContent.put("@Deprecated", 1);
        expectedContent.put("FIXME", 1);
        expectedContent.put("TODO", 4);

        assertThat(tsa.getTypesTabContents(), is(expectedContent));
    }

    /**
     * This method asserts the correct content of the Types tab for the files in fileset 1 with the TaskScanner scanning
     * for FIXME, BUG, TODO, XXX, @Deprecated and ??? (not case sensitive). +
     *
     * @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the current job
     */
    private void assertTypesTabFS1E2(TaskScannerAction tsa) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();

        expectedContent.put("@DEPRECATED", 1);
        expectedContent.put("FIXME", 2);
        expectedContent.put("TODO", 4);
        expectedContent.put("BUG", 1);
        expectedContent.put("XXX", 1);
        expectedContent.put("???", 1);

        assertThat(tsa.getTypesTabContents(), is(expectedContent));
    }

    /**
     * This method asserts the correct content of the Warnings tab for the files in fileset 1 with the TaskScanner
     * scanning for FIXME, TODO and @Deprecated (case sensitive). +
     *
     * @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the current job
     */
    private void assertWarningsTabFS1E1(TaskScannerAction tsa) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();

        expectedContent.put("TSRGitRepo.java:38", 38);
        expectedContent.put("TSRGitRepo.java:69", 69);
        expectedContent.put("TSRDockerImage.java:84", 84);
        expectedContent.put("TSRJenkinsAcceptanceTestRule.java:51", 51);
        expectedContent.put("TSRWinstoneDockerController.java:73", 73);
        expectedContent.put("TSRCleaner.java:40", 40);

        assertThat(tsa.getWarningsTabContents(), is(expectedContent));
    }

    /**
     * This method asserts the correct content of the Warnings tab for the files in fileset 1 with the TaskScanner
     * scanning for FIXME, BUG, TODO, XXX, @Deprecated and ??? (not case sensitive). +
     *
     * @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the current job
     */
    private void assertWarningsTabFS1E2(TaskScannerAction tsa) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();

        expectedContent.put("TSRGitRepo.java:38", 38);
        expectedContent.put("TSRGitRepo.java:69", 69);
        expectedContent.put("TSRGitRepo.java:88", 88);
        expectedContent.put("TSRDockerImage.java:56", 56);
        expectedContent.put("TSRDockerImage.java:84", 84);
        expectedContent.put("TSRDockerImage.java:102", 102);
        expectedContent.put("TSRJenkinsAcceptanceTestRule.java:51", 51);
        expectedContent.put("TSRWinstoneDockerController.java:73", 73);
        expectedContent.put("TSRCleaner.java:40", 40);
        expectedContent.put("TSREc2Provider.java:133", 133);

        assertThat(tsa.getWarningsTabContents(), is(expectedContent));
    }


    /**
     * This method asserts the correct content of the Fixed tab for the files in fileset_1_less with the TaskScanner
     * scanning for FIXME, TODO, and @Deprecated (case sensitive).
     *
     * @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the current job
     */
    private void assertFixedTab(TaskScannerAction tsa) {
        SortedMap<String, String> expectedContent = new TreeMap<>();

        expectedContent.put("TSRCleaner.java", "@Deprecated");
        expectedContent.put("TSRDockerImage.java", "TODO");
        expectedContent.put("TSRGitRepo.java", "FIXME");

        assertThat(tsa.getFixedTabContents(), is(expectedContent));
    }

    /**
     * This method asserts that a certain task is contained in the table shown in the "Warnings"-tab with the correct
     * task type and text
     *
     * @param tsa         the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the
     *                    current job
     * @param filename    the name of the source file containing the task
     * @param lineNumber  the line number of the task
     * @param type        the task type
     * @param warningText the text which should have been extracted from the source file
     */
    private void assertWarningExtraction(final TaskScannerAction tsa, String filename, Integer lineNumber,
            String type, String warningText) {
        final List<String> cellStrings = tsa.getCertainWarningsTabRow(filename + ":" + lineNumber);

        assertThat(cellStrings.get(3), is(type));
        assertThat(cellStrings.get(4), is(warningText));
    }

    @Override
    protected TaskScannerAction createProjectAction(final Job job) {
        return new TaskScannerAction(job);
    }

    @Override
    protected TaskScannerAction createResultAction(final Build build) {
        return new TaskScannerAction(build);
    }

    @Override
    protected FreeStyleJob createFreeStyleJob(final Container owner) {
        return createFreeStyleJob(owner, settings -> {
            settings.setPattern("**/*.java");
            settings.setExcludePattern("**/*Test.java");
            settings.setHighPriorityTags("FIXME");
            settings.setNormalPriorityTags("TODO");
            settings.setLowPriorityTags("@Deprecated");
            settings.setIgnoreCase(false);
        });
    }

    @Override
    protected WorkflowJob createPipeline() {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        String[] files = {"TSRBuildTimeoutPluginTest.java", "TSRCleaner.java", "TSRCreateSlaveTest.java",
                "TSRDockerImage.java", "TSREc2Provider.java", "TSRGitRepo.java",
                "TSRJenkinsAcceptanceTestRule.java", "TSRTestCleaner.java", "TSRWinstoneDockerController.java"};
        StringBuilder copyFilesWithTasks = new StringBuilder();
        for (String file : files) {
            copyFilesWithTasks.append(job.copyResourceStep(TASKS_FILES + "/" + file));
        }
        job.script.set("node {\n"
                + copyFilesWithTasks.toString()
                + "  step([$class: 'TasksPublisher', high: 'FIXME', normal: 'TODO', low: '@Deprecated',"
                + "excludePattern: '**/*Test.java'])\n}");
        job.sandbox.check();
        job.save();
        return job;
    }

    @Override
    protected int getNumberOfWarnings() {
        return 6;
    }

    @Override
    protected int getNumberOfHighPriorityWarnings() {
        return 1;
    }

    @Override
    protected int getNumberOfNormalPriorityWarnings() {
        return 4;
    }

    @Override
    protected int getNumberOfLowPriorityWarnings() {
        return 1;
    }

    private FreeStyleJob createFreeStyleJob() {
        return createFreeStyleJob(jenkins);
    }

    private FreeStyleJob createFreeStyleJob(final AnalysisConfigurator<TasksFreestyleSettings> buildConfigurator) {
        return createFreeStyleJob(TASKS_FILES, buildConfigurator);
    }

    private FreeStyleJob createFreeStyleJob(final Container owner,
            final AnalysisConfigurator<TasksFreestyleSettings> buildConfigurator) {
        return createFreeStyleJob(TASKS_FILES, owner, buildConfigurator);
    }

    private FreeStyleJob createFreeStyleJob(final String fileset,
            final AnalysisConfigurator<TasksFreestyleSettings> buildConfigurator) {
        return createFreeStyleJob(fileset, jenkins, buildConfigurator);
    }

    private FreeStyleJob createFreeStyleJob(final String fileset,
            final Container owner, final AnalysisConfigurator<TasksFreestyleSettings> buildConfigurator) {
        return createJob(owner, fileset, FreeStyleJob.class, TasksFreestyleSettings.class, buildConfigurator);
    }

    private MavenModuleSet createMavenJob(final String files,
            final AnalysisConfigurator<TasksMavenSettings> buildConfigurator) {
        return createMavenJob(files, NO_GOAL, buildConfigurator);
    }

    private MavenModuleSet createMavenJob(final String files, final String goal,
            final AnalysisConfigurator<TasksMavenSettings> buildConfigurator) {
        return createMavenJob(files, goal, TasksMavenSettings.class, buildConfigurator);
    }
}
