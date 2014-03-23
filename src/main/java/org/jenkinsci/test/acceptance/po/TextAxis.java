package org.jenkinsci.test.acceptance.po;

/**
 * @author Kohsuke Kawaguchi
 */
@AxisPageObjecct("User-defined Axis")
public class TextAxis extends Axis {
    public TextAxis(PageObject context, String path) {
        super(context, path);
    }

    public final Control valueString = control("valueString");
}
