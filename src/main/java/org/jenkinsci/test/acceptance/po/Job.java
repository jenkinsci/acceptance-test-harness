package org.jenkinsci.test.acceptance.po;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.codehaus.plexus.util.Base64;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.LocalController;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.junit.internal.AssumptionViolatedException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zeroturnaround.zip.ZipUtil;

import com.google.inject.Injector;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;
import static org.junit.Assert.*;

/**
 * Job Page object superclass.
 * As with other {@link TopLevelItem}s, use {@link Describable} annotation to register an implementation.
 *
 * @author Kohsuke Kawaguchi
 */
public class Job extends TopLevelItem {
    public List<Parameter> getParameters() {
        return parameters;
    }

    /**
     * The controller that starts/stops Jenkins
     */
    @Inject
    public JenkinsController controller;
    
    private List<Parameter> parameters = new ArrayList<>();

    // TODO these controls (and some methods) actually belong in a subclass corresponding to AbstractProject
    protected List<PostBuildStep> publishers = new ArrayList<>();
    public final Control concurrentBuild = control("/concurrentBuild");
    private final Control hasSlaveAffinity = control("/hasSlaveAffinity");
    private final Control assignedLabel = control("/hasSlaveAffinity/assignedLabelString", "/label");

    public Job(Injector injector, URL url, String name) {
        super(injector, url, name);
    }

    public Job(PageObject context, URL url, String name) {
        super(context, url, name);
    }
    
    public <T extends Scm> T useScm(Class<T> type) {
        ensureConfigPage();

        WebElement radio = findCaption(type, new Finder<WebElement>() {
            @Override
            protected WebElement find(String caption) {
                return outer.find(by.radioButton(caption));
            }
        });

        check(radio);

        return newInstance(type, this, requireNonNull(getPath(radio)));
    }

    public <T extends BuildStep> T addPreBuildStep(Class<T> type) {
        return addStep(type, "prebuilder");
    }

    public <T extends BuildStep> T addBuildStep(Class<T> type) {
        return addStep(type, "builder");
    }

    // TODO move this functionality to page area itself
    public void removeFirstBuildStep() {
        removeFirstStep("builder");
        elasticSleep(500); // chrome needs some time
    }

    /**
     * Adds the specified publisher to this job. Publishers are stored in a list member to provide
     * later access for modification.
     *
     * @param publisherClass the publisher to configure
     * @param <T>            the type of the publisher
     * @see #getPublisher(Class)
     * @see #editPublisher(Class, Consumer)
     */
    public <T extends PostBuildStep> T addPublisher(Class<T> publisherClass) {
        T p = addStep(publisherClass, "publisher");

        publishers.add(p);

        return p;
    }

    /**
     * Adds the specified publisher to this job. Publishers are stored in a list member to provide
     * later access for modification. After the publisher has been added the publisher is configured
     * with the specified configuration lambda. Afterwards, the job configuration page still is visible and
     * not saved.
     *
     * @param type          the publisher to configure
     * @param configuration the additional configuration options for this job
     * @param <T>           the type of the publisher
     * @see #getPublisher(Class)
     * @see #editPublisher(Class, Consumer)
     */
    public <T extends PostBuildStep> T addPublisher(final Class<T> type, final Consumer<T> configuration) {
        T p = addPublisher(type);

        configuration.accept(p);

        return p;
    }

    /**
     * Edits this job using the specified configuration lambda. Opens the job configuration view, selects the specified
     * publisher page object, runs the specified configuration lambda, and saves the changes. Afterwards, the job
     * configuration page still is visible and not saved.
     *
     * @param type          the publisher to configure
     * @param configuration the additional configuration options for this job
     * @param <T>           the type of the publisher
     */
    public <T extends PostBuildStep> void editPublisher(final Class<T> type, final Consumer<T> configuration) {
        configure();
        T publisher = getPublisher(type);
        configuration.accept(publisher);
        save();
    }

    /**
     * Getter for a specific publisher previously added to the job.
     * If a publisher of a class is requested which has not been added previously
     * this will result in a {@link java.util.NoSuchElementException}.
     */
    @SuppressWarnings("unchecked") // The check is performed in the method
    public <T extends PostBuildStep> T getPublisher(Class<T> type) {
        for (PostBuildStep p : publishers) {
            if (type.isAssignableFrom(p.getClass()))
                return (T) p;
        }

        throw new NoSuchElementException();

    }

    private <T extends Step> T addStep(final Class<T> type, final String section) {
        ensureConfigPage();

        String path = createPageArea('/' + section, new Runnable() {
            @Override public void run() {
                control(by.path("/hetero-list-add[%s]", section)).selectDropdownMenu(type);
            }
        });
        return newInstance(type, this, path);
    }

