package org.jenkinsci.test.acceptance.plugins.nodelabelparameter;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.Parameter;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Label")
public class LabelParameter extends Parameter {
    public LabelParameter(Job job, String path) {
        super(job, path);
    }

    @Override
    public void fillWith(Object v) {
        Control control = control("value");
        // TODO in some cases the LabelParameter is simple text entry, in others it is a drop down
        control.set(v.toString());
    }
}
