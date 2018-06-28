package plugins.warnings.assertions;

import org.jenkinsci.test.acceptance.plugins.warnings.SummaryPage.SummaryBoxPageArea;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsPriorityChart;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsTrendChart;

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

}