    private void removeFirstStep(String section) {
        ensureConfigPage();

        String sectionWithStep = String.format("/%s", section);

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

    public BatchCommandBuildStep addBatchStep(String batch) {
        BatchCommandBuildStep step = addBuildStep(BatchCommandBuildStep.class);
        step.command(batch);
        return step;
    }

    public <T extends BuildWrapper> T addBuildWrapper(Class<T> type) {
        ensureConfigPage();
        T wrapper = newInstance(type, this);
        wrapper.enable.check();
        return wrapper;
    }

    public <T extends Trigger> T addTrigger(Class<T> type) {
        ensureConfigPage();
        T trigger = newInstance(type, this);
        WebElement checkbox = trigger.enabled.resolve();
        WebElement label = checkbox.findElement(by.xpath("../label"));
        check(label);
        return trigger;
    }

    /**
     * Adds a shell step that copies a resource inside the test project into a file on the build machine.
     * <p>
     * Because there's no direct file system access to Jenkins master, we do this by packing file content in
     * base64 and put it as a heredoc in the shell script.
     */
    public void copyResource(Resource resource, String fileName) {
        if (SystemUtils.IS_OS_WINDOWS) {
            addBatchStep(copyResourceBatch(resource));
        }
        else {
            addShellStep(copyResourceShell(resource, fileName));
        }
    }

    protected String copyResourceShell(Resource resource, String fileName) {
        try (InputStream in = resource.asInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try (OutputStream gz = new GZIPOutputStream(out)) {
                IOUtils.copy(in, gz);
            }

            // fileName can include path portion like foo/bar/zot
            return String.format("(mkdir -p %1$s || true) && rm -r %1$s && base64 --decode << ENDOFFILE | gunzip > %1$s \n%2$s\nENDOFFILE",
                    fileName, new String(Base64.encodeBase64Chunked(out.toByteArray())));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    protected String copyResourceBatch(Resource resource) {
        String path = resource.url.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        path = path.replace("/", "\\");
        return xCopy(path, ".");
    }

    public void copyResource(Resource resource) {
        copyResource(resource, resource.getName());
    }

    /**
     * "Copy" any file from the System into the Workspace using a zipFIle.
     *
     * Differentiates when the file is being run on Windows or Unix based machines.
     */
    public void copyFile(File file) {
        File tmp = null;
        try {
            tmp = File.createTempFile("jenkins-acceptance-tests", "dir");
            ZipUtil.pack(file, tmp);
            byte[] archive = IOUtils.toByteArray(new FileInputStream(tmp));

            if (SystemUtils.IS_OS_WINDOWS) {
                if (!(controller instanceof LocalController)) {
                    // TODO: Make it work for RemoteJenkinsController like in Unix (below)
                    throw new AssumptionViolatedException("Copying files in Windows is only supported if a LocalController is in use. Test will be skipped.");
                }
                addBatchStep(xCopy(file.getAbsolutePath(), "%cd%"));
            } else {
                addShellStep(String.format(
                        "base64 --decode << ENDOFFILE > archive.zip && unzip -o archive.zip \n%s\nENDOFFILE",
                        new String(Base64.encodeBase64Chunked(archive))
                ));
            }
        } catch (IOException e) {
            throw new AssertionError(e);
        } finally {
            if (tmp != null) {
                tmp.delete();
            }
        }
    }

    public void copyDir(Resource dir) {
        copyFile(dir.asFile());
    }

    /**
     * "Copies" a resource (can be a single file or a directory) to the jobs workspace by utilizing a shell step.
     *
     * @param resourcePath the resource to copy
     */
    public void copyResource(String resourcePath) {
        ensureConfigPage();
        final Resource res = resource(resourcePath);
        //decide whether to utilize copyResource or copyDir
        if (res.asFile().isDirectory()) {
            copyDir(res);
        } else {
            copyResource(res);
        }
    }

    public URL getBuildUrl() {
        return url("build?delay=0sec");
    }

    public Build startBuild() {
        return scheduleBuild().waitUntilStarted();
    }

    public Build startBuild(Map<String, ?> params) {
        return scheduleBuild(params).waitUntilStarted();
    }

    public Build scheduleBuild() {
        return scheduleBuild(Collections.emptyMap());
    }

    public Build scheduleBuild(Map<String, ?> params) {
        open();
        int nb = getJson().get("nextBuildNumber").intValue();
        if (parameters.isEmpty()) {
            clickLink("Build Now");
        } else {
            clickLink("Build with Parameters");
            try {
                BuildWithParameters paramsPage = new BuildWithParameters(this, new URL(driver.getCurrentUrl()));
                paramsPage.enter(parameters, params).start();
            } catch (MalformedURLException e) {
                throw new Error(e);
            }
        }

        return build(nb);
    }

    public Build build(int buildNumber) {
        return new Build(this, buildNumber);
    }

    public Build getLastBuild() {
        return new Build(this, "lastBuild");
    }

    public <T extends Parameter> T addParameter(Class<T> type) {
        ensureConfigPage();

        control(by.checkbox("This project is parameterized")).check();

        control(by.xpath("//button[text()='Add Parameter']")).selectDropdownMenu(type);

        // TODO selectDropdownMenu should not need this sleep - try and remove it
        elasticSleep(500);

        // /properties/hudson-model-ParametersDefinitionProperty/parameterDefinitions  for the first
        // /properties/hudson-model-ParametersDefinitionProperty/parameterDefinitions[n] for subsequent ones
        // if we have another descriptor inside the descriptor inside the parameterDefinition we select the that instead of the actual parameter. (e.g NodeParameter)
        // as all browsers do not support matches in xpath 2.0 we need to do this ourselves (find the last match and then extract the correct part even if we match the wrong thing originally
        String path = last(by.xpath("//div[starts-with(@path,'/properties/hudson-model-ParametersDefinitionProperty/parameterDefinitions')]")).getAttribute("path");

        Pattern pattern = Pattern.compile("^(/properties/hudson-model-ParametersDefinitionProperty/parameterDefinitions([^/]*))(/.*)?$");
        Matcher m = pattern.matcher(path);
        // for some as yet unknown reason the matcher sometimes failed to match throwing an illegalStateException with no information to help diagnose
        // after I added this I never reproduced the issue - but still having what failed to match will at least help in the future
        if (!m.matches()) {
            throw new IllegalStateException("No match for path in regexp : " + path);
        }
        path = m.group(1);

        T p = newInstance(type, this, path);
        parameters.add(p);
        return p;
    }

    public void disable() {
        try {
            // Newer versions of Jenkins use a toggle switch with the active state as its label
            check(find(by.id("toggle-switch-enable-disable-project")));
        } catch (org.openqa.selenium.NoSuchElementException exception) {
            check("Disable this project");
        }
    }

    public int getNextBuildNumber() {
        return getJson().get("nextBuildNumber").intValue();
    }

    public Workspace getWorkspace() {
        return new Workspace(this);
    }

    public void useCustomWorkspace(String ws) {
        ensureConfigPage();

        // There may be multiple "Advanced..." buttons visible on the job config page, and there's no easy way to identify
        // which one is the Advanced Project Options one, so let's just hit all of them.
        for (WebElement advancedButton : all(by.button("Advanced..."))) {
            if (advancedButton.isDisplayed()) {
                advancedButton.click();
            }
        }

        // Note that ordering is important here as the old name for the checkbox == new name for the text field.
        control("/customWorkspace", "/hasCustomWorkspace").check();
        control("/customWorkspace/directory", "/customWorkspace").set(ws);
    }

    public void setLabelExpression(String label) {
        ensureConfigPage();
        hasSlaveAffinity.check();
        assignedLabel.set(label);
    }

    public Job shouldBeTiedToLabel(final String label) {
        visit("/label/" + label); // TODO: this doesn't work correctly if the URL has non-empty context path
        assertTrue(control(by.css("a[href$='job/" + name + "/']")).exists());
        return this;
    }

    /**
     * Verify that the job contains some builds on exact one of the given list of nodes.
     * To test whether the the job has built on the master, the jenkins instance has to be
     * passed in the parameter.
     */
    public void shouldHaveBuiltOnOneOfNNodes(List<Node> nodes) {
        int noOfNodes = 0;

        for (Node n : nodes) {
            if (!n.getBuildHistory().getBuildsOf(this).isEmpty()) {
                noOfNodes++;
            }
        }
        assertThat(noOfNodes, is(1));
    }

    public ScmPolling pollScm() {
        return new ScmPolling(this);
    }

    /**
     * Deletes the current job.
     */
    public void delete() {
        open();
        runThenConfirmAlert(() -> clickLink("Delete Project"),2);
    }

    public static org.hamcrest.Matcher<WebDriver> disabled() {
        return allOf(
                not(hasContent("Build Now")),
                not(hasContent("Disable Project")),
                hasContent("Enable"),
                hasContent("This project is currently disabled")
        );
    }

    private String xCopy(final String source, final String destination) {
        return "xcopy " + source + " " + destination + " /E /Y";
    }
}
