package org.jenkinsci.test.acceptance.po;

@Describable("Password Parameter")
public class PasswordParameter extends Parameter {

    public PasswordParameter(Job job, String path) {
        super(job, path);
    }

    @Override
    public void fillWith(Object v) {
        control("value").set(v.toString());
    }

    @Override
    public Parameter setDefault(String value) {
        control("defaultValueAsSecret").set(value);
        return this;
    }
}
