package org.jenkinsci.test.acceptance.plugins.dashboard_view.read;

import java.util.List;
import java.util.stream.Collectors;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

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
     * Header in the table for the name of the node. If only one node, the header is not shown.
     */
    private final By header = By.xpath("//div[@class=\"computer-caption\"]/span");

    public BuildExecutorStatus(PageObject context, String path) {
        super(context, path);
    }

    /**
     * Get the div containing the nodes and executors.
     *
     * @return the WebElement for the div.
     */
    public WebElement getExecutorsDiv() {
        return find(executors);
    }

    /**
     * Get the Headers (names of all nodes) in the table.
     * <p>
     * If only one node header would be shown, it is not. And therefore also not in this list.
     *
     * @return the names of all displayed nodes/agents.
     */
    public List<String> getHeaders() {
        return getExecutorsDiv().findElements(header).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
