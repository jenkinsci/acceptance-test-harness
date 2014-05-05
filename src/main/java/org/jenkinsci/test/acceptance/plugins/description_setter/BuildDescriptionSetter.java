package org.jenkinsci.test.acceptance.plugins.description_setter;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStepImpl;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Set build description")
public class BuildDescriptionSetter extends PostBuildStepImpl {
    public BuildDescriptionSetter(Job parent, String path) {
        super(parent, path);
    }

    public final Control description = control("description");
    public final Control regexp = control("regexp");

}
