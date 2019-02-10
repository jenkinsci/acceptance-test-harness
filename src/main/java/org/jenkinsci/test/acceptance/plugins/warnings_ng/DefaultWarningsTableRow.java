package org.jenkinsci.test.acceptance.plugins.warnings_ng;

import org.openqa.selenium.WebElement;

/**
 * Representation for default issues-table table row.
 * <p>
 *
 * @author Stephan Plöderl
 */
public class DefaultWarningsTableRow extends AbstractNonDetailsIssuesTableRow {
    DefaultWarningsTableRow(WebElement element, IssuesTable issuesTable) {
        super(element, issuesTable);
    }

    /**
     * Opens the source code of the affected file.
     */
    public SourceView openFile() {
        return clickOnLink(getFileLink(), SourceView.class);
    }

    /**
     * Opens the source code of the affected file.
     */
    public ConsoleLogView openConsoleLog() {
        return clickOnLink(getFileLink(), ConsoleLogView.class);
    }

    /**
     * Returns the category of the issue.
     *
     * @return the category name
     */
    public String getCategoryName() {
        return getCellContent("Category");
    }

    /**
     * Returns the type of the issue.
     *
     * @return the type of the issue
     */
    public String getTypeName() {
        return getCellContent("Type");
    }
}
