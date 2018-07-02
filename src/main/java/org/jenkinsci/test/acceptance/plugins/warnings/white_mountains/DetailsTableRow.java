package org.jenkinsci.test.acceptance.plugins.warnings.white_mountains;

import org.openqa.selenium.WebElement;

/**
 * Representation for the details row which can be toggled by clicking the icon in the Details column on a issues-table
 * row.
 *
 * @author Stephan Plöderl
 */
public class DetailsTableRow extends AbstractIssuesTableRow {
    private final String details;

    /**
     * Creates a new representation for a issues-table details row.
     *
     * @param row
     *         the WebElement representing the row.
     */
    DetailsTableRow(final WebElement row) {
        this.details = row.getText();
    }

    /**
     * Returns the text displayed in this row.
     *
     * @return the text
     */
    public String getDetails() {
        return details;
    }
}
