package org.jenkinsci.test.acceptance.plugins.warnings.issues_table;

import org.openqa.selenium.WebElement;

/**
 * Representation for default issues-table table row.
 * <p>
 * todo implement the necessary methods for this table row type
 *
 * @author Stephan Plöderl
 */
public class DefaultWarningsTableRow extends AbstractNonDetailsIssuesTableRow {

    DefaultWarningsTableRow(WebElement element, IssuesTable issuesTable) {
        super(element, issuesTable);
    }

}
