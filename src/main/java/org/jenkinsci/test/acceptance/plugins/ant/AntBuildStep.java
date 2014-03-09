package org.jenkinsci.test.acceptance.plugins.ant;

import org.jenkinsci.test.acceptance.po.BuildStep;
import org.jenkinsci.test.acceptance.po.BuildStepPageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * @author Kohsuke Kawaguchi
 */
@BuildStepPageObject("Invoke Ant")
public class AntBuildStep extends BuildStep {
    public final Control targets = control("targets");
    public final Control antName = control("antName");

    public AntBuildStep(Job parent, String path) {
        super(parent, path);
    }
}
