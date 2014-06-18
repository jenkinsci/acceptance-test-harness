package plugins;


import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jenkinsci.test.acceptance.junit.Bug;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction;
import org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;
import static org.junit.Assert.*;

/**
 Feature: Scan for open tasks
 In order to be able to collect and analyse open tasks.
 As a Jenkins user
 I want to install and configure Task Scanner plugin

  @author Martin Ende
 */
@WithPlugins("tasks")
public class TaskScannerPluginTest extends AbstractCodeStylePluginHelper{

    /**
     * This test's objective is to verify the basic functionality of the Task
     * Scanner plugin, i.e. finding different task tags, including / excluding
     * files and providing the correct results.
     * The test builds the same job twice with and without case sensitivity.
     */

    @Test
    public void single_task_tags_and_exclusion_pattern() throws Exception{
        //do basic setup
        FreeStyleJob j = setupFreestyleJob("/tasks_plugin/fileset1", "**/*.java", TaskScannerPublisher.class
        );

        //set up the some more task scanner settings
        j.configure();
        TaskScannerPublisher pub = j.getPublisher(TaskScannerPublisher.class);
        pub.excludePattern.set("**/*Test.java");
        pub.highPriorityTags.set("FIXME");
        pub.normalPriorityTags.set("TODO");
        pub.lowPriorityTags.set("@Deprecated");
        pub.ignoreCase.uncheck();

        j.save();

        // as no threshold is defined to mark the build as FAILED or UNSTABLE, the build should succeed
        Build lastBuild = buildJobWithSuccess(j);
        assertThat(lastBuild, hasAction("Open Tasks"));
        assertThat(j, hasAction("Open Tasks"));
        lastBuild.open();
        TaskScannerAction tsa = new TaskScannerAction(j);

        // The file set consists of 9 files, whereof
        //   - 2 file names match the exclusion pattern
        //   - 7 files are to be scanned for tasks
        //   - 5 files actually contain tasks with the specified tags (with case sensitivity)
        //
        // The expected task priorities are:
        //   - 1x high
        //   - 4x medium
        //   - 1x low

        assertThat(tsa.getResultLinkByXPathText("6 open tasks"), is("tasksResult"));
        assertThat(tsa.getResultTextByXPathText("6 open tasks"), endsWith("in 7 workspace files."));
        assertThat(tsa.getWarningNumber(), is(6));
        assertThat(tsa.getHighWarningNumber(), is(1));
        assertThat(tsa.getNormalWarningNumber(), is(4));
        assertThat(tsa.getLowWarningNumber(), is(1));

        assertFilesTabFS1E1(tsa);
        assertTypesTabFS1E1(tsa);
        assertWarningsTabFS1E1(tsa);

        // check the correct warning extraction for two examples:
        //  - TSRDockerImage.java:84 properly wait for either cidfile to appear or process to exit
        //  - TSRCleaner.java:40 @Deprecated without a text

        assertWarningExtraction(tsa,"TSRDockerImage.java",84,"TODO",
                                "properly wait for either cidfile to appear or process to exit");
        assertWarningExtraction(tsa,"TSRCleaner.java",40,"@Deprecated", "");

        //check that the correct line / task is displayed when following the link in the warnings tab
        //assert contents of that line
        assertThat(tsa.getLinkedSourceFileLineNumber("TSRDockerImage.java:84", "Normal Priority"), is(84));
        assertThat(tsa.getLinkedSourceFileLineAsString("TSRDockerImage.java:84", "Normal Priority"), containsString("TODO"));
        assertThat(tsa.getLinkedSourceFileLineAsString("TSRDockerImage.java:84", "Normal Priority"), endsWith("properly wait for either cidfile to appear or process to exit"));


        // now disable case sensitivity and build again. Then the publisher shall also
        // find the high priority task in Ec2Provider.java:133.

        j.configure();
        pub.ignoreCase.check();
        j.save();

        lastBuild = buildJobWithSuccess(j);

        lastBuild.open();
        assertThat(tsa.getResultLinkByXPathText("7 open tasks"), is("tasksResult"));
        assertThat(tsa.getResultTextByXPathText("7 open tasks"), endsWith("in 7 workspace files."));
        assertThat(tsa.getResultLinkByXPathText("1 new open task"), is("tasksResult/new"));
        assertThat(tsa.getWarningNumber(), is(7));
        assertThat(tsa.getNewWarningNumber(), is(1));
        assertThat(tsa.getHighWarningNumber(), is(2));
        assertThat(tsa.getNormalWarningNumber(), is(4));
        assertThat(tsa.getLowWarningNumber(), is(1));

        lastBuild.visit(tsa.getNewWarningsUrlAsRelativePath());
        assertThat(tsa.getResultLinkByXPathText("TSREc2Provider.java:133"), startsWith("source"));
    }




