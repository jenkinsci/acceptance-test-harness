package org.jenkinsci.test.acceptance.plugins.warnings.white_mountains;

import java.util.ArrayList;
import java.util.List;

import org.jenkinsci.test.acceptance.plugins.warnings.TableView;
import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Job;
import org.openqa.selenium.WebElement;

/**
 * Page object for the MavenConsoleParser view of the warnings plugin (white mountains release).
 *
 * @author Frank Christian Geyer
 * @author Deniz Mardin
 */
public class MavenConsoleParser extends AbstractStep {

    private static final List<String> FILE_CONTENT_LIST = new ArrayList<>();
    private static final List<String> HEADERS = new ArrayList<>();
    private final String mavenResultPath;

    /**
     * Creates a new page object.
     *
     * @param parent
     *         parent page object
     */
    public MavenConsoleParser(final Job parent, final String path, final String mavenResultPathConstructor) {
        super(parent, path);
        mavenResultPath = mavenResultPathConstructor;
    }

    private String getHeadlines() {
        return find(by.tagName("h1")).getText();
    }

    private List<WebElement> getStyleTags() {
        return driver.findElements(by.xpath("//td[contains(@style, 'background-color')]"));
    }

    private List<WebElement> getAllIssueLinksByXpath() {
        return driver.findElements(by.xpath("//td/a[contains(@href, 'source')]"));
    }

    private List<String> getAllIssueLinks() {
        List<WebElement> list = getAllIssueLinksByXpath();
        List<String> links = new ArrayList<>();
        for (WebElement webElement : list) {
            links.add(webElement.getAttribute("href"));
        }
        return links;
    }

    private String splitStringAndReturnSourceLink(final String link) {
        int sourceLinkPosition = 2;
        String[] splittedLinkArrayBySlash = link.split("/");
        return splittedLinkArrayBySlash[splittedLinkArrayBySlash.length - sourceLinkPosition];
    }

    /**
     * For convenience reasons the page object returns a finished object where all operations, like data extraction of
     * the important parts of the page is already done. Important parts are e.g. HEADERS, file contents etc.
     *
     * @return the prepared MavenConsoleParser object which is ready for a custom assertion
     */
    public MavenConsoleParser processMavenConsoleParserOutput() {

        parent.visit(mavenResultPath);

        new TableView(parent, "").selectIssuesTab();

        List<String> links = getAllIssueLinks();

        for (String link : links) {
            parent.visit(mavenResultPath + splitStringAndReturnSourceLink(link));

            if (getStyleTags().size() > 1) {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < getStyleTags().size(); i++) {
                    stringBuilder.append(getStyleTags().get(i).getText());
                    int firstElementOccurrence = 0;
                    if (i == firstElementOccurrence) {
                        stringBuilder.append(System.getProperty("line.separator"));
                    }
                }
                FILE_CONTENT_LIST.add(stringBuilder.toString());
            }
            else {
                int onlyOneElementOccurrence = 0;
                FILE_CONTENT_LIST.add(getStyleTags().get(onlyOneElementOccurrence).getText());
            }
            HEADERS.add(getHeadlines());
        }
        return this;
    }

    /**
     * A simple getter for the warnings obtained from the web page.
     *
     * @return a list of warnings obtained from the web page
     */
    public List<String> getWarningContentList() {
        return FILE_CONTENT_LIST;
    }

    /**
     * A simple getter for the headers obtained from the web page.
     *
     * @return a list of headers obtained from the web page
     */
    public List<String> getHeaders() {
        return HEADERS;
    }

}
