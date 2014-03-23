package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;

/**
 * Page area for matrix axis.
 *
 * @author Kohsuke Kawaguchi
 * @see MatrixProject#addAxis(Class)
 */
public abstract class Axis extends PageArea {
    public final Control name = control("name");

    protected Axis(Injector injector, String path) {
        super(injector, path);
    }

    protected Axis(PageObject context, String path) {
        super(context, path);
    }
}
