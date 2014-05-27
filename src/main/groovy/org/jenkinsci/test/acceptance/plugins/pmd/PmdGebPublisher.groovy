package org.jenkinsci.test.acceptance.plugins.pmd

import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginPostBuildStep
import org.jenkinsci.test.acceptance.po.Describable
import org.jenkinsci.test.acceptance.po.Job
import org.jenkinsci.test.acceptance.po.Page

/**
 * Geb PageObject for PMD Publisher.
 *
 * It is compatible with the java {@link org.jenkinsci.test.acceptance.po.PageArea}
 * (acts as {@link AbstractCodeStylePluginPostBuildStep}).
 *
 * @author christian.fritz
 */
@Describable("Publish PMD analysis results")
class PmdGebPublisher extends Page {

    static content = {
        advancedButton { $("button[path=/publisher/advanced-button]") }
        pmdPlugin { $("div[descriptorid=hudson.plugins.pmd.PmdPublisher]") }
        thresholdInstableAll { pmdPlugin.find("input[path=/publisher/unstableTotalAll]") }
        thresholdInstableHigh { pmdPlugin.find("input[path=/publisher/unstableTotalHigh]") }
        thresholdInstableNormal { pmdPlugin.find("input[path=/publisher/unstableTotalNormal]") }
        thresholdInstableLow { pmdPlugin.find("input[path=/publisher/unstableTotalLow]") }
        thresholdFailingAll { pmdPlugin.find("input[path=/publisher/failedTotalAll]") }
        thresholdFailingHigh { pmdPlugin.find("input[path=/publisher/failedTotalHigh]") }
        thresholdFailingNormal { pmdPlugin.find("input[path=/publisher/failedTotalNormal]") }
        thresholdFailingLow { pmdPlugin.find("input[path=/publisher/failedTotalLow]") }
        runAlways { pmdPlugin.find("input[path=/publisher/canRunOnFailed]") }

    }
    @Delegate
    AbstractCodeStylePluginPostBuildStep parent;

    PmdGebPublisher(Job job, String path) {
        super(job.injector)
        parent = new AbstractCodeStylePluginPostBuildStep(job, path) {}
    }
}
