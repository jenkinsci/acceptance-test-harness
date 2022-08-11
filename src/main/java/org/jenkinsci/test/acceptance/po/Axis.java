package org.jenkinsci.test.acceptance.po;

/**
 * Page area for matrix axis.
 * <p>
 * Use {@link Describable} annotation to register an implementation.
 *
 * @author Kohsuke Kawaguchi
 * @see MatrixProject#addAxis(Class)
 */
public abstract class Axis extends PageAreaImpl {
    public final Control name = control("name");

    protected Axis(PageObject context, String path) {
        super(context, path);
    }
}
