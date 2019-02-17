package org.jenkinsci.test.acceptance.plugins.warnings_ng;

import org.openqa.selenium.WebElement;

/**
 * Default row of the issues table that is used by most of the static analysis tools.
 *
 * @author Stephan Plöderl
 */
public class DefaultWarningsTableRow extends AbstractNonDetailsIssuesTableRow {
    DefaultWarningsTableRow(final WebElement rowElement, final IssuesTable issuesTable) {
        super(rowElement, issuesTable);
    }

    /**
     * Returns the category of the issue in this row.
     *
     * @return the category
     */
    public String getCategory() {
        return getCellContent("Category");
    }

    /**
     * Returns the type of the issue in this row.
     *
     * @return the type
     */
    public String getType() {
        return getCellContent("Type");
    }

    /**
     * Opens the source code of the affected file.
     */
    public ConsoleLogView openConsoleLog() {
        return clickOnLink(getFileLink(), ConsoleLogView.class);
    }
}
