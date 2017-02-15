package org.jenkinsci.test.acceptance.po;

/**
 * @author Scott Hebert
 */
@Describable("Label expression")
public class LabelExpressionAxis extends Axis {
    public LabelExpressionAxis(PageObject context, String path) {
        super(context, path);
    }

    public final Control values = control("values");
}
