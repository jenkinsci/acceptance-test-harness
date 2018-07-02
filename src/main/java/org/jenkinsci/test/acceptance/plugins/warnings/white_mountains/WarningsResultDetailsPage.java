package org.jenkinsci.test.acceptance.plugins.warnings.white_mountains;

import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.IssuesTable.Type;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.google.inject.Injector;

/**
 * PageObject representing the details page of static analysis tool results.
 */
public class WarningsResultDetailsPage extends PageObject {
    private static final String RESULT_PATH_END = "Result/";
    private final String id;

    /**
     * Creates an instance of the page displaying the details of the issues for a specific tool.
     *
     * @param parent
     *         a finished build configured with a static analysis tool
     * @param id
     *         the type of the result page (e.g. simian or cpd)
     */
    public WarningsResultDetailsPage(final Build parent, final String id) {
        super(parent, parent.url(id.toLowerCase() + RESULT_PATH_END));
        this.id = id;
    }

    /**
     * Creates an instance of the page displaying the details of the issues. This Constructor is used for injecting a
     * filtered instance of the page (e.g. by clicking on links which open a filtered instance of a
     * WarningsResultDetailsPage.
     *
     * @param injector
     *         the injector of the page
     * @param url
     *         the url of the page
     * @param id
     *         the id of  the result page (e.g simian or cpd)
     */
    @SuppressWarnings("unused")
    public WarningsResultDetailsPage(final Injector injector, final URL url,
            final String id) {
        super(injector, url);
        this.id = id;
    }

    /**
     * Returns the table type of the issues table.
     *
     * @return the table type
     */
    private Type getIssuesTableType() {
        Type type = Type.Default;
        if (StringUtils.equalsIgnoreCase(id, "cpd") || StringUtils.equalsIgnoreCase(id, "cpd")) {
            type = Type.DRY;
        }
        return type;
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
     * Opens the WarningsResultDetailsPage and opens a specific tab in it.
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
    public IssuesTable getIssuesTable() {
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
        System.out.println(link);
        T retVal = newInstance(type, injector, url(link));
        element.click();
        return retVal;
    }

    /**
     * Opens a link to a filtered version of this WarningsResultDetailsPage by clicking on a link.
     *
     * @param element
     *         the WebElement representing the link to be clicked
     *
     * @return the instance of the filtered WarningsResultDetailsPage
     */
    public WarningsResultDetailsPage openFilterLinkOnSite(final WebElement element) {
        String link = element.getAttribute("href");
        WarningsResultDetailsPage retVal = newInstance(WarningsResultDetailsPage.class, injector, url(link), id);
        element.click();
        return retVal;
    }

    /**
     * Enum representing the possible tabs which can be opened on a WarningsResultDetailsPage.
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
    /**
     * Returns the trend chart page object.
     *
     * @return the trend chart
     */
    public WarningsTrendChart getTrendChart() {
        return new WarningsTrendChart(this);
    }

    /**
     * Returns the priority chart page object.
     *
     * @return the priority chart
     */
    public WarningsPriorityChart getPriorityChart() {
        return new WarningsPriorityChart(this);
    }
}
