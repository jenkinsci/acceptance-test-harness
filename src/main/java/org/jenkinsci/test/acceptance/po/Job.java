package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;

import cucumber.api.DataTable;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.Base64;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.openqa.selenium.WebElement;
import org.zeroturnaround.zip.ZipUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class Job extends ContainerPageObject {
    public final String name;
    private List<Parameter> parameters = new ArrayList<>();

    public final Control concurrentBuild = control("/concurrentBuild");

    public Job(Injector injector, URL url, String name) {
        super(injector,url);
        this.name = name;
    }

    /**
     * "Casts" this object into a subtype by creating the specified type.
     */
    public <T extends Job> T as(Class<T> type) {
        if (type.isInstance(this))
            return type.cast(this);
        return newInstance(type, injector, url, name);
    }

    public <T extends Scm> T useScm(Class<T> type) {
        ensureConfigPage();

        WebElement radio = findCaption(type, new Finder<WebElement>() {
            @Override protected WebElement find(String caption) {
                return outer.find(by.radioButton(caption));
            }
        });

        check(radio);

        return newInstance(type, this, radio.getAttribute("path"));
    }

    public <T extends BuildStep> T addPreBuildStep(Class<T> type) {
        return addStep(type,"prebuilder");
    }

    public <T extends BuildStep> T addBuildStep(Class<T> type) {
        return addStep(type,"builder");
    }

    public <T extends PostBuildStep> T addPublisher(Class<T> type) {
        return addStep(type,"publisher");
    }

    private <T extends Step> T addStep(Class<T> type, String section) {
        ensureConfigPage();

        final WebElement dropDown = find(by.path("/hetero-list-add[%s]",section));
        findCaption(type, new Resolver() {
            @Override protected void resolve(String caption) {
                selectDropdownMenu(caption, dropDown);
            }
        });

        String path = last(by.xpath("//div[@name='%s']", section)).getAttribute("path");

        return newInstance(type, this, path);
    }

    public ShellBuildStep addShellStep(Resource res) {
        return addShellStep(res.asText());
    }

    public ShellBuildStep addShellStep(String shell) {
        ShellBuildStep step = addBuildStep(ShellBuildStep.class);
        step.command.set(shell);
        return step;
    }

    /**
     * Adds a shell step that creates a file of the given name in the workspace that has the specified content.
     */
    public void addCreateFileStep(String name, String content) {
        addShellStep(String.format("cat > %s << ENDOFFILE\n%s\nENDOFFILE",name,content));
    }

    /**
     * Adds a shell step that copies a resource inside the test project into a file on the build machine.
     *
     * Because there's no direct file system access to Jenkins master, we do this by packing file content in
     * base64 and put it as a heredoc in the shell script.
     */
    public void copyResource(Resource resource, String fileName) {
        try (InputStream in=resource.asInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try (OutputStream gz = new GZIPOutputStream(out)) {
                IOUtils.copy(in, gz);
            }

            // fileName can include path portion like foo/bar/zot
            addShellStep(String.format("(mkdir -p %1$s || true) && rm -r %1$s && base64 --decode << ENDOFFILE | gunzip > %1$s \n%2$s\nENDOFFILE",
                    fileName, new String(Base64.encodeBase64Chunked(out.toByteArray()))));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    public void copyResource(Resource resource) {
        copyResource(resource,resource.getName());
    }

    public void copyDir(Resource dir) {
        File tmp = null;
        try {
            tmp = File.createTempFile("jenkins-acceptance-tests", "dir");
            ZipUtil.pack(dir.asFile(), tmp);
            byte[] archive = IOUtils.toByteArray(new FileInputStream(tmp));

            addShellStep(String.format(
                    "base64 --decode << ENDOFFILE > archive.zip && unzip archive.zip \n%s\nENDOFFILE",
                    new String(Base64.encodeBase64Chunked(archive))
            ));
        } catch (IOException e) {
            throw new AssertionError(e);
        } finally {
            if (tmp != null) tmp.delete();
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

        check(find(by.xpath("//input[@name='parameterized']")));

        final WebElement dropDown = find(by.xpath("//button[text()='Add Parameter']"));
        findCaption(type, new Resolver() {
            @Override protected void resolve(String caption) {
                selectDropdownMenu(caption, dropDown);
            }
        });

//        find(xpath("//button[text()='Add Parameter']")).click();
//        find(xpath("//a[text()='%s']",displayName)).click();

        sleep(500);

        String path = last(by.xpath("//div[@name='parameter']")).getAttribute("path");

        T p = newInstance(type, this, path);
        parameters.add(p);
        return p;
    }

    public void disable() {
        check("disable");
    }

    public int getNextBuildNumber() {
        return getJson().get("nextBuildNumber").intValue();
    }

    public Workspace getWorkspace() {
        return new Workspace(this);
    }

    public void useCustomWorkspace(String ws) {
        ensureConfigPage();
        clickButton("Advanced...");

        check(find(by.path("/customWorkspace")));
        find(by.path("/customWorkspace/directory")).sendKeys(ws);
    }

    public void setLabelExpression(String l) {
        ensureConfigPage();
        check(find(by.input("hasSlaveAffinity")));
        find(by.input("_.assignedLabelString")).sendKeys(l);
    }

    public Job shouldBeTiedToLabel(final String label) {
        visit("/label/" + label); // TODO: this doesn't work correctly if the URL has non-empty context path
        assertThat(driver, hasContent(name));
        return this;
    }

    /**
     * Verify that the job contains some builds on the given slave.
     */
    public void shouldHaveBuiltOn(Jenkins j, String nodeName) {
        Node n;
        if (nodeName.equals("master"))
            n=j;
        else
            n=j.slaves.get(DumbSlave.class, nodeName);
        n.getBuildHistory().shouldInclude(this.name);
    }
}
