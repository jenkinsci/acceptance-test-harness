package org.jenkinsci.test.acceptance.po;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable({"String Parameter", "hudson.model.StringParameterDefinition"})
public class StringParameter extends Parameter {
    public StringParameter(Job job, String path) {
        super(job, path);
    }

    @Override
    public void fillWith(Object v) {
        control("value").set(v.toString());
    }
}
