package org.jenkinsci.test.acceptance.po;

/**
 * @author Kohsuke Kawaguchi
 */
@ParameterPageObject("String Parameter")
public class StringParameter extends Parameter {
    public StringParameter(Job job, String path) {
        super(job, path);
    }

    @Override
    public void fillWith(Object v) {
        control("value").sendKeys(v.toString());
    }
}
