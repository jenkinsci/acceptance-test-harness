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
    public final Control runSequentially = control("executionStrategy/runSequentially");

    public MatrixProject(Injector injector, URL url, String name) {
        super(injector, url, name);
    }

    public void addUserAxis(String name, String value) {
        TextAxis a = addUserAxis(TextAxis.class);
        a.name.set(name);
        a.valueString.set(value);
    }

    public <T extends Axis> T addUserAxis(Class<T> type) {
        AxisPageObjecct a = type.getAnnotation(AxisPageObjecct.class);

        ensureConfigPage();
        selectDropdownMenu(a.value(), addAxis.resolve());

        String path = last(by.xpath("//div[@name='axis']")).getAttribute("path");

        try {
            return type.getConstructor(PageObject.class,String.class).newInstance(this,path);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }
}
