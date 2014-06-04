package org.jenkinsci.test.acceptance.plugins.tasks;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginAction;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.openqa.selenium.WebElement;

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
     * This method gets a certain task's entry in the "Warnings"-tab specified by a key.
     *
     * @param key the name of the source file containing the task
     * @return the row as list of cells which matches the key
     *
     * @throws java.util.NoSuchElementException if key is not found
     *
     */
    public List<WebElement> getCertainWarningsTabRow(String key){
        ensureTab("Warnings");

        final List<WebElement> rows = getVisibleTableRows(true,false);

        for(WebElement elem : rows){
            final List<WebElement> cells = elem.findElements(by.xpath("./td"));
            if (key.equals(asTrimmedString(cells.get(0)))){
                return cells;
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * This method gets the source code file line which is linked by the "Warnings"-tab
     * table entries.
     * The particular line is found via the warning priority as it is used as title attribute's
     * value for this div object.
     *
     * @param linkText the name link in the "Warnings" tab.
     * @param priority the task priority: "High Priority", "Normal Priority" or "Low Priority"
     * @return the {@link org.openqa.selenium.WebElement} containing the source code line.
     */
    public WebElement getLinkedSourceFileLine(String linkText, String priority){

        ensureTab("Warnings");

        //find and follow the link to the source file display
        find(by.xpath(".//A[text() = '" + linkText + "']")).click();

        //find the highlighted line using the title attribute which is set to the priority
        return find(by.xpath("//div[@title='" + priority + "']"));

    }


}