    /**
     * Builds a job and tests if the tasks api (with depth=0 parameter set) responds with the expected output.
     * Difference in whitespaces are ok.
     */
    @Test
    public void xml_api_report_depth_0() throws IOException, SAXException, ParserConfigurationException {
        //do the same setup as in test single_task_tags_and_exclusion_pattern
        FreeStyleJob j = setupFreestyleJob("/tasks_plugin/fileset1", "**/*.java", TaskScannerPublisher.class
        );

        //set up the some more task scanner settings
        j.configure();
        TaskScannerPublisher pub = j.getPublisher(TaskScannerPublisher.class);
        pub.excludePattern.set("**/*Test.java");
        pub.highPriorityTags.set("FIXME");
        pub.normalPriorityTags.set("TODO");
        pub.lowPriorityTags.set("@Deprecated");
        pub.ignoreCase.uncheck();

        j.save();

        Build build = buildJobWithSuccess(j);

        final String apiUrl = "tasksResult/api/xml?depth=0";
        final String expectedXmlPath = "/tasks_plugin/api_depth_0.xml";
        assertXmlApiMatchesExpected(build, apiUrl, expectedXmlPath);
    }

    /**
     * This test's objective is to verify that the plugin correctly works for
     * multiple tags per priority.
     * In the first step the task scanner is configured with two tags for high
     * priority tasks. Prior to the second build also the normal and low priority
     * tag list is extended.
     */
    @Test
    public void multiple_task_tags() throws Exception{
        //do basic setup
        FreeStyleJob j = setupFreestyleJob("/tasks_plugin/fileset1", "**/*.java", TaskScannerPublisher.class
        );

        //set up the some more task scanner settings
        j.configure();
        TaskScannerPublisher pub = j.getPublisher(TaskScannerPublisher.class);
        pub.excludePattern.set("**/*Test.java");
        pub.highPriorityTags.set("FIXME,BUG");
        pub.normalPriorityTags.set("TODO");
        pub.lowPriorityTags.set("@Deprecated");
        pub.ignoreCase.check();

        j.save();

        // as no threshold is defined to mark the build as FAILED or UNSTABLE, the build should succeed
        Build lastBuild = buildJobWithSuccess(j);
        lastBuild.open();
        TaskScannerAction tsa = new TaskScannerAction(j);

        // The file set consists of 9 files, whereof
        //   - 2 file names match the exclusion pattern
        //   - 7 files are to be scanned for tasks
        //   - 6 files actually contain tasks with the specified tags
        //
        // The expected task priorities are:
        //   - 3x high
        //   - 4x medium
        //   - 1x low

        assertThat(tsa.getResultLinkByXPathText("8 open tasks"), is("tasksResult"));
        assertThat(tsa.getResultTextByXPathText("8 open tasks"), endsWith("in 7 workspace files."));
        assertThat(tsa.getWarningNumber(), is(8));
        assertThat(tsa.getHighWarningNumber(), is(3));
        assertThat(tsa.getNormalWarningNumber(), is(4));
        assertThat(tsa.getLowWarningNumber(), is(1));

        // now add further task tags. Then the publisher shall also
        // find the second priority task in TSRDockerImage.java (line 102) amd
        // a low priority task in TSRDockerImage.java (line 56).

        j.configure();
        pub.normalPriorityTags.set("TODO,XXX");
        pub.lowPriorityTags.set("@Deprecated,\\?\\?\\?");
        j.save();

        lastBuild = buildJobWithSuccess(j);

        lastBuild.open();
        assertThat(tsa.getResultLinkByXPathText("10 open tasks"), is("tasksResult"));
        assertThat(tsa.getResultTextByXPathText("10 open tasks"), endsWith("in 7 workspace files."));
        assertThat(tsa.getResultLinkByXPathText("2 new open tasks"), is("tasksResult/new"));
        assertThat(tsa.getWarningNumber(), is(10));
        assertThat(tsa.getNewWarningNumber(), is(2));
        assertThat(tsa.getHighWarningNumber(), is(3));
        assertThat(tsa.getNormalWarningNumber(), is(5));
        assertThat(tsa.getLowWarningNumber(), is(2));

        assertFilesTabFS1E2(tsa);
        assertTypesTabFS1E2(tsa);
        assertWarningsTabFS1E2(tsa);

    }


