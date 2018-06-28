package plugins.warnings.assertions;

import org.jenkinsci.test.acceptance.plugins.warnings.SummaryPage.SummaryBoxPageArea;

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
}