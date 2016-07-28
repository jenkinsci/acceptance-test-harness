package org.jenkinsci.test.acceptance.po;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.codehaus.plexus.util.Base64;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.LocalController;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.junit.internal.AssumptionViolatedException;
import org.openqa.selenium.WebElement;
import org.zeroturnaround.zip.ZipUtil;

import com.google.common.reflect.ClassPath;
import com.google.inject.Injector;

import cucumber.api.DataTable;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Job Page object superclass.
 * As with other {@link TopLevelItem}s, use {@link Describable} annotation to register an implementation.
 *
 * @author Kohsuke Kawaguchi
 */
public class Job extends TopLevelItem {
    private JobParameterHelper jobParamHelper = new JobParameterHelper();
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

    public <T extends Scm> T useScm(Class<T> type) {
        ensureConfigPage();

        WebElement radio = findCaption(type, new Finder<WebElement>() {
            @Override
            protected WebElement find(String caption) {
                return outer.find(by.radioButton(caption));
            }
        });

        check(radio);

        return newInstance(type, this, radio.getAttribute("path"));
    }

    public <T extends BuildStep> T addPreBuildStep(Class<T> type) {
        return addStep(type, "prebuilder");
    }

    public <T extends BuildStep> T addBuildStep(Class<T> type) {
        return addStep(type, "builder");
    }

    public void removeFirstBuildStep() {
        removeFirstStep("builder");
    }

    /**
     * publishers added to the job are stored in a list member to provide
     * later access for modification
     */

