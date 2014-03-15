package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import cucumber.api.DataTable;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.Base64;
import org.jenkinsci.test.acceptance.junit.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

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

    public <T extends BuildStep> T addBuildStep(Class<T> type) {
        return addStep(type,"builder");
    }

    public <T extends PostBuildStep> T addPublisher(Class<T> type) {
        return addStep(type,"publisher");
    }

    private <T extends Step> T addStep(Class<T> type, String section) {
        ensureConfigPage();

        String caption = type.getAnnotation(BuildStepPageObject.class).value();

        selectDropdownMenu(caption, find(by.path("/hetero-list-add[%s]",section)));
        String path = last(by.xpath("//div[@name='%s']", section)).getAttribute("path");

        try {
            return type.getConstructor(Job.class,String.class).newInstance(this,path);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    public ShellBuildStep addShellStep(String shell) {
        ShellBuildStep step = addBuildStep(ShellBuildStep.class);
        step.setCommand(shell);
        return step;
    }

    public void addCreateFileStep(String name, String content) {
        addShellStep(String.format("cat > %s << ENDOFFILE\n%s\nENDOFFILE",name,content));
    }

    /**
     * Adds a shell step that copies a resource inside the test project into a file on the build machine.
     */
    public void copyResource(Resource resource, String fileName) {
        try (InputStream in=resource.asInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gz = new GZIPOutputStream(out);
            IOUtils.copy(in, gz);
            gz.close();

            addShellStep(String.format("base64 -d | gunzip > %s << ENDOFFILE\n%s\nENDOFFILE",
                    fileName, new String(Base64.encodeBase64Chunked(out.toByteArray()))));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public URL getBuildUrl() {
        return url("build?delay=0sec");
    }

    public Build queueBuild(DataTable table) {
        Map<String,String> params = new HashMap<>();
        for (List<String> row : table.raw()) {
            params.put(row.get(0), row.get(1));
        }
        return queueBuild(params);
    }

    public Build queueBuild() {
        return queueBuild(Collections.<String,Object>emptyMap());
    }

    public Build queueBuild(Map<String,?> params) {
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

    public Build build(int buildNumber) {
        return new Build(this,buildNumber);
    }

    public Build getLastBuild() {
        return new Build(this,"lastBuild");
    }

    public <T extends Parameter> T addParameter(Class<T> type) {
        ensureConfigPage();

        String displayName = type.getAnnotation(ParameterPageObject.class).value();

        check(find(by.xpath("//input[@name='parameterized']")));
        selectDropdownMenu(displayName, find(by.xpath("//button[text()='Add Parameter']")));
//        find(xpath("//button[text()='Add Parameter']")).click();
//        find(xpath("//a[text()='%s']",displayName)).click();

        sleep(500);

        String path = last(by.xpath("//div[@name='parameter']")).getAttribute("path");

        try {
            T p = type.getConstructor(Job.class,String.class).newInstance(this,path);
            parameters.add(p);
            return p;
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    public void disable() {
        check("disable");
    }

    public int getNextBuildNumber() {
        return getJson().get("nextBuildNumber").intValue();
    }

    public void useCustomWorkspace(String ws) {
        ensureConfigPage();
        clickButton("Advanced...");

        check(find(by.path("/customWorkspace")));
        find(by.path("/customWorkspace/directory")).sendKeys(ws);
    }
}