    /**
     * This test's objective is to verify the detection of closed tasks.
     * Therefore two runs of the same job with the same task scanner setup are
     * conducted but the fileset in the workspace will be replaced by the same
     * files containing less warnings for the second run.
     * The tasks that have been removed shall be correctly listed as closed tasks.
     */

    @Test
    public void closed_tasks() throws Exception {
        //do the same setup as for single_task_tags_and_exclusion_pattern
        FreeStyleJob j = setupFreestyleJob("/tasks_plugin/fileset1", "**/*.java", TaskScannerPublisher.class
        );

        j.configure();
        TaskScannerPublisher pub = j.getPublisher(TaskScannerPublisher.class);
        pub.excludePattern.set("**/*Test.java");
        pub.highPriorityTags.set("FIXME");
        pub.normalPriorityTags.set("TODO");
        pub.lowPriorityTags.set("@Deprecated");
        pub.ignoreCase.uncheck();

        j.save();

        // as no threshold is defined to mark the build as FAILED or UNSTABLE, the build should succeed
        buildJobWithSuccess(j);

        // this time we do not check the task scanner output as the result is the same
        // as for single_task_tags_and_exclusion_pattern
        // So we proceed directly with the preparation of build #2

        j.configure();
        j.removeFirstBuildStep(); //removes the build step previously created by copyDir
        j.copyDir(resource("/tasks_plugin/fileset1_less"));
        j.save();

        Build lastBuild = buildJobWithSuccess(j);
        lastBuild.open();
        TaskScannerAction tsa = new TaskScannerAction(j);

        // In the first build the task priorities were
        //   - 1x high
        //   - 4x medium
        //   - 1x low
        //
        // For the second build (reduced warnings) the expected priorities are
        //   - 3x medium
        //
        // --> we expect 3 closed tasks (1x high, 1x normal, 1x low)

        assertThat(tsa.getResultLinkByXPathText("3 open tasks"), is("tasksResult"));
        assertThat(tsa.getResultTextByXPathText("3 open tasks"), endsWith("in 7 workspace files."));
        assertThat(tsa.getResultLinkByXPathText("3 closed tasks"), is("tasksResult/fixed"));
        assertThat(tsa.getWarningNumber(), is(3));
        assertThat(tsa.getFixedWarningNumber(), is(3));
        assertThat(tsa.getHighWarningNumber(), is(0));
        assertThat(tsa.getNormalWarningNumber(), is(3));
        assertThat(tsa.getLowWarningNumber(), is(0));

        assertFixedTab(tsa);
    }

    /**
     * This test's objective is to check the "Run always" option of the publisher,
     * i.e whether the task scanner activity is skipped in case the main build step
     * has already failed and the option "run always" is not activated. The option
     * is activated for the second part to also scan for tasks in this failed job
     */

    @Test
    public void run_always_option() throws Exception {
        //do the same setup as for single_task_tags_and_exclusion_pattern
        FreeStyleJob j = setupFreestyleJob("/tasks_plugin/fileset1", "**/*.java", TaskScannerPublisher.class
        );

        j.configure();
        j.addShellStep("exit 1"); //ensures the FAILURE status of the main build
        TaskScannerPublisher pub = j.getPublisher(TaskScannerPublisher.class);
        pub.excludePattern.set("**/*Test.java");
        pub.highPriorityTags.set("FIXME");
        pub.ignoreCase.check();
        pub.advanced.click();
        pub.runAlways.uncheck();
        j.save();

        // due to the "exit 1" shell step, the build fails
        Build lastBuild = j.startBuild().shouldFail();

        // the task scanner activity shall be skipped due to the failed main build
        // so we have to search for the particular console output
        lastBuild.shouldContainsConsoleOutput(".*\\[TASKS\\] Skipping publisher since build result is FAILURE");

        // now activate "Run always"
        j.configure();
        pub.advanced.click();
        pub.runAlways.check();
        j.save();

        lastBuild = j.startBuild().shouldFail();
        lastBuild.open();

        TaskScannerAction tsa = new TaskScannerAction(j);

        // as the failed result is now ignored, we expect 2 open tasks, both
        // of high priority and both considered as new warnings.
        assertThat(tsa.getResultLinkByXPathText("2 open tasks"), is("tasksResult"));
        assertThat(tsa.getResultTextByXPathText("2 open tasks"), endsWith("in 7 workspace files."));
        assertThat(tsa.getResultLinkByXPathText("2 new open tasks"), is("tasksResult/new"));
        assertThat(tsa.getWarningNumber(), is(2));
        assertThat(tsa.getHighWarningNumber(), is(2));
    }

