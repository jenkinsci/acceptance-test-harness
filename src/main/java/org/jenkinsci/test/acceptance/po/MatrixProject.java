package org.jenkinsci.test.acceptance.po;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Injector;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
@JobPageObject("hudson.matrix.MatrixProject")
public class MatrixProject extends Job {
    // config page objects
    public final Control addAxis = control(by.button("Add axis"));
    public final Control runSequentially = control("/executionStrategy/runSequentially");

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

        return newInstance(type,this,path);
    }

    @Override
    public MatrixBuild getLastBuild() {
        return super.getLastBuild().as(MatrixBuild.class);
    }

    @Override
    public MatrixBuild build(int buildNumber) {
        return super.build(buildNumber).as(MatrixBuild.class);
    }

    public List<MatrixConfiguration> getConfigurations() {
        List<MatrixConfiguration> r = new ArrayList<>();
        for (JsonNode n : getJson().get("activeConfigurations")) {
            r.add(getConfiguration(n.get("name").asText()));
        }
        return r;
    }

    private MatrixConfiguration getConfiguration(String name) {
        return new MatrixConfiguration(injector, url(name+'/'), name);
    }
}
