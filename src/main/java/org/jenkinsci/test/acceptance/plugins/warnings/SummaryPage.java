package org.jenkinsci.test.acceptance.plugins.warnings;

import java.util.HashMap;
import java.util.List;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * Page object for the SummaryPage (Result-Page) of the warnings plugin (white mountains release).
 *
 * @author Michaela Reitschuster
 * @author Alexandra Wenzel
 * @author Manuel Hampp
 */
public class SummaryPage extends ContainerPageObject {

    private HashMap<String, SummaryBoxPageArea> summaryBoxes = new HashMap<>();
    private WebElement buildTitle;

    /**
     * Creates a new PageObject which represents the summary page of a build.
     */
    public SummaryPage(Build build, List<String> pluginNames, Boolean aggregatedResults) {
        super(build, build.url("/"));
        this.buildTitle = find(By.className("build-caption"));
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

    private WebElement getBuildTitle() {
        return buildTitle;
    }

    public String getBuildState() {
        return getBuildTitle().findElement(By.tagName("img")).getAttribute("title");
    }

    /**
     * Summary Box of a issue recorder result.
     *
     * @author Michaela Reitschuster
     * @author Alexandra Wenzel
     * @author Manuel Hampp
     */
    public class SummaryBoxPageArea {
        private WebElement warningDiv;
        private WebElement titleDiv;
        private List<WebElement> resultList;
        private WebElement titleDivResultLink;
        private WebElement titleDivResultInfoLink;

        SummaryBoxPageArea(String parserName) {
            warningDiv = find(By.id(parserName + "-summary"));
            titleDiv = find(By.id(parserName + "-title"));
            resultList = initResultList();
            titleDivResultLink = initTitleDivResultLink(parserName);
            titleDivResultInfoLink = initTitleDivResultInfoLink(parserName);
        }

        private List<WebElement> initResultList() {
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

        /**
         * Returns the qualitygate result of this parser, if set.
         *
         * @return Success - if the quality gate thresholds have not been reached. Failed - otherwise.
         */
        public String getQualityGateResult() {
            for (WebElement el : resultList) {
                if (el.getText().contains("Quality gate")) {
                    return el.findElement(by.tagName("img")).getAttribute("title");
                }
            }
            return null;
        }

        /**
         * Returns a clickable WebElement (a-tag), by a part of the elements text.
         *
         * @param namePart
         *         part of the visible text (should be unique within the item list)
         *
         * @return WebElement that belongs to the name part
         */
        public WebElement findClickableResultEntryByNamePart(String namePart) {
            for (WebElement el : resultList) {
                if (el.getText().contains(namePart)) {
                    return el.findElement(by.tagName("a"));
                }
            }
            return null;
        }

        /**
         * Returns the complete visible text by a part of the elements text.
         *
         * @param namePart
         *         part of the visible text (should be unique within the item list)
         *
         * @return String that belongs to the name part
         */
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

        public WebElement getTitleDivResultLink() {
            return titleDivResultLink;
        }

        public WebElement getTitleDivResultInfoLink() {
            return titleDivResultInfoLink;
        }

        public WebElement getTitleDiv() {
            return titleDiv;
        }
    }

}
