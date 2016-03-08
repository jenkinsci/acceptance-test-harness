package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.NoSuchElementException;

@Describable("Execute Windows batch command")
public class BatchBuildStep extends AbstractStep implements BuildStep {

    public BatchBuildStep(Job parent, String path) {
        super(parent, path);
    }

    public void command(String command) {
        try {

            control("command").set(command);
        } catch (NoSuchElementException e) {

            new CodeMirror(this, "command").set(command);
        }
    }
}