    public <T extends PostBuildStep> T addPublisher(Class<T> type) {
        T p = addStep(type, "publisher");

        publishers.add(p);
        return p;
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

    private <T extends Step> T addStep(Class<T> type, String section) {
        ensureConfigPage();

        control(by.path("/hetero-list-add[%s]", section)).selectDropdownMenu(type);
        elasticSleep(1000); // it takes some time until the element is visible
        WebElement last = last(by.xpath("//div[@name='%s']", section));
        String path = last.getAttribute("path");

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

    /**
     * Adds a shell step that copies a resource inside the test project into a file on the build machine.
     * <p/>
     * Because there's no direct file system access to Jenkins master, we do this by packing file content in
     * base64 and put it as a heredoc in the shell script.
     */
    public void copyResource(Resource resource, String fileName) {
        addShellStep(copyResourceShell(resource, fileName));
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

    public void copyResource(Resource resource) {
        copyResource(resource, resource.getName());
    }

    /**
     * "Copy" any file from the System into the Workspace using a zipFIle.
     *
     * Differentiates when the file is being run on Windows or Unix based machines.
     * 
     * @param file
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
                addBatchStep("xcopy " + file.getAbsolutePath() + " %cd% /E");
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

    public Build startBuild(DataTable table) {
        Map<String, String> params = new HashMap<>();
        for (List<String> row : table.raw()) {
            params.put(row.get(0), row.get(1));
        }
        return startBuild(params);
    }

    public Build startBuild() {
        return scheduleBuild().waitUntilStarted();
    }

    public Build startBuild(Map<String, ?> params) {
        return scheduleBuild(params).waitUntilStarted();
    }

    public Build scheduleBuild() {
        return scheduleBuild(Collections.<String, Object>emptyMap());
    }

    public Build scheduleBuild(Map<String, ?> params) {
        open();
        int nb = getJson().get("nextBuildNumber").intValue();
        if (parameters.isEmpty()) {
            clickLink("Build Now");
        } else {
            clickLink("Build with Parameters");
            for (Parameter def : parameters) {
                Object v = params.get(def.getName());
                if (v != null) {
                    def.fillWith(v);
                }
            }
            clickButton("Build");
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

        control("/properties/hudson-model-ParametersDefinitionProperty/specified",
                "/properties/hudson-model-ParametersDefinitionProperty/parameterized" // 1.636-
        ).check();

        control(by.xpath("//button[text()='Add Parameter']")).selectDropdownMenu(type);

//        find(xpath("//button[text()='Add Parameter']")).click();
//        find(xpath("//a[text()='%s']",displayName)).click();

        elasticSleep(500);

        // 1.636-: …/parameter (or …/parameter[1] etc.); 1.637+: …/parameterDefinitions
        String path = last(by.xpath("//div[starts-with(@path,'/properties/hudson-model-ParametersDefinitionProperty/parameter')]")).getAttribute("path");

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
        assertThat(driver, hasContent(name));
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
     * Returns the relevant information of the trend graph image map. A trend graph shows for each build three
     * values: the number of warnings for priority HIGH, NORMAL, and LOW. These results are returned in a map.
     * The key is the URL to the warnings results of each build (and priority). The value is the number of warnings
     * for each result.
     *
     * @param url the URL of the graph to look at
     * @return the content of the trend graph
     */
    public Map<String, Integer> getTrendGraphContent(final String url) {
        Map<String, Integer> links = new HashMap<String, Integer>();
        Pattern resultLink = Pattern.compile("href=\"(.*" + url +".*)\"");
        Pattern warningsCount = Pattern.compile("title=\"(\\d+).*\"");
        for (WebElement area : all(by.xpath(".//div/map/area"))) {
            String outerHtml = area.getAttribute("outerHTML");
            Matcher linkMatcher = resultLink.matcher(outerHtml);
            if (linkMatcher.find()) {
                Matcher countMatcher = warningsCount.matcher(outerHtml);
                if (countMatcher.find()) {
                    links.put(linkMatcher.group(1), Integer.valueOf(countMatcher.group(1)));
                }
            }
        }

        return links;
    }

    /**
     * Deletes the current job
     */
    public void delete() {
        this.open();
        clickLink("Delete Project");
        confirmAlert(2);
    }

    /**
     * Loads all existing build parameters of the specified type into this Job object. Useful is this Job has been copied/moved or promoted
     * @param paramClasses The {@link org.jenkinsci.test.acceptance.po.Parameter} subtypes to load
     */
    public void loadExistingBuildParameters(Class<? extends Parameter>... paramClasses) {
        ensureConfigPage();
        jobParamHelper.loadJobParams(paramClasses);
    }

    /**
     * Loads all existing build parameters of any type inside the scanningPaths into this Job object. Useful is this Job
     * has been copied/moved or promoted
     * @param scanningPaths The packages to scan for {@link org.jenkinsci.test.acceptance.po.Parameter} subtypes to load
     */
    public void loadExistingBuildParameters(String... scanningPaths) {
        ensureConfigPage();
        jobParamHelper.loadJobParams(scanningPaths);
    }


    /**
     * Helper class to fill the Job`s PO build parameters from the existing ones in the config UI, usefull when a Job
     * has been copied, moved or promoted.
     *
     * Since the number and type of params is variable this class needs to known in advance the types of the parameters
     * to load (for example {@link org.jenkinsci.test.acceptance.po.StringParameter}) or perform a classpath scan for
     * {@link org.jenkinsci.test.acceptance.po.Parameter} subtypes before loading them.
     */
    protected class JobParameterHelper {

        /**
         * Scans the given packages for {@link org.jenkinsci.test.acceptance.po.Parameter} subtypes. It only scans for
         * top level classes, that is no nested classes are included in the scan results.
         * This implementation does not perform any type of catching of the results, is responsability of the caller
         * to do that if neccesary.
         *
         * @param paths The package names to scan for {@link org.jenkinsci.test.acceptance.po.Parameter} subtypes
         * @return A list of all the {@link org.jenkinsci.test.acceptance.po.Parameter} subtypes found in the given
         * packages
         */
        public List<Class<? extends Parameter>> scan(String... paths) {
            List<Class<? extends Parameter>> parametersClasses = new LinkedList<>();
            try {
                for (String path : paths) {
                    List<ClassPath.ClassInfo> paramClasses = ClassPath.from(Parameter.class.getClassLoader()).getTopLevelClasses(path).asList();
                    for (ClassPath.ClassInfo info : paramClasses) {
                        Class<?> clazz = info.load();
                        if (!clazz.equals(Parameter.class) && Parameter.class.isAssignableFrom(clazz)) {
                            parametersClasses.add((Class<? extends Parameter>)clazz);
                        }
                    }
                }
                return parametersClasses;
            } catch (IOException e) {
                throw new Error(e);
            }

        }

        public void loadJobParams(String... pathsToScan) {
            List<Class<? extends Parameter>> paramClasses = scan(pathsToScan);
            fillJobParams(paramClasses);
        }

        public void loadJobParams(Class<? extends Parameter>... paramClasses) {
            fillJobParams(Arrays.asList(paramClasses));
        }

        protected void fillJobParams(List<Class<? extends Parameter>> paramClasses) {
            List<WebElement> paramElements = driver.findElements(by.xpath("//div[starts-with(@path,'/properties/hudson-model-ParametersDefinitionProperty/parameter')]"));
            for (WebElement paramElement : paramElements) {
                String elementText = paramElement.getText();
                for (Class<? extends Parameter> paramClass : paramClasses) {
                    String[] captions = paramClass.getAnnotation(Describable.class).value();
                    for (String value : captions) {
                        if (elementText.contains(value)) {
                            Parameter param = newInstance(paramClass, Job.this, paramElement.getAttribute("path"));
                            param.setNameProperty(param.control("name").resolve().getAttribute("value"));
                            parameters.add(param);
                        }
                    }
                }

            }
        }
    }
}
