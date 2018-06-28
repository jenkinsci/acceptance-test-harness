package org.jenkinsci.test.acceptance.plugins.warnings.assertions;

import org.jenkinsci.test.acceptance.plugins.warnings.issues_table.AbstractNonDetailsIssuesTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.issues_table.DRYIssuesTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.issues_table.DefaultWarningsTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.issues_table.DetailsTableRow;

/**
 * Entry point for assertions of different data types. Each method in this class is a static factory for the
 * type-specific assertion objects.
 */
@javax.annotation.Generated(value = "assertj-assertions-generator")
public class Assertions extends org.assertj.core.api.Assertions {

    /**
     * Creates a new <code>{@link Assertions}</code>.
     */
    protected Assertions() {
        // empty
    }

    /**
     * Creates a new instance of <code>{@link AbstractNonDetailsIssuesTableRowAssert}</code>.
     *
     * @param actual
     *         the actual value.
     *
     * @return the created assertion object.
     */
    @org.assertj.core.util.CheckReturnValue
    public static AbstractNonDetailsIssuesTableRowAssert assertThat(AbstractNonDetailsIssuesTableRow actual) {
        return new AbstractNonDetailsIssuesTableRowAssert(actual);
    }

    /**
     * Creates a new instance of <code>{@link DRYNonDetailsIssuesTableRowAssert}</code>.
     *
     * @param actual
     *         the actual value.
     *
     * @return the created assertion object.
     */
    @org.assertj.core.util.CheckReturnValue
    public static DRYNonDetailsIssuesTableRowAssert assertThat(DRYIssuesTableRow actual) {
        return new DRYNonDetailsIssuesTableRowAssert(actual);
    }

    /**
     * Creates a new instance of <code>{@link DetailsTableRowAssert}</code>.
     *
     * @param actual
     *         the actual value.
     *
     * @return the created assertion object.
     */
    @org.assertj.core.util.CheckReturnValue
    public static DetailsTableRowAssert assertThat(DetailsTableRow actual) {
        return new DetailsTableRowAssert(actual);
    }

    /**
     * Creates a new instance of <code>{@link IssuesTableAssert}</code>.
     *
     * @param actual
     *         the actual value.
     *
     * @return the created assertion object.
     */
    @org.assertj.core.util.CheckReturnValue
    public static IssuesTableAssert assertThat(
            org.jenkinsci.test.acceptance.plugins.warnings.issues_table.IssuesTable actual) {
        return new IssuesTableAssert(actual);
    }

    /**
     * Creates a new instance of <code>{@link DefaultWarningsTableRowAssert}</code>.
     *
     * @param actual
     *         the actual value.
     *
     * @return the created assertion object.
     */
    @org.assertj.core.util.CheckReturnValue
    public static DefaultWarningsTableRowAssert assertThat(DefaultWarningsTableRow actual) {
        return new DefaultWarningsTableRowAssert(actual);
    }
}
