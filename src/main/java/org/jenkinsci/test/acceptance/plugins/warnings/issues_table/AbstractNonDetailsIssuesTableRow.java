package org.jenkinsci.test.acceptance.plugins.warnings.issues_table;

import java.util.List;

import org.jenkinsci.test.acceptance.plugins.warnings.WarningsResultDetailsPage;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Abstract representation of a table row displaying an issue.
 *
 * @author Stephan Plöderl
 */
public abstract class AbstractNonDetailsIssuesTableRow extends AbstractIssuesTableRow {

    public static final String PRIORITY = "Priority";
    public static final String DETAILS = "Details";
    public static final String AGE = "Age";
    public static final String FILE = "File";
    private WebElement element;
    private IssuesTable issuesTable;

    /**
     * Creates a new instance of the table row.
     *
     * @param element
     *         the WebElement representing the row
     * @param table
     *         the table to which this row belongs to
     */
    AbstractNonDetailsIssuesTableRow(WebElement element, final IssuesTable table) {
        this.element = element;
        this.issuesTable = table;
    }

    /**
     * Performs a click on a link.
     *
     * @param element
     *         the WebElement representing the link
     * @param targetPageClass
     *         the PageObject class representing the target page
     *
     * @return the PageObject representing the target page
     */
    <T extends PageObject> T clickOnLink(final WebElement element, final Class<T> targetPageClass) {
        return issuesTable.clickLinkOnSite(element, targetPageClass);
    }

    /**
     * Returns all table data fields in the table row.
     *
     * @return the table data fields
     */
    List<WebElement> getCells() {
        return element.findElements(By.tagName("td"));
    }

    /**
     * Returns a specific table data field specified by the header of the column.
     *
     * @param header
     *         the header text specifying the column
     *
     * @return the WebElement of the table data field
     */
    WebElement getCell(String header) {
        return getCells().get(getHeaders().indexOf(header));
    }

    /**
     * Returns all possible headers representing the columns of the table.
     *
     * @return the headers of the table
     */
    List<String> getHeaders() {
        return issuesTable.getHeaders();
    }

    /**
     * Returns the String representation of the table cell.
     *
     * @param header
     *         the header specifying the column
     *
     * @return the String representation of the cell
     */
    String getCellContent(String header) {
        return getCell(header).getText();
    }

    /**
     * Returns the priority as String.
     *
     * @return the priority
     */
    public String getPriority() {
        return getCellContent(PRIORITY);
    }

    /**
     * Performs a click on the icon showing and hiding the details row.
     */
    public void toggleDetailsRow() {
        getCell(DETAILS).findElement(By.tagName("div")).click();
        issuesTable.updateTableRows();
    }

    /**
     * Returns the age of the issue as String.
     *
     * @return the age
     */
    public int getAge() {
        return Integer.parseInt(getCellContent(AGE));
    }

    /**
     * Returns the child WebElement representing a link.
     *
     * @param element
     *         the WebElement which is a parent of the link to be searched for
     *
     * @return the WebElement representing the link
     */
    private WebElement findLink(final WebElement element) {
        return element.findElement(By.tagName("a"));
    }

    /**
     * Returns a list of all the links which are children nodes of a specific WebElement.
     *
     * @param element
     *         the WebElement which is the parent of the links to be returned
     *
     * @return a List of the WebElements representing links
     */
    List<WebElement> findAllLinks(final WebElement element) {
        return element.findElements(By.tagName("a"));
    }

    /**
     * Performs a click on a link which filters the WarningsResultDetailsPage.
     *
     * @param columnName
     *         the columnName holding the link
     *
     * @return the representation of the filtered WarningsResultDetailsPage
     */
    private WarningsResultDetailsPage clickOnFilterLink(String columnName) {
        return issuesTable.clickFilterLinkOnSite(findLink(getCell(columnName)));
    }

    /**
     * Performs a click on the priority link.
     *
     * @return the representation of the filtered WarningsResultDetailsPage
     */
    public WarningsResultDetailsPage clickOnPriorityLink() {
        return clickOnFilterLink(PRIORITY);
    }

}
