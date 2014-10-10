package org.jenkinsci.test.acceptance.plugins.artifactory

import org.jenkinsci.test.acceptance.po.AbstractStep
import org.jenkinsci.test.acceptance.po.BuildStep
import org.jenkinsci.test.acceptance.po.Control
import org.jenkinsci.test.acceptance.po.Job

/**
 * Created by eli on 10/10/14.
 */
class ArtifactoryResolverStep extends AbstractStep implements BuildStep{
    ArtifactoryResolverStep(Job parent, String path) {
        super(parent, path)
    }
}
