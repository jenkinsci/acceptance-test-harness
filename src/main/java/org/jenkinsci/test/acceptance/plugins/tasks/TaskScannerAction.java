package org.jenkinsci.test.acceptance.plugins.tasks;

import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginAction;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

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
                expectedContent.put("Cleaner.java", 1);
                expectedContent.put("DockerImage.java", 1);
                expectedContent.put("GitRepo.java", 2);
                expectedContent.put("JenkinsAcceptanceTestRule.java", 1);
                expectedContent.put("WinstoneDockerController.java", 1);
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
     * @param expectedList determines which files and which warning counts are expected
     */
    public void assertWarningsTab(String expectedList){
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        // TODO: extend for all filesets
        switch (expectedList){
            case "fileset1_eval1":
                expectedContent.put("GitRepo.java:38", 38);
                expectedContent.put("DockerImage.java:84", 84);
                expectedContent.put("JenkinsAcceptanceTestRule.java:51", 51);
                expectedContent.put("GitRepo.java:69", 69);
                expectedContent.put("WinstoneDockerController.java:73", 73);
                expectedContent.put("Cleaner.java:40", 40);
                break;
            default:
                fail("invalid expectedList value");
        }

        assertThat(this.getWarningsTabContents(), is(expectedContent));
        // TODO: do also assert the correct warning extraction
        //TODO: follow at least one link and assert the corret line is displayed
    }

}
