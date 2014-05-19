package org.jenkinsci.test.acceptance.plugins.nodelabelparameter;

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
        control("label").set(v.toString());
    }
}
