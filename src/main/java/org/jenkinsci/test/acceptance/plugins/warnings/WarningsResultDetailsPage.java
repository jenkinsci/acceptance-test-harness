package org.jenkinsci.test.acceptance.plugins.warnings;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.gargoylesoftware.htmlunit.Page;

public class WarningsResultDetailsPage extends ContainerPageObject {
    public enum tabs{Issues, Details, Packages, Modules}

    private final static String resultPathEnd = "Result/";

    public WarningsResultDetailsPage(final Job parent, final String plugin){
        super(parent, parent.url(plugin.toLowerCase()+resultPathEnd));
    }

    public WarningsResultDetailsPage(final Build parent, final String plugin){
        super(parent, parent.url(plugin.toLowerCase()+resultPathEnd));
    }

    private WebElement getTabs(){
        return getElement(By.id("tab-details"));
    }

    private List<HashMap<String, WebElement>> parseTable(final WebElement element){
        List<HashMap<String, WebElement>> parsedTable = new ArrayList<>();
        List<String> tableHeaders = element.findElements(By.xpath(".//thead/tr/th")).stream().map(WebElement::getText).collect(
                Collectors.toList());
        for(WebElement row: element.findElements(By.xpath(".//tbody/tr"))){
            List<WebElement> cellsOfRow = row.findElements(By.tagName("td"));
            HashMap<String, WebElement> cellData = new HashMap<>();
            for(int i = 0; i< tableHeaders.size(); i++){
                cellData.put(tableHeaders.get(i), cellsOfRow.get(i));
            }
            parsedTable.add(cellData);
        }
        return parsedTable;
    }

    public void openTab(final tabs tab){
        open();
        WebElement tabs = getTabs();
        WebElement tabElement = tabs.findElement(By.xpath("//a[text()='"+ tab.name() + "']"));
        tabElement.click();
    }

    public List<HashMap<String, WebElement>> getIssuesTable() {
        openTab(tabs.Issues);
        WebElement issuesTable = find(By.id("issues"));
        return parseTable(issuesTable);
    }

}