    /**
     * This test's objective to check the correct treatment and display of tasks
     * in files with windows-1251 (a.k.a. cp1251) encoding.
     *
     * This test shall reproduce the observations described in JENKINS-22744:
     * https://issues.jenkins-ci.org/browse/JENKINS-22744
     *
     */

    @Test @Bug("22744") @Ignore("until JENKINS-22744 is fixed.")
    public void file_encoding_windows1251() throws Exception {
        //basic setup
        FreeStyleJob j = setupFreestyleJob("/tasks_plugin/cp1251_files", "**/*.java", TaskScannerPublisher.class
        );
        j.configure();
        TaskScannerPublisher pub = j.getPublisher(TaskScannerPublisher.class);
        pub.normalPriorityTags.set("TODO");
        pub.highPriorityTags.set("FIXME");
        pub.ignoreCase.check();
        pub.advanced.click();
        pub.defaultEncoding.set("windows-1251");

        j.save();

        Build lastBuild = buildJobWithSuccess(j);
        assertThat(lastBuild, hasAction("Open Tasks"));
        assertThat(j, hasAction("Open Tasks"));
        lastBuild.open();
        TaskScannerAction tsa = new TaskScannerAction(j);

        // The expected task priorities are:
        //   - 1x high
        //   - 1x medium

        assertThat(tsa.getResultLinkByXPathText("2 open tasks"), is("tasksResult"));
        assertThat(tsa.getResultTextByXPathText("2 open tasks"), endsWith("in 1 workspace file."));
        assertThat(tsa.getWarningNumber(), is(2));
        assertThat(tsa.getHighWarningNumber(), is(1));
        assertThat(tsa.getNormalWarningNumber(), is(1));

        // verify source code display in desired encoding
        assertThat(tsa.getLinkedSourceFileLineAsString("TestTaskScanner.java:5", "Normal Priority"), endsWith("пример комментария на русском"));

        // verify extraction in Warnings tab uses desired encoding
        assertWarningExtraction(tsa,"TestTaskScanner.java",5,"TODO","пример комментария на русском");

    }


    /**
     * This test's objective is to the correct treatment of the status thresholds (totals).
     * Therefore a more complex test case has been created which modifies files and task tags
     * to scan for multiple times to create appropriate scenarios for different thresholds.
     *
     * It shall also check whether the determination / justification of the build status is
     * done based on the highest priority threshold that has been exceeded.
     *
     * The test case consists of 6 steps:
     * 1 - create reference build (SUCCESS) -> no threshold exceeded
     * 2 - exceed the UNSTABLE LOW threshold
     * 3 - exceed the UNSTABLE NORMAL threshold but do not exceed UNSTABLE LOW
     * 4 - exceed UNSTABLE LOW, NORMAL and HIGH -> build status justified with HIGH priority tasks
     * 5 - further exceed the UNSTABLE TOTAL threshold -> new justification for build status
     * 6 - further exceed the FAILURE TOTAL threshold -> build failed
     * 7 - remove most of the task tags -> build is stable again
     *
     */

    @Test
    public void status_thresholds() throws Exception {

        // Basic setup

        FreeStyleJob j = setupFreestyleJob("/tasks_plugin/fileset1_less", "**/*.java", TaskScannerPublisher.class
        );

        TaskScannerPublisher pub = j.getPublisher(TaskScannerPublisher.class);
        TaskScannerAction tsa = new TaskScannerAction(j);

        // In order to increase readability each step has been placed in a separate
        // private function

        j = status_thresholds_step1(j, pub, tsa);
        j = status_thresholds_step2(j, pub, tsa);
        j = status_thresholds_step3(j, pub, tsa);
        j = status_thresholds_step4(j, pub, tsa);
        j = status_thresholds_step5(j, pub, tsa);
        j = status_thresholds_step6(j, pub, tsa);
        status_thresholds_step7(j, tsa);


    }

    /**
     * This method does special configurations for test step 1 of test
     * {@link TaskScannerPluginTest#status_thresholds()}. The scenario is that the file set
     * consists of 9 files, whereof
     *   - 7 files are to be scanned for tasks
     *
     * The expected task priorities are:
     *   - 0x high
     *   - 3x medium
     *   - 0x low
     *
     * So, the build status shall be SUCCESS as no threshold will be exceeded.     *
     *
     * @param j the {@link org.jenkinsci.test.acceptance.po.FreeStyleJob} created in the Test
     * @param pub a {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerPublisher} added to the Job
     * @param tsa a the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the current job
     *
     * @return The modified {@link org.jenkinsci.test.acceptance.po.FreeStyleJob}.
     */

