package org.jenkinsci.test.acceptance.plugins.warnings.white_mountains;

/**
 * Abstract representation of a table row of the issues-table.
 *
 * @author Stephan Plöderl
 */
public abstract class AbstractIssuesTableRow {
    /**
     * Returns this row as an instance of a specific sub class of {@link AbstractIssuesTableRow}.
     *
     * @param actualClass
     *         the class to which the table row shall be converted to
     * @param <T>
     *         actual type of the row
     *
     * @return the row
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractIssuesTableRow> T getAs(Class<T> actualClass) {
        return actualClass.cast(this);
    }
}
