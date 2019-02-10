package org.jenkinsci.test.acceptance.plugins.warnings_ng;

import java.net.URL;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.google.inject.Injector;

import org.jenkinsci.test.acceptance.plugins.warnings_ng.IssuesTable.Type;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * PageObject representing the details page of static analysis tool results.
 */
public class AnalysisResult extends PageObject {
    private static final String[] DRY_TOOLS = new String[] {"cpd", "simian", "dupfinder"};
    private final String id;

    /**
     * Creates an instance of the page displaying the details of the issues for a specific tool.
     *
     * @param parent
     *         a finished build configured with a static analysis tool
     * @param id
     *         the type of the result page (e.g. simian or cpd)
     */
    public AnalysisResult(final Build parent, final String id) {
        super(parent, parent.url(id.toLowerCase()));
        this.id = id;
    }

    /**
     * Creates an instance of the page displaying the details of the issues. This constructor is used for injecting a
     * filtered instance of the page (e.g. by clicking on links which open a filtered instance of a AnalysisResult.
     *
     * @param injector
     *         the injector of the page
     * @param url
     *         the url of the page
     * @param id
     *         the id of  the result page (e.g simian or cpd)
     */
    @SuppressWarnings("unused")
    public AnalysisResult(final Injector injector, final URL url, final String id) {
        super(injector, url);

        this.id = id;
    }

    /**
     * Returns the table type of the issues table.
     *
     * @return the table type
     */
    private Type getIssuesTableType() {
        if (ArrayUtils.contains(DRY_TOOLS, id)) {
            return Type.DRY;
        }
        return Type.DEFAULT;
    }

    /**
     * Returns the WebElement containing the tabs.
     *
     * @return the WebElement
     */
    private WebElement getTabs() {
        return getElement(By.id("tab-details"));
    }

    /**
     * Opens the AnalysisResult and opens a specific tab in it.
     *
     * @param tab
     *         the tab which shall be opened
     */
    public void openTab(final Tabs tab) {
        open();
        WebElement tabs = getTabs();
        WebElement tabElement = tabs.findElement(tab.getXpath());
        tabElement.click();
    }

    /**
     * Opens the issues tab and returns the table displayed in it as {@link IssuesTable} object.
     *
     * @return the issues-table.
     */
    public IssuesTable openIssuesTable() {
        openTab(Tabs.ISSUES);
        WebElement issuesTable = find(By.id("issues"));
        return new IssuesTable(issuesTable, this, getIssuesTableType());
    }

    /**
     * Opens a link on the page leading to another page.
     *
     * @param element
     *         the WebElement representing the link to be clicked
     * @param type
     *         the class of the PageObject which represents the page to which the link leads to
     *
     * @return the instance of the PageObject to which the link leads to
     */
    public <T extends PageObject> T openLinkOnSite(final WebElement element, final Class<T> type) {
        String link = element.getAttribute("href");
        T retVal = newInstance(type, injector, url(link));
        element.click();
        return retVal;
    }

    /**
     * Opens a link to a filtered version of this AnalysisResult by clicking on a link.
     *
     * @param element
     *         the WebElement representing the link to be clicked
     *
     * @return the instance of the filtered AnalysisResult
     */
    public AnalysisResult openFilterLinkOnSite(final WebElement element) {
        String link = element.getAttribute("href");
        AnalysisResult retVal = newInstance(AnalysisResult.class, injector, url(link), id);
        element.click();
        return retVal;
    }

    /**
     * Enum representing the possible tabs which can be opened on a AnalysisResult.
     */
    public enum Tabs {
        ISSUES, DETAILS, PACKAGES, MODULES;

        /**
         * Returns the selenium filter rule to find the specific tab.
         *
         * @return the selenium filter rule
         */
        public By getXpath() {
            return By.xpath("//a[text()='" + StringUtils.capitalize(name().toLowerCase()) + "']");
        }
    }
}
