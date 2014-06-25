package org.jenkinsci.test.acceptance.plugins.tasks;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AbstractCodeStylePluginAction;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.NoSuchElementException;

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
     * @return the row as list of cell contents which matches the key
     *
     * @throws java.util.NoSuchElementException if key is not found
     *
     */
    public List<String> getCertainWarningsTabRow(String key){
        ensureTab("Warnings");

        final List<WebElement> rows = getVisibleTableRows(true,false);

        for(WebElement elem : rows){
            final List<WebElement> cells = elem.findElements(by.xpath("./td"));
            if (key.equals(asTrimmedString(cells.get(0)))){
                return asTrimmedStringList(cells);
            }
        }
        throw new NoSuchElementException();
    }

    /**
     * Getter for the "Plug-in Result:" line on a build's page. This line is only displayed
     * in case the Task Scanner plugin is configured to change the build status w.r.t to
     * certain warning thresholds.
     *
     * The resulting build status is displayed as image. In order to facilitate evaluation of
     * this line the image is replaced by it's title (= status).
     *
     * @param build the {@link org.jenkinsci.test.acceptance.po.Build} object to get the result from
     * @return the full line starting with "Plug-in Result:"
     */
    public String getPluginResult(Build build){
        //ensure the build status page is open:
        build.open();

        String pResult = asTrimmedString(
                find(by.xpath(".//li[starts-with(normalize-space(.), 'Plug-in Result:')]")));

        //insert the icon title at the place of the icon
        return StringUtils.substringBefore(pResult,":") + ": " + find(by.xpath(
                ".//img[@title = 'Success' or @title = 'Unstable' or @title = 'Failed']")).
                getAttribute("title").toUpperCase() + " -" + StringUtils.substringAfterLast(pResult, "-");
    }

}
