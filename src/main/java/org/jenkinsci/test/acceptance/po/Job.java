package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import cucumber.api.DataTable;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.Base64;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.openqa.selenium.WebElement;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import static org.jenkinsci.test.acceptance.Matchers.hasContent;

/**
 * Job Page object superclass.
 *
 * Use {@link Describable} annotation to register an implementation.
 *
 * @author Kohsuke Kawaguchi
 */
public class Job extends ContainerPageObject {
    public final String name;

    public List<Parameter> getParameters() {
        return parameters;
    }

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

    public void removeFirstBuildStep() {
        removeFirstStep("builder");
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

    private void removeFirstStep(String section) {
        ensureConfigPage();

        String sectionWithStep = String.format("/%s" , section);

        WebElement step = find(by.path(sectionWithStep));

        step.findElement(by.path(String.format("%s/repeatable-delete", sectionWithStep))).click();
    }

    public ShellBuildStep addShellStep(Resource res) {
        return addShellStep(res.asText());
    }

    public ShellBuildStep addShellStep(String shell) {
        ShellBuildStep step = addBuildStep(ShellBuildStep.class);
        step.command(shell);
        return step;
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

    /**
     * "Copy" any file from the System into the Workspace using a zipFIle.
     * @param file
     */
    public void copyFile(File file) {
        File tmp = null;
        try {
            tmp = File.createTempFile("jenkins-acceptance-tests", "dir");
            ZipUtil.pack(file, tmp);
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

    public void copyDir(Resource dir) {
        copyFile(dir.asFile());
    }

    public URL getBuildUrl() {
        return url("build?delay=0sec");
    }

    public Build startBuild(DataTable table) {
        Map<String,String> params = new HashMap<>();
        for (List<String> row : table.raw()) {
            params.put(row.get(0), row.get(1));
        }
        return startBuild(params);
    }

    public Build startBuild() {
        return scheduleBuild().waitUntilStarted();
    }

    public Build startBuild(Map<String,?> params) {
        return scheduleBuild(params).waitUntilStarted();
    }

    public Build scheduleBuild() {
        return scheduleBuild(Collections.<String,Object>emptyMap());
    }

    public Build scheduleBuild(Map<String,?> params) {
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

        return build(nb);
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

    @Override
    public String toString() {
        return name;
    }

    public ScmPolling pollScm() {
        return new ScmPolling(this);
    }

    /**
     * Getter for all area links.
     *
     * @return All found area links found
     */
    public List<String> getAreaLinks() {
        open();
        final List<String> links = new ArrayList();
        final Collection<WebElement> areas = all(by.xpath(".//div/map/area"));
        final Pattern pattern = Pattern.compile("href=\"(.*?)\"");
        for (WebElement area : areas) {
            final Matcher matcher = pattern.matcher(area.getAttribute("outerHTML"));
            if (matcher.find()) {
                links.add(matcher.group(1));
            }
        }
        return links;
    }
}
