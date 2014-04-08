package org.jenkinsci.test.acceptance.plugins.nodelabelparameter;

import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Label")
public class LabelParameter extends NodeParameter {
    public LabelParameter(Job job, String path) {
        super(job, path);
    }

    @Override
    public void fillWith(Object v) {
        control("label").set(v.toString());
    }
}
