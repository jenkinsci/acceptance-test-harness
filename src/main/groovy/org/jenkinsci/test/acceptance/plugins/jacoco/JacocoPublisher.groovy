package org.jenkinsci.test.acceptance.plugins.jacoco

import org.jenkinsci.test.acceptance.geb.proxies.PostBuildStepProxy
import org.jenkinsci.test.acceptance.po.Describable
import org.jenkinsci.test.acceptance.po.Job
import org.jenkinsci.test.acceptance.po.Page
import org.jenkinsci.test.acceptance.po.PostBuildStep

/**
 * Geb Page Object for the JacocoPublisher.
 *
 * @author christian.fritz
 */
@Describable("Record JaCoCo coverage report")
class JacocoPublisher extends Page {

    static content = {
        area { $("div[descriptorid='hudson.plugins.jacoco.JacocoPublisher']") }
        execPattern { area.find("input[path='/publisher/execPattern']") }
        classPattern { area.find("input[path='/publisher/classPattern']") }
        sourcePattern { area.find("input[path='/publisher/sourcePattern']") }
        inclusionPattern { area.find("input[path='/publisher/inclusionPattern']") }
        exclusionPattern { area.find("input[path=/publisher/exclusionPattern") }
        maximumInstructionCoverage { area.find("input[path='/publisher/maximumInstructionCoverage']") }
        maximumBranchCoverage { area.find("input[path='/publisher/maximumBranchCoverage']") }
        maximumComplexityCoverage { area.find("input[path=/publisher/maximumComplexityCoverage") }
        maximumLineCoverage { area.find("input[path='/publisher/maximumLineCoverage']") }
        maximumMethodCoverage { area.find("input[path='/publisher/maximumMethodCoverage']") }
        maximumClassCoverage { area.find("input[path='/publisher/maximumClassCoverage']") }
        minimumInstructionCoverage { area.find("input[path='/publisher/minimumInstructionCoverage']") }
        minimumBranchCoverage { area.find("input[path='/publisher/minimumBranchCoverage']") }
        minimumComplexityCoverage { area.find("input[path=/publisher/minimumComplexityCoverage") }
        minimumLineCoverage { area.find("input[path='/publisher/minimumLineCoverage']") }
        minimumMethodCoverage { area.find("input[path='/publisher/minimumMethodCoverage']") }
        minimummaximumClassCoverage { area.find("input[path='/publisher/minimumClassCoverage']") }
        changeBuildStatus { area.find("input[path='/publisher/changeBuildStatus']") }
    }

    @Delegate
    PostBuildStep parent;

    JacocoPublisher(Job job, String path) {
        super(job.injector)
        parent = new PostBuildStepProxy(job, path)
    }
}
