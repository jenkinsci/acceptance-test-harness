package plugins.warnings.assertions;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.TrendChart;

/**
 * Assertions for {@link TrendChart}.
 *
 * @author Anna-Maria Hardi
 * @author Elvira Hauer
 */
@SuppressWarnings({"ParameterHidesMemberVariable", "NonBooleanMethodNameMayNotStartWithQuestion"})
public class WarningsTrendChartAssert extends AbstractAssert<WarningsTrendChartAssert, TrendChart> {
    private static final String EXPECTED_BUT_WAS_MESSAGE = "%nExpecting %s of:%n <%s>%nto be:%n <%s>%nbut was:%n <%s>.";

    /**
     * Creates a new {@link WarningsTrendChartAssert} to make assertions on actual {@link TrendChart}.
     *
     * @param actual
     *         the trend chart we want to make assertions on
     */
    WarningsTrendChartAssert(final TrendChart actual) {
        super(actual, WarningsTrendChartAssert.class);
    }

    /**
     * An entry point for {@link WarningsTrendChartAssert} to follow AssertJ standard {@code assertThat()}. With a
     * static import, one can write directly {@code assertThat(trendChart)} and get a specific assertion with code
     * completion.
     *
     * @param actual
     *         the issues we want to make assertions on
     *
     * @return a new {@link WarningsTrendChartAssert}
     */
    public static WarningsTrendChartAssert assertThat(final TrendChart actual) {
        return new WarningsTrendChartAssert(actual);
    }

    /**
     * Verifies the new issues of the {@link TrendChart} instance are equal to the expected ones.
     *
     * @param expectedNumberOfIssues
     *         the expected number of issues.
     *
     * @return this assertion object.
     * @throws AssertionError
     *         if the actual number of issues is not equal to the given ones.
     */
    public WarningsTrendChartAssert hasNewIssues(final int expectedNumberOfIssues) {
        isNotNull();

        if (!Objects.equals(actual.getNewIssues(), expectedNumberOfIssues)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "issues", actual, expectedNumberOfIssues, actual.getNewIssues());
        }
        return this;
    }

    /**
     * Verifies the fixed issues of the {@link TrendChart} instance are equal to the expected ones.
     *
     * @param expectedNumberOfIssues
     *         the expected number of issues.
     *
     * @return this assertion object.
     * @throws AssertionError
     *         if the actual number of issues is not equal to the given ones.
     */
    public WarningsTrendChartAssert hasFixedIssues(final int expectedNumberOfIssues) {
        isNotNull();

        if (!Objects.equals(actual.getFixedIssues(), expectedNumberOfIssues)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "issues", actual, expectedNumberOfIssues,
                    actual.getFixedIssues());
        }
        return this;
    }

    /**
     * Verifies the outstanding issues of the {@link TrendChart} instance are equal to the expected ones.
     *
     * @param expectedNumberOfIssues
     *         the expected number of issues.
     *
     * @return this assertion object.
     * @throws AssertionError
     *         if the actual number of issues is not equal to the given ones.
     */
    public WarningsTrendChartAssert hasOutstandingIssues(final int expectedNumberOfIssues) {
        isNotNull();

        if (!Objects.equals(actual.getOutstandingIssues(), expectedNumberOfIssues)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "issues", actual, expectedNumberOfIssues,
                    actual.getOutstandingIssues());
        }
        return this;
    }
}
