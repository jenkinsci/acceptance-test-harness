package org.jenkinsci.test.acceptance.plugins.warnings;

import java.util.HashMap;
import java.util.List;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class StatusPage extends ContainerPageObject {

    private HashMap<String, SummaryBoxPageArea> summaryBoxes = new HashMap<>();

    public StatusPage(Build build, List<String> pluginNames, Boolean aggregatedResults) {
        super(build, build.url("/"));
        if (aggregatedResults) {
            summaryBoxes.put("analysis", new SummaryBoxPageArea("analysis"));
        }
        else {
            for (String plugin : pluginNames) {
                summaryBoxes.put(plugin, new SummaryBoxPageArea(plugin.toLowerCase()));
            }
        }
    }

    public SummaryBoxPageArea getSummaryBoxByName(String pluginName) {
        return summaryBoxes.get(pluginName);
    }

    public class SummaryBoxPageArea {
        private WebElement warningDiv;
        private WebElement titleDiv;
        private List<WebElement> resultList;
        private WebElement titleDivResultLink;
        private WebElement titleDivResultInfoLink;
        private String titleDivResultLinkString;
        private String titleDivResultInfoLinkString;


        SummaryBoxPageArea(String parserName) {

            warningDiv = find(By.id(parserName + "-summary"));
            titleDiv = find(By.id(parserName + "-title"));

            resultList = initResultList(parserName);
            titleDivResultLink = initTitleDivResultLink(parserName);
            titleDivResultLinkString = titleDivResultLink != null ? titleDivResultLink.getAttribute("href") : "";
            titleDivResultInfoLink = initTitleDivResultInfoLink(parserName);
            titleDivResultInfoLinkString = titleDivResultInfoLink != null ? titleDivResultInfoLink.getAttribute("href") : "";
        }

        private List<WebElement> initResultList(String parserName) {
            try {
                return warningDiv.findElements(by.xpath("./ul/li"));
            }
            catch (NoSuchElementException e) {
                return null;
            }
        }

        private WebElement initTitleDivResultInfoLink(String parserName) {
            try {
                return warningDiv.findElement(by.href(parserName + "Result/info"));
            }
            catch (NoSuchElementException e) {
                return null;
            }
        }

        private WebElement initTitleDivResultLink(String parserName) {
            try {
                return warningDiv.findElement(by.href(parserName + "Result"));
            }
            catch (NoSuchElementException e) {
                return null;
            }
        }

        public WebElement findClickableResultEntryByNamePart(String namePart) {
            for (WebElement el : resultList) {
                if (el.getText().contains(namePart)) {
                    return el.findElement(by.tagName("a"));
                }
            }
            return null;
        }

        public String findResultEntryTextByNamePart(String namePart) {
            for (WebElement el : resultList) {
                if (el.getText().contains(namePart)) {
                    return el.getText();
                }
            }
            return null;
        }

        public WebElement getWarningDiv() {
            return warningDiv;
        }

        public WebElement getTitleDiv() {
            return titleDiv;
        }

        public List<WebElement> getResultList() {

            return resultList;
        }

        public String getTitleDivResultLinkString() {
            return titleDivResultLinkString;
        }

        public String getTitleDivResultInfoLinkString() {
            return titleDivResultInfoLinkString;
        }

        public WebElement getTitleDivResultLink() {
            return titleDivResultLink;
        }

        public WebElement getTitleDivResultInfoLink() {
            return titleDivResultInfoLink;
        }
    }

}
