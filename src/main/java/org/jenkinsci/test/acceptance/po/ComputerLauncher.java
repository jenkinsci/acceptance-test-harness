package org.jenkinsci.test.acceptance.po;

/**
 * Configuration fragment for computer launcher.
 * <p>
 * Use {@link Describable} annotation to register an implementation.
 *
 * @author Kohsuke Kawaguchi
 * @see DumbSlave#setLauncher(Class)
 */
public abstract class ComputerLauncher extends PageAreaImpl {
    protected ComputerLauncher(PageObject context, String path) {
        super(context, path);
    }
}
