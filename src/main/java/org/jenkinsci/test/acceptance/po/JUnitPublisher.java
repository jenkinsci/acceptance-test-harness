package org.jenkinsci.test.acceptance.po;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Publish JUnit test result report")
public class JUnitPublisher extends PostBuildStep {
    public final Control testResults = control("testResults");

    public JUnitPublisher(Job parent, String path) {
        super(parent, path);
    }
}
