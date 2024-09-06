package org.jenkinsci.test.acceptance.po;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Injector;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("hudson.matrix.MatrixProject")
public class MatrixProject extends Job {
    public final Control addAxis = control(by.button("Add axis"));
    public final Control runSequentially = control("/executionStrategy/runSequentially");

    public final Control hasTouchStoneCombinationFilter = control("/executionStrategy/hasTouchStoneCombinationFilter");
    public final Control touchStoneCombinationFilter = control("/executionStrategy/touchStoneCombinationFilter");
    public final Control touchStoneResultCondition = control("/executionStrategy/touchStoneResultCondition");

    public final Control hasCombinationFilter = control("/hasCombinationFilter");
    public final Control combinationFilter = control("/hasCombinationFilter/combinationFilter");

    public MatrixProject(Injector injector, URL url, String name) {
        super(injector, url, name);
    }

    public void addUserAxis(String name, String value) {
        TextAxis a = addAxis(TextAxis.class);
        a.name.set(name);
        a.valueString.set(value);
    }

    public <T extends Axis> T addAxis(final Class<T> type) {
        ensureConfigPage();
        String path = createPageArea("/axis", () -> addAxis.selectDropdownMenu(type));

        return newInstance(type, this, path);
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

    public MatrixConfiguration getConfiguration(String name) {
        return new MatrixConfiguration(injector, url(name + '/'), name);
    }

    public void setTouchStoneBuild(String filter, String result) {
        ensureConfigPage();
        hasTouchStoneCombinationFilter.check();
        touchStoneCombinationFilter.set(filter);
        touchStoneResultCondition.select(result);
    }

    public void setCombinationFilter(String filter) {
        ensureConfigPage();
        hasCombinationFilter.check();
        combinationFilter.set(filter);
    }
}
