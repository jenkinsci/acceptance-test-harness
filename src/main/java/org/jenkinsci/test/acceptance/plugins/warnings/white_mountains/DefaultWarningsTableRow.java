package org.jenkinsci.test.acceptance.plugins.warnings.white_mountains;

import org.openqa.selenium.WebElement;

/**
 * Representation for default issues-table table row.
 * <p>
 *
 * @author Stephan Plöderl
 */
 // TODO: implement necessary methods for this table row type
public class DefaultWarningsTableRow extends AbstractNonDetailsIssuesTableRow {
    DefaultWarningsTableRow(WebElement element, IssuesTable issuesTable) {
        super(element, issuesTable);
    }
}
