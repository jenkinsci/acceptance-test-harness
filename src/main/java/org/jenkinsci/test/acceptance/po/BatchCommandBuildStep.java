package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.NoSuchElementException;

/**
 * BuildStep page object for a Windows Batch Command.
 */
@Describable("Execute Windows batch command")
public class BatchCommandBuildStep extends AbstractStep implements BuildStep {

    public BatchCommandBuildStep(Job parent, String path) {
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