    private FreeStyleJob status_thresholds_step1(FreeStyleJob j, TaskScannerPublisher pub, TaskScannerAction tsa){
        j.configure();
        pub.excludePattern.set("**/*Test.java");
        pub.highPriorityTags.set(""); //no high prio tags
        pub.normalPriorityTags.set("TODO");
        pub.lowPriorityTags.set("@Deprecated");
        pub.ignoreCase.uncheck();

        // setup thresholds
        pub.advanced.click();
        pub.warningThresholdLowUnstable.set("1");
        pub.warningThresholdNormalUnstable.set("4");
        pub.warningThresholdHighUnstable.set("0");
        pub.warningThresholdUnstable.set("10");
        pub.warningThresholdFailed.set("15");

        j.save();

        final Build lastBuild = buildJobWithSuccess(j);
        lastBuild.open();

        assertThat(tsa.getResultTextByXPathText("3 open tasks"), endsWith("in 7 workspace files."));
        assertThat(tsa.getResultLinkByXPathText("3 new open tasks"), is("tasksResult/new"));
        assertThat(tsa.getWarningNumber(), is(3));

        // Note:
        //   high warning is omitted in summary table because no high prio tag is defined.

        assertThat(tsa.getNormalWarningNumber(), is(3));
        assertThat(tsa.getLowWarningNumber(), is(0));
        assertThat(tsa.getPluginResult(lastBuild), is("Plug-in Result: SUCCESS - no threshold has been exceeded"));

        return j;
    }

    /**
     * This method does special configurations for test step 2 of test
     * {@link TaskScannerPluginTest#status_thresholds()}. The scenario is that the file set
     * consists of 9 files, whereof
     *   - 7 files are to be scanned for tasks
     *
     * The expected task priorities are:
     *   - 0x high
     *   - 4x medium
     *   - 2x low
     *
     * So, the build status shall be UNSTABLE due to low priority threshold is exceeded by 1.
     *
     * @param j the {@link org.jenkinsci.test.acceptance.po.FreeStyleJob} created in the Test
     * @param pub a {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerPublisher} added to the Job
     * @param tsa a the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the current job
     *
     * @return The modified {@link org.jenkinsci.test.acceptance.po.FreeStyleJob}.
     */

    private FreeStyleJob status_thresholds_step2(FreeStyleJob j, TaskScannerPublisher pub, TaskScannerAction tsa){
        j.configure();
        j.removeFirstBuildStep(); // remove copyDir shell step for fileset1_less
        j.copyDir(resource("/tasks_plugin/fileset1"));
        pub.lowPriorityTags.set("@Deprecated,\\?\\?\\?"); // add tag "???"
        j.save();

        final Build lastBuild = j.startBuild().shouldBeUnstable();
        lastBuild.open();

        assertThat(tsa.getResultTextByXPathText("6 open tasks"), endsWith("in 7 workspace files."));
        assertThat(tsa.getResultLinkByXPathText("3 new open tasks"), is("tasksResult/new"));
        assertThat(tsa.getWarningNumber(), is(6));
        assertThat(tsa.getNewWarningNumber(), is(3));
        assertThat(tsa.getNormalWarningNumber(), is(4));
        assertThat(tsa.getLowWarningNumber(), is(2));
        assertThat(tsa.getPluginResult(lastBuild),
                is("Plug-in Result: UNSTABLE - 2 warnings of priority Low Priority exceed the threshold of 1 by 1 (Reference build: #1)"));

        return j;
    }

    /**
     * This method does special configurations for test step 3 of test
     * {@link TaskScannerPluginTest#status_thresholds()}. The scenario is that the file set
     * consists of 9 files, whereof
     *   - 7 files are to be scanned for tasks
     *
     * The expected task priorities are:
     *   - 0x high
     *   - 5x medium
     *   - 1x low
     *
     * So, the build status shall be UNSTABLE due to normal priority threshold is exceeded by 1.
     *
     * @param j the {@link org.jenkinsci.test.acceptance.po.FreeStyleJob} created in the Test
     * @param pub a {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerPublisher} added to the Job
     * @param tsa a the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the current job
     *
     * @return The modified {@link org.jenkinsci.test.acceptance.po.FreeStyleJob}.
     */

