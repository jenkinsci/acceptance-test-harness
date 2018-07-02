package plugins.warnings.assertions;

import static org.assertj.core.api.Assertions.*;

/**
 * Abstract representation of a table row of the issues-table.
 *
 * @author Stephan Plöderl
 */
public abstract class AbstractIssuesTableRow {
    /**
     * Returns this row as an instance of a specific sub class of AbstractIssuesTableRow
     *
     * @param actualClass
     *         the class to which the table row shall be converted to
     *
     * @return the row
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractIssuesTableRow> T getAs(Class<T> actualClass) {
        assertThat(this).isInstanceOf(actualClass);
        return (T) this;
    }
}
