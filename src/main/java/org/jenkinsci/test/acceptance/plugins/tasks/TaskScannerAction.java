package org.jenkinsci.test.acceptance.plugins.tasks;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginAction;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.openqa.selenium.WebElement;
import org.w3c.dom.html.HTMLElement;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * Page object for Task Scanner action.
 *
 * @author Martin Ende
 */
public class TaskScannerAction  extends AbstractCodeStylePluginAction {

    public TaskScannerAction(ContainerPageObject parent) {
        super(parent, "tasks");
    }

    /**
     * Getter for the full result text surrounding the link text, split at the
     * newline character.
     * @param linkText link text to find the result string
     * @return the full sentence containing the link text.
     */
    public String getResultTextByXPathText(final String linkText) {
        final String htmlElement = find(by.xpath(".//A[text() = '" + linkText + "']/..")).getText();
        return StringUtils.substringBefore(htmlElement,"\n");
    }

    /**
     * This method asserts the correct content of the files tab
     * depending on the file set loaded to the workspace and the
     * task tags used.
     *
     * Supported assertions:
     *  - fileset1_eval1 = fileset1, tags: FIXME, TODO, @Deprecated, case sensitive
     *
     * @param expectedList determines which files and which warning counts are expected
     */
    public void assertFilesTab(String expectedList){
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

        assertThat(this.getFileTabContents(), is(expectedContent));
    }

    /**
     * This method asserts the correct content of the Types tab
     * depending on the file set loaded to the workspace and the
     * task tags used.
     *
     * Supported assertions:
     *  - fileset1_eval1 = fileset1, tags: FIXME, TODO, @Deprecated, case sensitive
     *
     * @param expectedList determines which files and which warning counts are expected
     */
    public void assertTypesTab(String expectedList){
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

        assertThat(this.getTypesTabContents(), is(expectedContent));
    }

    /**
     * This method asserts the correct content of the Warnings tab
     * depending on the file set loaded to the workspace and the
     * task tags used.
     *
     * Supported assertions:
     *  - fileset1_eval1 = fileset1, tags: FIXME, TODO, @Deprecated, case sensitive
     *
     * @param expectedList determines which files and lines are expected
     */
    public void assertWarningsTab(String expectedList){
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

        assertThat(this.getWarningsTabContents(), is(expectedContent));
    }

    /**
     * This method asserts that a certain task is contained in the table shown in
     * the "Warnings"-tab with the correct task type and text
     *
     * @param filename the name of the source file containing the task
     * @param lineNumber the line number of the task
     * @param type the task type
     * @param warningText the text which should have been extracted from the source file
     */
    public void assertWarningExtraction(String filename, Integer lineNumber,
                                        String type, String warningText){
        ensureTab("Warnings");

        final List<WebElement> rows = getVisibleTableRows(true,false);
        boolean isContained = false;

        for(WebElement elem : rows){
            final List<WebElement> cells = elem.findElements(by.xpath("./td"));
            final String key = filename + ":" + lineNumber;
            if (key.equals(asTrimmedString(cells.get(0)))){
                assertThat(asTrimmedString(cells.get(3)), is(type));
                assertThat(asTrimmedString(cells.get(4)), is(warningText));
                isContained = true;
                break;
            }
        }

        //assert that the Warning was listed
        assertThat(isContained, is(true));
    }

    /**
     * This method asserts the correct linking between the "Warnings"-tab table entries
     * and the source file display page where the task line is highlighted
     *
     * @param filename the name of the source file containing the task
     * @param lineNumber the line number which shall be highlighted
     * @param priority the task priority: "High Priority", "Normal Priority" or "Low Priority"
     * @param type the task type
     * @param warningText the text which should have been extracted from the source file
     */
    public void assertLinkToSourceFileLine(String filename, Integer lineNumber, String priority,
                                           String type, String warningText){

        ensureTab("Warnings");

        //find and follow the link to the source file display
        find(by.xpath(".//A[text() = '" + filename + ":" + lineNumber + "']")).click();

        //find the highlighted line using the title attribute which is set to the priority
        WebElement taskLine = find(by.xpath("//div[@title='" + priority + "']"));

        //assert contents of that line
        assertThat(asInt(taskLine.findElement(by.tagName("a"))), is(lineNumber));
        assertThat(taskLine.getText(), containsString(type));
        assertThat(taskLine.getText(), endsWith(warningText));

    }


}
