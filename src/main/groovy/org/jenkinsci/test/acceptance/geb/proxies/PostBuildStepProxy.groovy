package org.jenkinsci.test.acceptance.geb.proxies

import org.jenkinsci.test.acceptance.po.AbstractStep
import org.jenkinsci.test.acceptance.po.Job
import org.jenkinsci.test.acceptance.po.PostBuildStep

/**
 * Proxy class for the {@link PostBuildStep}.
 * @author christian.fritz
 */
class PostBuildStepProxy extends AbstractStep implements PostBuildStep {
    PostBuildStepProxy(Job parent, String path) {
        super(parent, path)
    }
}
