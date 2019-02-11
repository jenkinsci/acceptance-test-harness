package org.jenkinsci.test.acceptance.plugins.warnings_ng;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Page object for the analysis summary on the build page of a job.
 *
 * @author Ullrich Hafner
 * @author Manuel Hampp
 * @author Michaela Reitschuster
 * @author Alexandra Wenzel
 */
public class AnalysisSummary extends PageObject {
    private static final Pattern NUMBER = Pattern.compile(".*(\\d)+.*");

    private final WebElement summary;
    private WebElement title;
    private List<WebElement> results;
    private final String id;

    /**
     * Creates a new PageObject which represents the summary page of a build.
     */
    public AnalysisSummary(final Build parent, final String id) {
        super(parent, parent.url(id.toLowerCase()));

        this.id = id;
        summary = getElement(By.id(id + "-summary"));
        if (summary != null) {
            title = find(By.id(id + "-title"));
            results = initResultList();
        }
    }

    public boolean isDisplayed() {
        return summary != null && summary.isDisplayed();
    }

    public String getTitleText() {
        return title.getText();
    }

    public int getNewSize() {
        return getSize("new");
    }

    public int getFixedSize() {
        return getSize("fixed");
    }

    public int getReferenceBuild() {
        return getSize("#");
    }

    public String getAggregation() {
        for (WebElement result : results) {
            String message = result.getText();
            String aggregation = "Static analysis results from: ";
            if (message.startsWith(aggregation)) {
                return StringUtils.removeStart(message, aggregation);
            }
        }
        return "-";
    }

    private int getSize(final String linkName) {
        Optional<WebElement> newLink = findClickableResultEntryByNamePart(linkName);

        return newLink.map(webElement -> extractNumber(webElement.getText())).orElse(0);
    }

    private int extractNumber(final String linkText) {
        Matcher matcher = NUMBER.matcher(linkText);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        else if (linkText.startsWith("One")) {
            return 1;
        }
        else {
            return 0;
        }
    }

    private List<WebElement> initResultList() {
        return summary.findElements(by.xpath("./ul/li"));
    }

    /**
     * Clicks the title link that opens the details page with the analysis results.
     *
     * @return the details page with the analysis result
     */
    public AnalysisResult clickTitleLink() {
        return openPage(getTitleResultLink(), AnalysisResult.class);
    }

    /**
     * Clicks the info link that opens the messages page showing all info and error messages.
     *
     * @return the messages page showing all info and error messages
     */
    public LogMessagesView clickInfoLink() {
        return openPage(getTitleResultInfoLink(), LogMessagesView.class);
    }

    /**
     * Clicks the new link that opens details page with the analysis results - filtered by new issues.
     *
     * @return the details page with the analysis result
     */
    public AnalysisResult clickNewLink() {
        return openLink("new", "No new link found");
    }

    /**
     * Clicks the fixed link that opens details page with the fixed issues.
     *
     * @return the details page with the analysis result
     */
    public AnalysisResult clickFixedLink() {
        return openLink("fixed", "No fixed link found");
    }

    /**
     * Clicks the reference build link that opens details page with the analysis results of the reference build.
     *
     * @return the details page with the analysis result of the reference build
     */
    public AnalysisResult clickReferenceBuildLink() {
        return openLink("Reference", "No reference build link found");
    }

    private AnalysisResult openLink(final String s, final String s2) {
        Optional<WebElement> newLink = findClickableResultEntryByNamePart(s);
        if (newLink.isPresent()) {
            return openPage(newLink.get(), AnalysisResult.class);
        }
        throw new NoSuchElementException(s2);
    }

    private <T extends PageObject> T openPage(final WebElement link, final Class<T> type) {
        String href = link.getAttribute("href");
        T result = newInstance(type, injector, url(href), id);
        link.click();

        return result;
    }

    /**
     * Returns the qualitygate result of this parser, if set.
     *
     * @return Success - if the quality gate thresholds have not been reached. Failed - otherwise.
     */
    public String getQualityGateResult() {
        for (WebElement el : results) {
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
    public Optional<WebElement> findClickableResultEntryByNamePart(String namePart) {
        for (WebElement el : results) {
            if (el.getText().contains(namePart)) {
                return Optional.of(el.findElement(by.tagName("a")));
            }
        }
        return Optional.empty();
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
        for (WebElement el : results) {
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
        return summary.findElement(by.href(this.id));
    }

    public WebElement getTitleResultInfoLink() {
        return summary.findElement(by.href(this.id + "/info"));
    }

    public WebElement getTitle() {
        return title;
    }
}