    private FreeStyleJob status_thresholds_step3(FreeStyleJob j, TaskScannerPublisher pub, TaskScannerAction tsa){
        j.configure();
        pub.lowPriorityTags.set("@Deprecated"); // remove tag "???"
        pub.normalPriorityTags.set("TODO,XXX"); // add tag "XXX"
        j.save();

        final Build lastBuild = j.startBuild().shouldBeUnstable();
        lastBuild.open();

        assertThat(tsa.getResultTextByXPathText("6 open tasks"), endsWith("in 7 workspace files."));

        // Note:
        //   As the previous build was unstable the determination which warnings have changed is
        //   done based on the reference buil (#1)!!
        //   The same applies to step 4 to 6

        assertThat(tsa.getResultLinkByXPathText("3 new open tasks"), is("tasksResult/new"));
        assertThat(tsa.getWarningNumber(), is(6));
        assertThat(tsa.getNewWarningNumber(), is(3));
        assertThat(tsa.getNormalWarningNumber(), is(5));
        assertThat(tsa.getLowWarningNumber(), is(1));
        assertThat(tsa.getPluginResult(lastBuild),
                is("Plug-in Result: UNSTABLE - 5 warnings of priority Normal Priority exceed the threshold of 4 by 1 (Reference build: #1)"));

        return j;
    }

    /**
     * This method does special configurations for test step 4 of test
     * {@link TaskScannerPluginTest#status_thresholds()}. The scenario is that the file set
     * consists of 9 files, whereof
     *   - 7 files are to be scanned for tasks
     *
     * The expected task priorities are:
     *   - 1x high
     *   - 5x medium
     *   - 2x low
     *
     * So, the build status shall be UNSTABLE due to high priority threshold is exceeded by 1.
     *
     * @param j the {@link org.jenkinsci.test.acceptance.po.FreeStyleJob} created in the Test
     * @param pub a {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerPublisher} added to the Job
     * @param tsa a the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the current job
     *
     * @return The modified {@link org.jenkinsci.test.acceptance.po.FreeStyleJob}.
     */

    private FreeStyleJob status_thresholds_step4(FreeStyleJob j, TaskScannerPublisher pub, TaskScannerAction tsa){
        j.configure();
        pub.lowPriorityTags.set("@Deprecated,\\?\\?\\?"); // add tag "???"
        pub.highPriorityTags.set("FIXME"); // add tag "FIXME"
        j.save();

        final Build lastBuild = j.startBuild().shouldBeUnstable();
        lastBuild.open();

        assertThat(tsa.getResultTextByXPathText("8 open tasks"), endsWith("in 7 workspace files."));
        assertThat(tsa.getResultLinkByXPathText("5 new open tasks"), is("tasksResult/new"));
        assertThat(tsa.getWarningNumber(), is(8));
        assertThat(tsa.getNewWarningNumber(), is(5));
        assertThat(tsa.getHighWarningNumber(), is(1));
        assertThat(tsa.getNormalWarningNumber(), is(5));
        assertThat(tsa.getLowWarningNumber(), is(2));
        assertThat(tsa.getPluginResult(lastBuild),
                is("Plug-in Result: UNSTABLE - 1 warning of priority High Priority exceeds the threshold of 0 by 1 (Reference build: #1)"));

        return j;
    }

    /**
     * This method does special configurations for test step 5 of test
     * {@link TaskScannerPluginTest#status_thresholds()}. The scenario is that the file set
     * consists of 19 files, whereof
     *   - 17 files are to be scanned for tasks
     *
     * The expected task priorities are:
     *   -  1x high
     *   - 11x medium
     *   -  3x low
     *
     * So, the build status shall be UNSTABLE due to total warnings threshold is exceeded by 5.
     *
     * @param j the {@link org.jenkinsci.test.acceptance.po.FreeStyleJob} created in the Test
     * @param pub a {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerPublisher} added to the Job
     * @param tsa a the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the current job
     *
     * @return The modified {@link org.jenkinsci.test.acceptance.po.FreeStyleJob}.
     */

    private FreeStyleJob status_thresholds_step5(FreeStyleJob j, TaskScannerPublisher pub, TaskScannerAction tsa){
        j.configure();
        j.copyDir(resource("/tasks_plugin/fileset2")); // add a second shell step to copy another folder
        pub.normalPriorityTags.set("TODO"); //remove tag "XXX"
        j.save();

        final Build lastBuild = j.startBuild().shouldBeUnstable();
        lastBuild.open();

        assertThat(tsa.getResultTextByXPathText("15 open tasks"), endsWith("in 17 workspace files."));
        assertThat(tsa.getResultLinkByXPathText("12 new open tasks"), is("tasksResult/new"));

        assertThat(tsa.getWarningNumber(), is(15));
        assertThat(tsa.getNewWarningNumber(), is(12));
        assertThat(tsa.getHighWarningNumber(), is(1));
        assertThat(tsa.getNormalWarningNumber(), is(11));
        assertThat(tsa.getLowWarningNumber(), is(3));
        assertThat(tsa.getPluginResult(lastBuild),
                is("Plug-in Result: UNSTABLE - 15 warnings exceed the threshold of 10 by 5 (Reference build: #1)"));

        return j;
    }

