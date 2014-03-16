package org.jenkinsci.test.acceptance.po;

/**
 * @author Kohsuke Kawaguchi
 */
@BuildStepPageObject("Execute shell")
public class ShellBuildStep extends BuildStep {
    public final Control command = control("command");

    public ShellBuildStep(Job parent, String path) {
        super(parent, path);
    }
}
