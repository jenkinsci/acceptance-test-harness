package org.jenkinsci.test.acceptance.plugins.warnings;

import org.assertj.core.api.AbstractAssert;

/*
 * Created by Manuel Hampp
 * on 21.06.18
 *
 */
public class SummaryBoxPageAreaAssert extends AbstractAssert<SummaryBoxPageAreaAssert, StatusPage.SummaryBoxPageArea> {

    public SummaryBoxPageAreaAssert(StatusPage.SummaryBoxPageArea summaryBoxPageArea) {
        super(summaryBoxPageArea, SummaryBoxPageAreaAssert.class);
    }

    public static SummaryBoxPageAreaAssert assertThat(StatusPage.SummaryBoxPageArea actual) {
        return new SummaryBoxPageAreaAssert(actual);
    }

    public SummaryBoxPageAreaAssert hasWarningDiv() {
        isNotNull();
        if (actual.getWarningDiv() == null) {
            failWithMessage("Summary Element does not exists.");
        }
        return this;
    }
}