    /**
     * This method does special configurations for test step 6 of test
     * {@link TaskScannerPluginTest#status_thresholds()}. The scenario is that the file set
     * consists of 19 files, whereof
     *   - 17 files are to be scanned for tasks
     *
     * The expected task priorities are:
     *   -  2x high
     *   - 11x medium
     *   -  3x low
     *
     * So, the build status shall be FAILED due to total warnings threshold is exceeded by 1.
     *
     * @param j the {@link org.jenkinsci.test.acceptance.po.FreeStyleJob} created in the Test
     * @param pub a {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerPublisher} added to the Job
     * @param tsa a the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the current job
     *
     * @return The modified {@link org.jenkinsci.test.acceptance.po.FreeStyleJob}.
     */

    private FreeStyleJob status_thresholds_step6(FreeStyleJob j, TaskScannerPublisher pub, TaskScannerAction tsa){
        j.configure();
        pub.ignoreCase.check(); //disable case sensitivity
        j.save();

        final Build lastBuild = j.startBuild().shouldFail();
        lastBuild.open();

        assertThat(tsa.getResultTextByXPathText("16 open tasks"), endsWith("in 17 workspace files."));
        assertThat(tsa.getResultLinkByXPathText("13 new open tasks"), is("tasksResult/new"));
        assertThat(tsa.getWarningNumber(), is(16));
        assertThat(tsa.getNewWarningNumber(), is(13));
        assertThat(tsa.getHighWarningNumber(), is(2));
        assertThat(tsa.getNormalWarningNumber(), is(11));
        assertThat(tsa.getLowWarningNumber(), is(3));
        assertThat(tsa.getPluginResult(lastBuild),
                is("Plug-in Result: FAILED - 16 warnings exceed the threshold of 15 by 1 (Reference build: #1)"));

        return j;
    }

    /**
     * This method does special configurations for test step 6 of test
     * {@link TaskScannerPluginTest#status_thresholds()}. Another shell step is added which
     * consists of a small script to replace all todo, fixme, xxx, deprecated occurences in the
     * workspace files by the string "CLOSED".
     *
     * The scenario is that the file set consists of 19 files, whereof
     *   - 17 files are to be scanned for tasks
     *
     * The expected task priorities are:
     *   -  0x high
     *   -  0x medium
     *   -  1x low
     *
     * So, the build status shall be SUCCESS as no threshold will be exceeded.
     *
     * @param j the {@link org.jenkinsci.test.acceptance.po.FreeStyleJob} created in the Test
     * @param tsa a the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for the current job
     *
     * @return The modified {@link org.jenkinsci.test.acceptance.po.FreeStyleJob}.
     */

    private FreeStyleJob status_thresholds_step7(FreeStyleJob j, TaskScannerAction tsa){
        j.configure();
        j.addShellStep( "NEW=\"CLOSED\"\n" +
                        "for t in \"todo\" \"xxx\" \"fixme\" \"deprecated\"\n" +
                        "do\n" +
                        "  OLD=$t\n" +
                        "  for f in `ls`\n" +
                        "  do\n" +
                        "    if [ -f $f -a -r $f ]; then\n" +
                        "      sed \"s/$OLD/$NEW/I\" \"$f\" > \"${f}.new\"\n" +
                        "      mv \"${f}.new\" \"$f\"\n" +
                        "    else\n" +
                        "      echo \"Error: Cannot read $f\"\n" +
                        "    fi\n" +
                        "  done\n" +
                        "done");
        j.save();

        final Build lastBuild = buildJobWithSuccess(j);
        lastBuild.open();

        assertThat(tsa.getResultTextByXPathText("1 open task"), endsWith("in 17 workspace files."));
        assertThat(tsa.getResultLinkByXPathText("1 new open task"), is("tasksResult/new"));
        assertThat(tsa.getResultLinkByXPathText("3 closed tasks"), is("tasksResult/fixed"));
        assertThat(tsa.getWarningNumber(), is(1));
        assertThat(tsa.getNewWarningNumber(), is(1));
        assertThat(tsa.getFixedWarningNumber(), is(3));
        assertThat(tsa.getHighWarningNumber(), is(0));
        assertThat(tsa.getNormalWarningNumber(), is(0));
        assertThat(tsa.getLowWarningNumber(), is(1));
        assertThat(tsa.getPluginResult(lastBuild),
                is("Plug-in Result: SUCCESS - no threshold has been exceeded (Reference build: #1)"));

        return j;
    }

