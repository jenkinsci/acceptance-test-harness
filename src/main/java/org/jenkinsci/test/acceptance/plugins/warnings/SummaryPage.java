package org.jenkinsci.test.acceptance.plugins.warnings;

import java.util.List;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Page object for the SummaryPage (Result-Page) of the warnings plugin (white mountains release).
 *
 * @author Michaela Reitschuster
 * @author Alexandra Wenzel
 * @author Manuel Hampp
 */
public class SummaryPage extends ContainerPageObject {

    /**
     * Creates a new PageObject which represents the summary page of a build.
     */
    public SummaryPage(Build build, boolean aggregatedResults) {
        super(build, build.url("/"));
    }

    public SummaryBoxPageArea getSummaryBoxByName(String pluginName) {
        return new SummaryBoxPageArea(pluginName.toLowerCase());
    }

    private WebElement getBuildTitle() {
        return find(By.className("build-caption"));
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
        private WebElement summary;
        private WebElement title;
        private List<WebElement> resultList;
        String parserName;

        SummaryBoxPageArea(String parserName) {
            this.parserName = parserName;
            summary = find(By.id(parserName + "-summary"));
            title = find(By.id(parserName + "-title"));
            resultList = initResultList();
        }

        private List<WebElement> initResultList() {
            return summary.findElements(by.xpath("./ul/li"));
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

        public WebElement getSummary() {
            return summary;
        }

        public WebElement getTitleResultLink() {
            return summary.findElement(by.href(this.parserName + "Result"));
        }

        public WebElement getTitleResultInfoLink() {
            return summary.findElement(by.href(this.parserName + "Result/info"));
        }

        public WebElement getTitle() {
            return title;
        }
    }

}
