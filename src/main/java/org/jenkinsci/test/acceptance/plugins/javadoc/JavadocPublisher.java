package org.jenkinsci.test.acceptance.plugins.javadoc;

import org.jenkinsci.test.acceptance.po.*;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Publish Javadoc")
public class JavadocPublisher extends AbstractStep implements PostBuildStep {
    public final Control javadocDir = control("javadocDir");

    public JavadocPublisher(Job parent, String path) {
        super(parent, path);
    }

}
