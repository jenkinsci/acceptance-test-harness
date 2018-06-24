package org.jenkinsci.test.acceptance.plugins.warnings;

import org.assertj.core.api.AbstractAssert;
import org.jenkinsci.test.acceptance.plugins.warnings.SummaryPage.SummaryBoxPageArea;

/**
 * Assertion class of {@link SummaryBoxPageArea}.
 *
 * @author Manuel Hampp
 * @author Michaela Reitschuster
 * @author Alexandra Wenzel
 *
 */
public class SummaryBoxPageAreaAssert extends AbstractAssert<SummaryBoxPageAreaAssert, SummaryPage.SummaryBoxPageArea> {

    public SummaryBoxPageAreaAssert(SummaryPage.SummaryBoxPageArea summaryBoxPageArea) {
        super(summaryBoxPageArea, SummaryBoxPageAreaAssert.class);
    }

    public static SummaryBoxPageAreaAssert assertThat(SummaryPage.SummaryBoxPageArea actual) {
        return new SummaryBoxPageAreaAssert(actual);
    }

    public SummaryBoxPageAreaAssert hasWarningDiv() {
        isNotNull();
        if (actual.getWarningDiv() == null) {
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
