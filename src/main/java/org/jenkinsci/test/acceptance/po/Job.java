package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import cucumber.api.DataTable;
import org.jenkinsci.test.acceptance.cucumber.By2;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jenkinsci.test.acceptance.cucumber.By2.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class Job extends PageObject {
    public final String name;
    private List<Parameter> parameters = new ArrayList<>();

    public Job(Injector injector, URL url, String name) {
        super(injector,url);
        this.name = name;
    }

    public <T extends BuildStep> T addBuildStep(Class<T> type) throws Exception {
        ensureConfigPage();

        String caption = type.getAnnotation(BuildStepPageObject.class).value();

        selectDropdownMenu(caption, find(By2.path("/hetero-list-add[builder]")));
        String path = last(xpath("//div[@name='builder']")).getAttribute("path");

        return type.getConstructor(Job.class,String.class).newInstance(this,path);
    }

    public ShellBuildStep addShellStep(String shell) throws Exception {
        ShellBuildStep step = addBuildStep(ShellBuildStep.class);
        step.setCommand(shell);
        return step;
    }

    public URL getBuildUrl() throws Exception {
        return new URL(url,"build?delay=0sec");
    }

    public Build queueBuild(DataTable table) throws Exception {
        Map<String,String> params = new HashMap<>();
        for (List<String> row : table.raw()) {
            params.put(row.get(0), row.get(1));
        }
        return queueBuild(params);
    }

    public Build queueBuild() throws Exception {
        return queueBuild(Collections.<String,Object>emptyMap());
    }

    public Build queueBuild(Map<String,?> params) throws Exception {
        int nb = getJson().get("nextBuildNumber").intValue();
        visit(getBuildUrl());

        if (!parameters.isEmpty()) {
            for (Parameter def : parameters) {
                Object v = params.get(def.getName());
                if (v!=null)
                    def.fillWith(v);
            }
            clickButton("Build");
        }

        return build(nb).waitUntilStarted();
    }

    public Build build(int buildNumber) throws Exception {
        return new Build(this,buildNumber);
    }

    public Build getLastBuild() throws Exception {
        return new Build(this,"lastBuild");
    }

    public <T extends Parameter> T addParameter(Class<T> type) throws Exception {
        ensureConfigPage();

        String displayName = type.getAnnotation(ParameterPageObject.class).value();

        check(find(xpath("//input[@name='parameterized']")));
        selectDropdownMenu(displayName, find(xpath("//button[text()='Add Parameter']")));
//        find(xpath("//button[text()='Add Parameter']")).click();
//        find(xpath("//a[text()='%s']",displayName)).click();

        Thread.sleep(500);

        String path = last(xpath("//div[@name='parameter']")).getAttribute("path");

        T p = type.getConstructor(Job.class,String.class).newInstance(this,path);
        parameters.add(p);
        return p;
    }

    public void disable() {
        check("disable");
    }

    public int getNextBuildNumber() throws Exception {
        return getJson().get("nextBuildNumber").intValue();
    }
}
