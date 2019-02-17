package org.jenkinsci.test.acceptance.plugins.warnings_ng;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Class representing an issues-table on the {@link AnalysisResult}.
 *
 * @author Stephan Plöderl
 */
public class IssuesTable {
    private final AnalysisResult resultDetailsPage;
    private final List<AbstractIssuesTableRow> tableRows = new ArrayList<>();
    private final List<String> headers;
    private final WebElement tableElement;
    private final IssuesTableRowType type;

    /**
     * Creates an IssuesTable of a specific type.
     *
     * @param element
     *         the WebElement representing the issues-table
     * @param resultDetailsPage
     *         the AnalysisResult on which the issues-table is displayed on
     * @param type
     *         the type of the issues-table (e.g. Default or DRY)
     */
    public IssuesTable(final WebElement element, final AnalysisResult resultDetailsPage, final IssuesTableRowType type) {
        headers = element.findElements(By.xpath(".//thead/tr/th"))
                .stream()
                .map(WebElement::getText)
                .collect(
                        Collectors.toList());
        this.type = type;
        this.tableElement = element;
        updateTableRows();
        this.resultDetailsPage = resultDetailsPage;
    }

    /**
     * Performs a click on a link on this site and returns the corresponding PageObject of the target page.
     *
     * @param link
     *         the WebElement representing the link
     * @param targetPageClass
     *         the class of the PageObject representing the target page
     *
     * @return the PageObject representing the target page
     */
    public <T extends PageObject> T clickLinkOnSite(WebElement link, Class<T> targetPageClass) {
        return resultDetailsPage.openLinkOnSite(link, targetPageClass);
    }

    /**
     * Updates the table rows. E.g. if they are changed by toggling a details-row.
     */
    public void updateTableRows() {
        tableRows.clear();
        List<WebElement> tableRowsAsWebElements = tableElement.findElements(By.xpath(".//tbody/tr"));
        tableRowsAsWebElements.forEach(element -> tableRows.add(getRightTableRow(element)));
    }

    /**
     * Returns the table row as an object of the right sub class of {@link AbstractIssuesTableRow}.
     *
     * @param row
     *         the WebElement representing the specific row.
     *
     * @return the table row
     */
    private AbstractIssuesTableRow getRightTableRow(final WebElement row) {
        String rowType = row.getAttribute("role");
        if (StringUtils.equals(rowType, "row")) {
            if (type == IssuesTableRowType.DRY) {
                return new DryIssuesTableRow(row, this);
            }
            else {
                return new DefaultWarningsTableRow(row, this);
            }
        }
        else {
            return new DetailsTableRow(row);
        }
    }

    /**
     * Returns the amount of the headers for this table.
     *
     * @return the amount of table headers
     */
    public int getHeaderSize() {
        return headers.size();
    }

    /**
     * Returns the amount of table rows.
     *
     * @return the amount of table rows.
     */
    public int getSize() {
        return tableRows.size();
    }

    /**
     * Returns the table rows as List.
     *
     * @return the rows of the table
     */
    public List<AbstractIssuesTableRow> getTableRows() {
        return tableRows;
    }

    /**
     * Return the headers of the table.
     *
     * @return the headers
     */
    public List<String> getHeaders() {
        return headers;
    }

    /**
     * Returns a specific row as an instance of the expected class.
     *
     * @param row
     *         the number of the row to be returned
     * @param expectedClass
     *         the expected type of the row
     *
     * @return the row
     */
    public <T extends AbstractIssuesTableRow> T getRowAs(int row, Class<T> expectedClass) {
        return getTableRows().get(row).getAs(expectedClass);
    }

    /**
     * Performs a click on a link which opens a filtered instance of the AnalysisResult.
     *
     * @param element
     *         the WebElement representing the link
     *
     * @return the filtered AnalysisResult
     */
    public AnalysisResult clickFilterLinkOnSite(final WebElement element) {
        return this.resultDetailsPage.openFilterLinkOnSite(element);
    }

    public enum IssuesTableRowType {DEFAULT, DRY}
}