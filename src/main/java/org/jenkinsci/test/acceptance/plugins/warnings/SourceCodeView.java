package org.jenkinsci.test.acceptance.plugins.warnings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Job;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

/**
 * Page object for the SourceCode view of the warnings plugin (white mountains release).
 *
 * @author Frank Christian Geyer
 * @author Deniz Mardin
 */
public class SourceCodeView extends AbstractStep {

    private static final List<String> FILE_CONTENT_LIST = new ArrayList<>();
    private static final List<String> HEADERS = new ArrayList<>();
    private final String eclipseResultPath;

    /**
     * Creates a new page object.
     *
     * @param parent
     *         parent page object
     */
    public SourceCodeView(final Job parent, final String path, final String eclipseResultPathConstructor) {
        super(parent, path);
        eclipseResultPath = eclipseResultPathConstructor;
    }

    private String getHeadlines() {
        return find(by.tagName("h1")).getText();
    }

    private WebElement getCodeTag() {
        return find(by.tagName("code"));
    }

    private String getTextOnly() {
        return driver.findElement(by.xpath("//*[@id=\"main-panel\"]")).getText();
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

    private void removeSourceLinesFromView() {
        JavascriptExecutor js = null;
        if (driver instanceof JavascriptExecutor) {
            js = (JavascriptExecutor) driver;
        }
        Objects.requireNonNull(js).executeScript("inputs = document.getElementsByTagName('code')[1];"
                + "document.querySelectorAll(\"a[name]\").forEach(e => e.parentNode.removeChild(e));");
    }

    /**
     * For convenience reasons the page object returns a finished object where all operations, like data extraction of
     * the important parts of the page is already done. Important parts are e.g. HEADERS, file contents etc.
     *
     * @return the prepared SourceView object which is ready for a custom assertion
     */
    public SourceCodeView processSourceCodeData() {

        parent.visit(eclipseResultPath);

        new TableView(parent, "").selectIssuesTab();

        List<String> links = getAllIssueLinks();

        for (int i = 0; i < links.size(); i++) {

            parent.visit(eclipseResultPath + splitStringAndReturnSourceLink(links.get(i)));

            removeSourceLinesFromView();

            int nonExistingFileOnFirstPosition = 0;
            if (i == nonExistingFileOnFirstPosition) {
                FILE_CONTENT_LIST.add(getTextOnly());
            }
            else {
                FILE_CONTENT_LIST.add(getCodeTag().getText());
            }
            HEADERS.add(getHeadlines());
        }
        return this;
    }

    /**
     * A simple getter for the source codes obtained from the web page.
     *
     * @return a list of source codes obtained from the web page
     */
    public List<String> getFileContentList() {
        return FILE_CONTENT_LIST;
    }

    /**
     * A simple getter for the HEADERS obtained from the web page.
     *
     * @return a list of HEADERS obtained from the web page
     */
    public List<String> getHeaders() {
        return HEADERS;
    }

}
