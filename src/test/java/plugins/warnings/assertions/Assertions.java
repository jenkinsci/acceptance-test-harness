package plugins.warnings.assertions;

import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.AbstractNonDetailsIssuesTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.DryIssuesTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.DefaultWarningsTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.DetailsTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.IssuesTable;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.SummaryPage.SummaryBoxPageArea;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.WarningsPriorityChart;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.WarningsTrendChart;
import org.jenkinsci.test.acceptance.po.MessageBox;

/**
 * Custom assertions for ui tests.
 *
 * @author Michaela Reitschuster
 * @author Alexandra Wenzel
 * @author Manuel Hampp
 */
public class Assertions extends org.assertj.core.api.Assertions {
    /**
     * Creates a new {@link SummaryBoxPageAreaAssert} to make assertions on actual {@link SummaryBoxPageArea}.
     *
     * @param actual
     *         the issue we want to make assertions on
     *
     * @return a new {@link SummaryBoxPageAreaAssert}
     */
    public static SummaryBoxPageAreaAssert assertThat(final SummaryBoxPageArea actual) {
        return new SummaryBoxPageAreaAssert(actual);
    }

    /**
     * An entry point for {@link WarningsPriorityChartAssert} to follow AssertJ standard {@code assertThat()}. With a static import,
     * one can write directly {@code assertThat(myIssues)} and get a specific assertion with code completion.
     *
     * @param actual
     *         the issues we want to make assertions on
     *
     * @return a new {@link WarningsPriorityChartAssert}
     */
    public static WarningsPriorityChartAssert assertThat(final WarningsPriorityChart actual) {
        return new WarningsPriorityChartAssert(actual);
    }

    /**
     * An entry point for {@link WarningsTrendChartAssert} to follow AssertJ standard {@code assertThat()}. With a
     * static import, one can write directly {@code assertThat(myIssues)} and get a specific assertion with code
     * completion.
     *
     * @param actual
     *         the issues we want to make assertions on
     *
     * @return a new {@link WarningsTrendChartAssert}
     */
    public static WarningsTrendChartAssert assertThat(final WarningsTrendChart actual) {
        return new WarningsTrendChartAssert(actual);
    }

    public static MessageBoxAssert assertThat(MessageBox actual) {
        return new MessageBoxAssert(actual);
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
     * Creates a new instance of <code>{@link DryNonDetailsIssuesTableRowAssert}</code>.
     *
     * @param actual
     *         the actual value.
     *
     * @return the created assertion object.
     */
    @org.assertj.core.util.CheckReturnValue
    public static DryNonDetailsIssuesTableRowAssert assertThat(DryIssuesTableRow actual) {
        return new DryNonDetailsIssuesTableRowAssert(actual);
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
            IssuesTable actual) {
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