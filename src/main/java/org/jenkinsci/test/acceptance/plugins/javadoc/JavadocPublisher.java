package org.jenkinsci.test.acceptance.plugins.javadoc;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Publish Javadoc")
public class JavadocPublisher extends PostBuildStep {
    public final Control javadocDir = control("javadocDir");

    public JavadocPublisher(Job parent, String path) {
        super(parent, path);
    }

}
