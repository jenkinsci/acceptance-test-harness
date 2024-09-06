package org.jenkinsci.test.acceptance.plugins.javadoc;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Publish Javadoc")
public class JavadocPublisher extends AbstractStep implements PostBuildStep {
    public final Control javadocDir = control("javadocDir");
    public final Control keepAll = control("keepAll");

    public JavadocPublisher(Job parent, String path) {
        super(parent, path);
    }
}
