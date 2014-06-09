package plugins;


import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindbugsPublisher;
import org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction;
import org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 Feature: Scan for open tasks
 In order to be able to collect and analyse open tasks.
 As a Jenkins user
 I want to install and configure Task Scanner plugin

  @author Martin Ende
 */
@WithPlugins("tasks")
public class TaskScannerPluginTest extends AbstractCodeStylePluginHelper{

    //TODO: Test for JENKINS-22744: https://issues.jenkins-ci.org/browse/JENKINS-22744


    /**
     * This test's objective is to verify the basic functionality of the Task
     * Scanner plugin, i.e. finding different task tags, including / excluding
     * files and providing the correct results.
     * The test builds the same job twice with and without case sensitivity.
     */

    @Test
    public void single_task_tags_and_exclusion_pattern() throws Exception{
        //do basic setup
        FreeStyleJob j = setupJob("/tasks_plugin/fileset1",TaskScannerPublisher.class,
                                  "**/*.java");

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

        assertFilesTab(tsa, "fileset1_eval1");
        assertTypesTab(tsa, "fileset1_eval1");
        assertWarningsTab(tsa, "fileset1_eval1");

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
        FreeStyleJob j = setupJob("/tasks_plugin/fileset1",TaskScannerPublisher.class,
                "**/*.java");

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
        FreeStyleJob j = setupJob("/tasks_plugin/fileset1",TaskScannerPublisher.class,
                "**/*.java");

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

        assertFilesTab(tsa, "fileset1_eval2");
        assertTypesTab(tsa, "fileset1_eval2");
        assertWarningsTab(tsa, "fileset1_eval2");

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
        FreeStyleJob j = setupJob("/tasks_plugin/fileset1", TaskScannerPublisher.class,
                "**/*.java");

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

        // this time we do not check the task scanner output as the result is the same
        // as for single_task_tags_and_exclusion_pattern
        // So we proceed directly with the preparation of build #2

        j.configure();
        j.removeFirstBuildStep(); //removes the build step previously created by copyDir
        j.copyDir(resource("/tasks_plugin/fileset1_less"));
        j.save();

        lastBuild = buildJobWithSuccess(j);
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

        assertFixedTab(tsa, "fileset1_less");
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
        FreeStyleJob j = setupJob("/tasks_plugin/fileset1", TaskScannerPublisher.class,
                "**/*.java");

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
     * This method asserts the correct content of the files tab
     * depending on the file set loaded to the workspace and the
     * task tags used.
     *
     * Supported assertions:
     *  - fileset1_eval1 = fileset1, tags: FIXME, TODO, @Deprecated, case sensitive
     *  - fileset1_eval2 = fileset1, tags: FIXME, BUG, TODO, XXX, @Deprecated, ???, not case sensitive
     *
     * @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for
     *            the current job
     * @param expectedList determines which files and which warning counts are expected
     */
    private void assertFilesTab(TaskScannerAction tsa, String expectedList){
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        // TODO: extend for all filesets
        switch (expectedList){
            case "fileset1_eval1":
                expectedContent.put("TSRCleaner.java", 1);
                expectedContent.put("TSRDockerImage.java", 1);
                expectedContent.put("TSRGitRepo.java", 2);
                expectedContent.put("TSRJenkinsAcceptanceTestRule.java", 1);
                expectedContent.put("TSRWinstoneDockerController.java", 1);
                break;
            case "fileset1_eval2":
                expectedContent.put("TSRCleaner.java", 1);
                expectedContent.put("TSRDockerImage.java", 3);
                expectedContent.put("TSRGitRepo.java", 3);
                expectedContent.put("TSREc2Provider.java", 1);
                expectedContent.put("TSRJenkinsAcceptanceTestRule.java", 1);
                expectedContent.put("TSRWinstoneDockerController.java", 1);
                break;
            default:
                fail("invalid expectedList value");
        }

        assertThat(tsa.getFileTabContents(), is(expectedContent));
    }

    /**
     * This method asserts the correct content of the Types tab
     * depending on the file set loaded to the workspace and the
     * task tags used.
     *
     * Supported assertions:
     *  - fileset1_eval1 = fileset1, tags: FIXME, TODO, @Deprecated, case sensitive
     *  - fileset1_eval2 = fileset1, tags: FIXME, BUG, TODO, XXX, @Deprecated, ???, not case sensitive
     *
     * @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for
     *            the current job
     * @param expectedList determines which files and which warning counts are expected
     */
    private void assertTypesTab(TaskScannerAction tsa, String expectedList){
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        // TODO: extend for all filesets
        switch (expectedList){
            case "fileset1_eval1":
                expectedContent.put("@Deprecated", 1);
                expectedContent.put("FIXME", 1);
                expectedContent.put("TODO", 4);
                break;
            case "fileset1_eval2":
                expectedContent.put("@Deprecated", 1);
                expectedContent.put("FIXME", 1);
                expectedContent.put("fixme", 1);
                expectedContent.put("TODO", 4);
                expectedContent.put("BUG", 1);
                expectedContent.put("XXX", 1);
                expectedContent.put("???", 1);
                break;
            default:
                fail("invalid expectedList value");
        }

        assertThat(tsa.getTypesTabContents(), is(expectedContent));
    }

    /**
     * This method asserts the correct content of the Warnings tab
     * depending on the file set loaded to the workspace and the
     * task tags used.
     *
     * Supported assertions:
     *  - fileset1_eval1 = fileset1, tags: FIXME, TODO, @Deprecated, case sensitive
     *  - fileset1_eval2 = fileset1, tags: FIXME, BUG, TODO, XXX, @Deprecated, ???, not case sensitive
     *
     * @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for
     *            the current job
     * @param expectedList determines which files and lines are expected
     */
    private void assertWarningsTab(TaskScannerAction tsa, String expectedList){
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        // TODO: extend for all filesets
        switch (expectedList){
            case "fileset1_eval1":
                expectedContent.put("TSRGitRepo.java:38", 38);
                expectedContent.put("TSRGitRepo.java:69", 69);
                expectedContent.put("TSRDockerImage.java:84", 84);
                expectedContent.put("TSRJenkinsAcceptanceTestRule.java:51", 51);
                expectedContent.put("TSRWinstoneDockerController.java:73", 73);
                expectedContent.put("TSRCleaner.java:40", 40);
                break;
            case "fileset1_eval2":
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
                break;
            default:
                fail("invalid expectedList value");
        }

        assertThat(tsa.getWarningsTabContents(), is(expectedContent));
    }

    /**
     * This method asserts the correct content of the "Fixed" tab
     * depending on the file set loaded to the workspace and the
     * task tags used.
     *
     * Supported assertions:
     *  - fileset1_less = fileset1_less, tags: FIXME, TODO, @Deprecated, case sensitive
     *
     * @param tsa the {@link org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction} object for
     *            the current job
     * @param expectedList determines which files and which warning counts are expected
     */
    private void assertFixedTab(TaskScannerAction tsa, String expectedList) {
        SortedMap<String, String> expectedContent = new TreeMap<>();
        // TODO: extend for all filesets
        switch (expectedList) {
            case "fileset1_less":
                expectedContent.put("TSRCleaner.java", "@Deprecated");
                expectedContent.put("TSRDockerImage.java", "TODO");
                expectedContent.put("TSRGitRepo.java", "FIXME");
                break;
            default:
                fail("invalid expectedList value");
        }

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
