package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import cucumber.api.DataTable;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
public class Job extends ContainerPageObject {
    public final String name;
    private List<Parameter> parameters = new ArrayList<>();

    public Job(Injector injector, URL url, String name) {
        super(injector,url);
        this.name = name;
    }

    public <T extends BuildStep> T addBuildStep(Class<T> type) throws Exception {
        return addStep(type,"builder");
    }

    public <T extends PostBuildStep> T addPublisher(Class<T> type) throws Exception {
        return addStep(type,"publisher");
    }

    private <T extends Step> T addStep(Class<T> type, String section) throws Exception {
        ensureConfigPage();

        String caption = type.getAnnotation(BuildStepPageObject.class).value();

        selectDropdownMenu(caption, find(by.path("/hetero-list-add[%s]",section)));
        String path = last(by.xpath("//div[@name='%s']", section)).getAttribute("path");

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

        check(find(by.xpath("//input[@name='parameterized']")));
        selectDropdownMenu(displayName, find(by.xpath("//button[text()='Add Parameter']")));
//        find(xpath("//button[text()='Add Parameter']")).click();
//        find(xpath("//a[text()='%s']",displayName)).click();

        Thread.sleep(500);

        String path = last(by.xpath("//div[@name='parameter']")).getAttribute("path");

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

    public void useCustomWorkspace(String ws) throws Exception {
        ensureConfigPage();
        clickButton("Advanced...");

        check(find(by.path("/customWorkspace")));
        find(by.path("/customWorkspace/directory")).sendKeys(ws);
    }
}
