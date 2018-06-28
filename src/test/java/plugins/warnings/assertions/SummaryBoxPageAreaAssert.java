package plugins.warnings.assertions;

import org.assertj.core.api.AbstractAssert;
import org.jenkinsci.test.acceptance.plugins.warnings.SummaryPage.SummaryBoxPageArea;

/**
 * Assertion class of {@link SummaryBoxPageArea}.
 *
 * @author Manuel Hampp
 * @author Michaela Reitschuster
 * @author Alexandra Wenzel
 */
public class SummaryBoxPageAreaAssert extends AbstractAssert<SummaryBoxPageAreaAssert, SummaryBoxPageArea> {

    public SummaryBoxPageAreaAssert(SummaryBoxPageArea summaryBoxPageArea) {
        super(summaryBoxPageArea, SummaryBoxPageAreaAssert.class);
    }

    public static SummaryBoxPageAreaAssert assertThat(SummaryBoxPageArea actual) {
        return new SummaryBoxPageAreaAssert(actual);
    }

    public SummaryBoxPageAreaAssert hasSummary() {
        isNotNull();
        if (actual.getSummary() == null) {
            failWithMessage("Summary element does not exist.");
        }
        return this;
    }

    public SummaryBoxPageAreaAssert hasQualityGateState(String state) {
        isNotNull();
        if (!actual.getQualityGateResult().equals(state)) {
            failWithMessage("Qualitity gate has not the expected state", actual.getQualityGateResult(), state);
        }
        return this;
    }

}
