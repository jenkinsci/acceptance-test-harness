package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.NoSuchElementException;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Execute shell")
public class ShellBuildStep extends AbstractStep implements BuildStep {
    public ShellBuildStep(Job parent, String path) {
        super(parent, path);
    }

    public void command(String command) {
        try {
            new CodeMirror(this, "command").set(command);
        } catch (NoSuchElementException e) {
            control("command").set(command);
        }
    }
}
