package org.jenkinsci.test.acceptance.plugins.jacoco

import org.jenkinsci.test.acceptance.po.*

/**
 * Geb Page Object for the JacocoPublisher.
 *
 * @author christian.fritz
 */
@Describable("Publish PMD analysis results")
class JacocoPublisher extends Page {

    static content = {
        advancedButton { $("button[path=/publisher/advanced-button]") }
    }

    @Delegate(includeTypes = PostBuildStep)
    AbstractStep parent;


    JacocoPublisher(Job job, String path) {
        super(job.injector)
        parent = new AbstractStep(job, path) {}
    }
}
