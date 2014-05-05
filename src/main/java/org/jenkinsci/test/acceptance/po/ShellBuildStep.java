package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.NoSuchElementException;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Execute shell")
public class ShellBuildStep extends BuildStepImpl {

    public ShellBuildStep(Job parent, String path) {
        super(parent, path);
    }

    public void command(String command) {
        try {

            control("command").set(command);
        }
        catch (NoSuchElementException e) {

            new CodeMirror(this, "command").set(command);
        }
    }
}
