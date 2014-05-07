package org.jenkinsci.test.acceptance.plugins.checkstyle;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginPostBuildStep;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Publish Checkstyle analysis results")
public class CheckstylePublisher extends AbstractCodeStylePluginPostBuildStep {

    public CheckstylePublisher(Job parent, String path) {
        super(parent, path);
    }
}
