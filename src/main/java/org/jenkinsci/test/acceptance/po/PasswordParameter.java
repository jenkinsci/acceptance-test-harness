package org.jenkinsci.test.acceptance.po;

import hudson.util.VersionNumber;

@Describable("Password Parameter")
public class PasswordParameter extends Parameter {

    public PasswordParameter(Job job, String path) {
        super(job, path);
    }

    @Override
    public void fillWith(Object v) {
        control("value").set(v.toString());
    }

    //since JENKINS-61808 and jenkins 2.236 the path for "Default" input changed
    @Override
    public Parameter setDefault(String value) {
        if (injector.getInstance(Jenkins.class).getVersion().isOlderThan(new VersionNumber("2.236"))){
            return super.setDefault(value);
        } else {
            //the control path changed after JENKINS-61808 and 2.236
            control("defaultValueAsSecret").set(value);
            return this;
        }
    }
}
