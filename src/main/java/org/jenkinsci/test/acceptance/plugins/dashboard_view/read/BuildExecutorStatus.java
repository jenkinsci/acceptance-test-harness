package org.jenkinsci.test.acceptance.plugins.dashboard_view.read;

import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides a simple area for reading the executors pane on the dashboard.
 *
 * @author Peter Müller
 */
public class BuildExecutorStatus extends PageAreaImpl {

    /**
     * Container for all further elements.
     */
    private final By executors = By.xpath("//div[@id=\"side-panel\"]/div[@id=\"executors\"]");
    /**
     * Main table of nodes and executors.
     */
    private final By table = By.xpath("//table/tbody");
    /**
     * Header in the table for the name of the node. If only one node, the header is not shown.
     */
    private final By header = By.xpath("//th");
    /**
     * All Executors. (not split by header)
     */
    private final By executor = By.xpath("//tr/td[2]");

    public BuildExecutorStatus(PageObject context, String path) {
        super(context, path);
    }

    /**
     * Get the main table containing the nodes and executors.
     *
     * @return the WebElement for the table.
     */
    public WebElement getTable() {
        return find(executors).findElement(table);
    }

    /**
     * Get the Headers (names of all nodes) in the table.
     * <p>
     * If only one node header would be shown, it is not. And therefore also not in this list.
     *
     * @return the names of all displayed nodes/agents.
     */
    public List<String> getHeaders() {
        return getTable().findElements(header).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * Get all the executors displayed in the table.
     * @return the list of executor names.
     */
    public List<String> getExecutors() {
        return getTable().findElements(executor).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .collect(Collectors.toList());
    }

}
