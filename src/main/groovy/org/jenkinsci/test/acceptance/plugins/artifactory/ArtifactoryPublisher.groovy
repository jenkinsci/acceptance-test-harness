package org.jenkinsci.test.acceptance.plugins.artifactory

import org.jenkinsci.test.acceptance.geb.proxies.PostBuildStepProxy
import org.jenkinsci.test.acceptance.po.AbstractStep
import org.jenkinsci.test.acceptance.po.BuildStep
import org.jenkinsci.test.acceptance.po.Describable
import org.jenkinsci.test.acceptance.po.Job
import org.jenkinsci.test.acceptance.po.Page
import org.jenkinsci.test.acceptance.po.PageAreaImpl
import org.jenkinsci.test.acceptance.po.PostBuildStep
import org.jenkinsci.test.acceptance.po.Step

/**
 * Geb Page Object for the Artifactory Publisher.
 * @author Eli Givoni
 */
@Describable("Deploy artifacts to Artifactory")
class ArtifactoryPublisher extends Page {
    static content = {
        area { $("div[descriptorid='org.jfrog.hudson.ArtifactoryRedeployPublisher']") }
        refreshRepoButton { area.find('button', text: 'Refresh') }
    }
    @Delegate
    PostBuildStep parent;

    ArtifactoryPublisher(Job job, String path) {
        super(job.injector)
        parent = new PostBuildStepProxy(job, path)
    }
}
