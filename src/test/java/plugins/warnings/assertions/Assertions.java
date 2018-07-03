package plugins.warnings.assertions;

import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.AbstractNonDetailsIssuesTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.ConsoleLogView;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.DefaultWarningsTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.DetailsTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.DryIssuesTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.IssuesTable;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.LogMessagesView;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.SourceView;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.AnalysisSummary.SummaryBoxPageArea;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.SeverityChart;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.TrendChart;

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
    @org.assertj.core.util.CheckReturnValue
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
    @org.assertj.core.util.CheckReturnValue
    public static WarningsPriorityChartAssert assertThat(final SeverityChart actual) {
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
    @org.assertj.core.util.CheckReturnValue
    public static WarningsTrendChartAssert assertThat(final TrendChart actual) {
        return new WarningsTrendChartAssert(actual);
    }

    @org.assertj.core.util.CheckReturnValue
    public static MessageBoxAssert assertThat(LogMessagesView actual) {
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
   
    /**
     * An entry point for {@link ConsoleLogViewAssert} to follow AssertJ standard {@code assertThat()}. With a static import,
     * one can write directly {@code assertThat(view)} and get a specific assertion with code completion.
     *
     * @param actual
     *         the view we want to make assertions on
     *
     * @return a new {@link ConsoleLogViewAssert}
     */
    @org.assertj.core.util.CheckReturnValue
    public static ConsoleLogViewAssert assertThat(final ConsoleLogView actual) {
        return new ConsoleLogViewAssert(actual);
    }

    /**
     * An entry point for {@link SourceViewAssert} to follow AssertJ standard {@code assertThat()}. With a static import,
     * one can write directly {@code assertThat(view)} and get a specific assertion with code completion.
     *
     * @param actual
     *         the issues we want to make assertions on
     *
     * @return a new {@link SourceViewAssert}
     */
    @org.assertj.core.util.CheckReturnValue
    public static SourceViewAssert assertThat(final SourceView actual) {
        return new SourceViewAssert(actual);
    }
}