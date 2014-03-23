package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;

import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
@JobPageObject("hudson.matrix.MatrixProject")
public class MatrixProject extends Job {
    // config page objects
    public final Control addAxis = control(by.button("Add axis"));


    public MatrixProject(Injector injector, URL url, String name) {
        super(injector, url, name);
    }

    public void addUserAxis(String name, String value) {
        ensureConfigPage();
        addAxis.click();
        selectDropdownMenu("User-defined Axis",addAxis.resolve());
    }
}
