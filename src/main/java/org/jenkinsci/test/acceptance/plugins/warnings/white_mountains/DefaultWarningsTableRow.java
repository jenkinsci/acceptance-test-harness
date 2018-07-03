package org.jenkinsci.test.acceptance.plugins.warnings.white_mountains;

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
}
