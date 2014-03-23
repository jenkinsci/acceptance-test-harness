package org.jenkinsci.test.acceptance.plugins.maven;

import org.jenkinsci.test.acceptance.po.BuildStep;
import org.jenkinsci.test.acceptance.po.BuildStepPageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * @author Kohsuke Kawaguchi
 */
@BuildStepPageObject("Invoke top-level Maven targets")
public class MavenBuildStep extends BuildStep {
    public final Control targets = control("targets");

    public MavenBuildStep(Job parent, String path) {
        super(parent, path);
    }



}
