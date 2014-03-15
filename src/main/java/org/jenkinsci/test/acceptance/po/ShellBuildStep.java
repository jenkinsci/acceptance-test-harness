package org.jenkinsci.test.acceptance.po;

/**
 * @author Kohsuke Kawaguchi
 */
@BuildStepPageObject("Execute shell")
public class ShellBuildStep extends BuildStep {
    public ShellBuildStep(Job parent, String path) {
        super(parent, path);
    }

    public ShellBuildStep setCommand(String text) {
        control("command").set(text);
        return this;
    }
}
