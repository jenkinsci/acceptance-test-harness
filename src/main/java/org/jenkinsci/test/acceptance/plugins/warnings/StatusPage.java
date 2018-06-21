package org.jenkinsci.test.acceptance.plugins.warnings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Job;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class WarningsResultDetailsPage extends ContainerPageObject {
    public enum Tabs {
        ISSUES, DETAILS, PACKAGES, MODULES;
        
        public By getXpath() {
            return By.xpath("//a[text()='" + StringUtils.capitalize(name().toLowerCase()) + "']");
        }
    }

    private static final String RESULT_PATH_END = "Result/";

    public WarningsResultDetailsPage(final Job parent, final String id) {
        super(parent, parent.url(id.toLowerCase() + RESULT_PATH_END));
    }

    public WarningsResultDetailsPage(final Build parent, final String id) {
        super(parent, parent.url(id.toLowerCase() + RESULT_PATH_END));
    }

    private WebElement getTabs() {
        return getElement(By.id("tab-details"));
    }

    private List<Map<String, WebElement>> parseTable(final WebElement element) {
        List<Map<String, WebElement>> parsedTable = new ArrayList<>();
        List<String> tableHeaders = element.findElements(By.xpath(".//thead/tr/th"))
                .stream()
                .map(WebElement::getText)
                .collect(Collectors.toList());
        for (WebElement row : element.findElements(By.xpath(".//tbody/tr"))) {
            List<WebElement> cellsOfRow = row.findElements(By.tagName("td"));
            HashMap<String, WebElement> cellData = new HashMap<>();
            for (int i = 0; i < tableHeaders.size(); i++) {
                cellData.put(tableHeaders.get(i), cellsOfRow.get(i));
            }
            parsedTable.add(cellData);
        }
        return parsedTable;
    }

    public void openTab(final Tabs tab) {
        open();
        WebElement tabs = getTabs();
        WebElement tabElement = tabs.findElement(tab.getXpath());
        tabElement.click();
    }

    public List<Map<String, WebElement>> getIssuesTable() {
        openTab(Tabs.ISSUES);
        WebElement issuesTable = find(By.id("issues"));
        return parseTable(issuesTable);
    }

}
