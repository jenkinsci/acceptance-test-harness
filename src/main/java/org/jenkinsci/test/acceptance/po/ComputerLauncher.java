package org.jenkinsci.test.acceptance.po;

/**
 * Configuration fragment for computer launcher.
 *
 * @author Kohsuke Kawaguchi
 * @see DumbSlave#setLauncher(Class)
 */
public abstract class ComputerLauncher extends PageArea {
    protected ComputerLauncher(PageObject context, String path) {
        super(context, path);
    }
}
