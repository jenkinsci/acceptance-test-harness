package org.jenkinsci.test.acceptance.plugins.pmd;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginPostBuildStep;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Publish PMD analysis results")
public class PmdPublisher extends AbstractCodeStylePluginPostBuildStep  {
    public final Control advanced = control("advanced-button");
    public final Control canRunOnFailed = control("canRunOnFailed");

    public PmdPublisher(Job parent, String path) {
        super(parent, path);
    }
}