    /**
     * This method asserts the correct content of the files tab for the files in fileset 1
     * with the TaskScanner scanning for FIXME, TODO and @Deprecated (case sensitive).
     +
     *  @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for
     *            the current job
     */
    private void assertFilesTabFS1E1(TaskScannerAction tsa){
        SortedMap<String, Integer> expectedContent = new TreeMap<>();

        expectedContent.put("TSRCleaner.java", 1);
        expectedContent.put("TSRDockerImage.java", 1);
        expectedContent.put("TSRGitRepo.java", 2);
        expectedContent.put("TSRJenkinsAcceptanceTestRule.java", 1);
        expectedContent.put("TSRWinstoneDockerController.java", 1);

        assertThat(tsa.getFileTabContents(), is(expectedContent));
    }

    /**
     * This method asserts the correct content of the files tab for the files in fileset 1
     * with the TaskScanner scanning for FIXME, BUG, TODO, XXX, @Deprecated and ??? (not case sensitive).
     +
     *  @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for
     *            the current job
     */
    private void assertFilesTabFS1E2(TaskScannerAction tsa){
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
     * This method asserts the correct content of the Types tab for the files in fileset 1
     * with the TaskScanner scanning for FIXME, TODO and @Deprecated (case sensitive).
     +
     *  @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for
     *            the current job
     */
    private void assertTypesTabFS1E1(TaskScannerAction tsa){
        SortedMap<String, Integer> expectedContent = new TreeMap<>();

        expectedContent.put("@Deprecated", 1);
        expectedContent.put("FIXME", 1);
        expectedContent.put("TODO", 4);

        assertThat(tsa.getTypesTabContents(), is(expectedContent));
    }

    /**
     * This method asserts the correct content of the Types tab for the files in fileset 1
     * with the TaskScanner scanning for FIXME, BUG, TODO, XXX, @Deprecated and ??? (not case sensitive).
     +
     *  @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for
     *            the current job
     */
    private void assertTypesTabFS1E2(TaskScannerAction tsa){
        SortedMap<String, Integer> expectedContent = new TreeMap<>();

        expectedContent.put("@Deprecated", 1);
        expectedContent.put("FIXME", 1);
        expectedContent.put("fixme", 1);
        expectedContent.put("TODO", 4);
        expectedContent.put("BUG", 1);
        expectedContent.put("XXX", 1);
        expectedContent.put("???", 1);

        assertThat(tsa.getTypesTabContents(), is(expectedContent));
    }

    /**
     * This method asserts the correct content of the Warnings tab for the files in fileset 1
     * with the TaskScanner scanning for FIXME, TODO and @Deprecated (case sensitive).
     +
     *  @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for
     *            the current job
     */
    private void assertWarningsTabFS1E1(TaskScannerAction tsa){
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
     * This method asserts the correct content of the Warnings tab for the files in fileset 1
     * with the TaskScanner scanning for FIXME, BUG, TODO, XXX, @Deprecated and ??? (not case sensitive).
     +
     *  @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for
     *            the current job
     */
    private void assertWarningsTabFS1E2(TaskScannerAction tsa){
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
     * This method asserts the correct content of the Fixed tab for the files in fileset_1_less
     * with the TaskScanner scanning for FIXME, TODO, and @Deprecated (case sensitive).
     *
     * @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for
     *            the current job
     */
    private void assertFixedTab(TaskScannerAction tsa) {
        SortedMap<String, String> expectedContent = new TreeMap<>();

        expectedContent.put("TSRCleaner.java", "@Deprecated");
        expectedContent.put("TSRDockerImage.java", "TODO");
        expectedContent.put("TSRGitRepo.java", "FIXME");

        assertThat(tsa.getFixedTabContents(), is(expectedContent));
    }

    /**
     * This method asserts that a certain task is contained in the table shown in
     * the "Warnings"-tab with the correct task type and text
     *
     * @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for
     *            the current job
     * @param filename the name of the source file containing the task
     * @param lineNumber the line number of the task
     * @param type the task type
     * @param warningText the text which should have been extracted from the source file
     */
    private void assertWarningExtraction(final TaskScannerAction tsa, String filename, Integer lineNumber,
                                         String type, String warningText){
        final List<String> cellStrings = tsa.getCertainWarningsTabRow(filename + ":" + lineNumber);

        assertThat(cellStrings.get(3), is(type));
        assertThat(cellStrings.get(4), is(warningText));

    }

}
