package plugins;


import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerAction;
import org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.openqa.selenium.WebElement;

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

        // check that the correct line / task is displayed when following the link in the warnings tab
        final WebElement taskLine = tsa.getLinkedSourceFileLine("TSRDockerImage.java:84", "Normal Priority");

        //assert contents of that line
        assertThat(Integer.parseInt(taskLine.findElement(by.tagName("a")).getText().trim()), is(84));
        assertThat(taskLine.getText(), containsString("TODO"));
        assertThat(taskLine.getText(), endsWith("properly wait for either cidfile to appear or process to exit"));


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

    //TODO: Test for JENKINS-22744: https://issues.jenkins-ci.org/browse/JENKINS-22744

    /**
     * This method asserts the correct content of the files tab
     * depending on the file set loaded to the workspace and the
     * task tags used.
     *
     * Supported assertions:
     *  - fileset1_eval1 = fileset1, tags: FIXME, TODO, @Deprecated, case sensitive
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
                expectedContent.put("TSRDockerImage.java:84", 84);
                expectedContent.put("TSRJenkinsAcceptanceTestRule.java:51", 51);
                expectedContent.put("TSRGitRepo.java:69", 69);
                expectedContent.put("TSRWinstoneDockerController.java:73", 73);
                expectedContent.put("TSRCleaner.java:40", 40);
                break;
            default:
                fail("invalid expectedList value");
        }

        assertThat(tsa.getWarningsTabContents(), is(expectedContent));
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
        final List<WebElement> cells = tsa.getCertainWarningsTabRow(filename + ":" + lineNumber);

        assertThat(cells.get(3).getText().trim(), is(type));
        assertThat(cells.get(4).getText().trim(), is(warningText));

    }

}
