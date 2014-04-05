package org.jenkinsci.test.acceptance.plugins.description_setter;

import org.jenkinsci.test.acceptance.po.BuildStepPageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * @author Kohsuke Kawaguchi
 */
@BuildStepPageObject("Set build description")
public class BuildDescriptionSetter extends PostBuildStep {
    public BuildDescriptionSetter(Job parent, String path) {
        super(parent, path);
    }

    public final Control description = control("description");
    public final Control regexp = control("regexp");

}
