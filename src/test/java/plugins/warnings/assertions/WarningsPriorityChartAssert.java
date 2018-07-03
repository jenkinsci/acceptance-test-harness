package plugins.warnings.assertions;

import java.util.Objects;

import org.assertj.core.api.AbstractAssert;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.SeverityChart;

/**
 * Assertions for {@link SeverityChart}.
 *
 * @author Anna-Maria Hardi
 * @author Elvira Hauer
 */
@SuppressWarnings({"ParameterHidesMemberVariable", "NonBooleanMethodNameMayNotStartWithQuestion"})
public class WarningsPriorityChartAssert extends AbstractAssert<WarningsPriorityChartAssert, SeverityChart> {
    private static final String EXPECTED_BUT_WAS_MESSAGE = "%nExpecting %s of:%n <%s>%nto be:%n <%s>%nbut was:%n <%s>.";

    /**
     * Creates a new {@link WarningsPriorityChartAssert} to make assertions on actual {@link SeverityChart}.
     *
     * @param actual
     *         the trend chart we want to make assertions on
     */
    WarningsPriorityChartAssert(final SeverityChart actual) {
        super(actual, WarningsPriorityChartAssert.class);
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
    public static WarningsPriorityChartAssert assertThat(final SeverityChart actual) {
        return new WarningsPriorityChartAssert(actual);
    }

    /**
     * Verifies the issues with low priority of the {@link SeverityChart} instance are equal to the expected ones.
     *
     * @param expectedNumberOfIssues
     *         the expected number of issues.
     *
     * @return this assertion object.
     * @throws AssertionError
     *         if the actual number of issues is not equal to the given ones.
     */
    public WarningsPriorityChartAssert hasLowPriority(final int expectedNumberOfIssues) {
        isNotNull();

        if (!Objects.equals(actual.getLowPriority(), expectedNumberOfIssues)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "issues", actual, expectedNumberOfIssues, actual.getLowPriority());
        }
        return this;
    }

    /**
     * Verifies the issues with normal priority of the {@link SeverityChart} instance are equal to the expected ones.
     *
     * @param expectedNumberOfIssues
     *         the expected number of issues.
     *
     * @return this assertion object.
     * @throws AssertionError
     *         if the actual number of issues is not equal to the given ones.
     */
    public WarningsPriorityChartAssert hasNormalPriority(final int expectedNumberOfIssues) {
        isNotNull();

        if (!Objects.equals(actual.getNormalPriority(), expectedNumberOfIssues)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "issues", actual, expectedNumberOfIssues, actual.getNormalPriority());
        }
        return this;
    }

    /**
     * Verifies the issues with high priority of the {@link SeverityChart} instance are equal to the expected ones.
     *
     * @param expectedNumberOfIssues
     *         the expected number of issues.
     *
     * @return this assertion object.
     * @throws AssertionError
     *         if the actual number of issues is not equal to the given ones.
     */
    public WarningsPriorityChartAssert hasHighPriority(final int expectedNumberOfIssues) {
        isNotNull();

        if (!Objects.equals(actual.getHighPriority(), expectedNumberOfIssues)) {
            failWithMessage(EXPECTED_BUT_WAS_MESSAGE, "issues", actual, expectedNumberOfIssues, actual.getHighPriority());
        }
        return this;
    }
}
