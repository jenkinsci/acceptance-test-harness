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

    private final By executors = By.xpath("//div[@id=\"side-panel\"]/div[@id=\"executors\"]");
    private final By table = By.xpath("//table/tbody");
    private final By header = By.xpath("//th");
    private final By executor = By.xpath("//tr/td[2]");

    public BuildExecutorStatus(PageObject context, String path) {
        super(context, path);
    }

    public WebElement getTable() {
        return find(executors).findElement(table);
    }

    public List<String> getHeaders() {
        return getTable().findElements(header).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public List<String> getExecutors() {
        return getTable().findElements(executor).stream()
                .map(WebElement::getText)
                .map(String::trim)
                .collect(Collectors.toList());
    }

}
