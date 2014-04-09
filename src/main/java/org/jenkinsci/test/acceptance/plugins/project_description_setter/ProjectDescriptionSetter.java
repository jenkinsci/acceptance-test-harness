package org.jenkinsci.test.acceptance.plugins.project_description_setter;

import org.jenkinsci.test.acceptance.po.BuildWrapper;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * @author Kohsuke Kawaguchi
 */
public class ProjectDescriptionSetter extends BuildWrapper {
    public final Control filename = control("projectDescriptionFilename");

    public ProjectDescriptionSetter(Job context) {
        super(context, "/org-jenkinsCi-plugins-projectDescriptionSetter-DescriptionSetterWrapper");
    }
}
