/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.gradle.GradleInstallation;
import org.jenkinsci.test.acceptance.plugins.gradle.GradleStep;
import org.jenkinsci.test.acceptance.plugins.gradle.GradleWrapper;
import org.jenkinsci.test.acceptance.po.*;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.jenkinsci.test.acceptance.Matchers.containsRegexp;
import static org.jenkinsci.test.acceptance.Matchers.containsString;
import static org.junit.Assert.assertThat;

@WithPlugins("gradle")
public class GradlePluginTest extends AbstractJUnitTest {

    private static final String SUCCESSFUL_BUILD = "Finished: SUCCESS";

    @Test
    public void run_gradle_script() {
        GradleInstallation.installLatestGradleVersion(jenkins);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT_VERSION_NAME);
        job.save();

        final Build build = job.startBuild();
        build.shouldSucceed();
        assertThat(build.getConsole(), containsString(SUCCESSFUL_BUILD));
    }

    @Test
    public void run_gradle_task() {
        GradleInstallation.installLatestGradleVersion(jenkins);
        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT_VERSION_NAME);
        step.setTasks("hello");
        job.save();

        final Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString(SUCCESSFUL_BUILD));
    }

    @Test
    public void run_gradle_with_switches(){
        GradleInstallation.installLatestGradleVersion(jenkins);
        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT_VERSION_NAME);
        step.setSwitches("--quiet");
        job.save();

        final Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsRegexp("gradle.* --quiet"));
        assertThat(build.getConsole(), containsString(SUCCESSFUL_BUILD));
    }

    @Test
    public void run_gradle_script_in_dir() {
        final String gradleInstallationName = "gradle-1.5";
        GradleInstallation.installGradle(jenkins, gradleInstallationName, "1.5");

        FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/scriptNoPlugins.gradle"), "gradle/hello.gradle");
        GradleStep step = job.addBuildStep(GradleStep.class);
        step.setFile("hello.gradle");
        step.setVersion(gradleInstallationName);
        step.setTasks("hello");
        step.setDir("gradle");
        job.save();

        final Build build = job.startBuild();
        build.shouldSucceed();
        assertThat(build.getConsole(), containsString("Hello world!"));
    }

    @Test
    public void run_gradle_script_multiple_tasks() {
        GradleInstallation.installLatestGradleVersion(jenkins);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT_VERSION_NAME);
        step.setTasks("firstTask secondTask");
        job.save();

        final Build build = job.startBuild();
        build.shouldSucceed();
        assertThat(build.getConsole(), containsString("First!"));
        assertThat(build.getConsole(), containsString("Second!"));
    }

    @Test
    @Issue({"JENKINS-40778"})
    public void run_gradle_script_messages_initialized() {
        GradleInstallation.installLatestGradleVersion(jenkins);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT_VERSION_NAME);
        step.setTasks("hello");
        job.save();

        final Build build = job.startBuild();
        build.shouldSucceed();
        assertThat(build.getConsole(), containsString("Invoke Gradle script"));
    }

    @Test @WithPlugins("gradle@1.27-SNAPSHOT")
    public void run_gradle_script_build_scan_link() {
        GradleInstallation.installLatestGradleVersion(jenkins);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT_VERSION_NAME);
        step.setTasks("hello");
        step.setSwitches("--scan");
        job.save();

        final Build build = job.startBuild();
        build.shouldSucceed();
        build.openStatusPage();
        final WebElement buildScanLink = build.find(By.partialLinkText("Gradle Build Scan"));
        assertThat(buildScanLink.getAttribute("href"), containsString("https://gradle.com/"));
    }

    @Test
    public void run_gradle_script_with_wrapper(){

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(job.resource("/gradle_plugin/script.gradle"), "build.gradle");

        GradleWrapper.downloadWrapperFiles(job);
        GradleWrapper.addWrapperStep(job, null, "hello");
        job.save();

        final Build build = job.startBuild();
        build.shouldSucceed();
        assertThat(build.getConsole(), containsString(SUCCESSFUL_BUILD));
        assertThat(build.getConsole(), containsString("Hello world!"));
    }

    @Test
    public void run_gradle_environment_variables(){
        GradleInstallation.installLatestGradleVersion(jenkins);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT_VERSION_NAME);
        step.setTasks("environmentVariables");
        job.save();

        final Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("Build Number: " + build.getNumber()));
        assertThat(build.getConsole(), containsString("Build Name: " + build.getName()));
    }

    @Test @WithPlugins("gradle@1.27-SNAPSHOT")
    public void run_gradle_job_parameters_as_project_properties(){
        GradleInstallation.installLatestGradleVersion(jenkins);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        job.addParameter(StringParameter.class).setName("TEST_PARAM_1").setDefault("hello");
        job.addParameter(StringParameter.class).setName("TEST_PARAM_2").setDefault("world");

        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT_VERSION_NAME);
        step.setTasks("jobParametersAsProjectProperties");
        step.setPassAllAsProjectProperties();
        job.save();

        final Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("Gradle Properties: hello world"));
    }

    @Test @WithPlugins("gradle@1.27-SNAPSHOT")
    public void run_gradle_job_parameters_as_system_properties(){
        GradleInstallation.installLatestGradleVersion(jenkins);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        job.addParameter(StringParameter.class).setName("TEST_PARAM_1").setDefault("hello");
        job.addParameter(StringParameter.class).setName("TEST_PARAM_2").setDefault("world");

        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT_VERSION_NAME);
        step.setTasks("jobParametersAsSystemProperties");
        step.setPassAllAsSystemProperties();
        job.save();

        final Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("Gradle Properties: hello world"));
    }

    @Test
    public void run_gradle_wrapper_location_param(){
        GradleInstallation.installLatestGradleVersion(jenkins);
        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");

        final String location = "test";
        GradleWrapper.downloadWrapperFiles(job);
        GradleWrapper.moveWrapperFiles(job, location);
        GradleWrapper.addWrapperStep(job, location, "hello");

        job.save();
        final Build build = job.startBuild().shouldSucceed();

        assertThat(build.getConsole(), containsString(SUCCESSFUL_BUILD));
        assertThat(build.getConsole(), containsString("Hello world!"));
    }

    @Test
    public void run_gradle_user_home_workspace(){
        GradleInstallation.installGradle(jenkins, GradleInstallation.DEFAULT_VERSION_NAME, GradleInstallation.LATEST_VERSION);
        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");

        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setTasks("hello");
        step.setVersion(GradleInstallation.DEFAULT_VERSION_NAME);
        step.setForceGradleHomeToUseWorkspace();
        job.save();

        job.startBuild().shouldSucceed();
        assertThat(job, Workspace.workspaceContains("caches"));
    }

    @Test @WithPlugins("gradle@1.27-SNAPSHOT")
    public void run_gradle_add_project_properties(){
        GradleInstallation.installLatestGradleVersion(jenkins);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");

        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT_VERSION_NAME);
        step.setProjectProperties("TEST_PARAM_1=hello\nTEST_PARAM_2=world");
        step.setTasks("jobParametersAsProjectProperties");
        job.save();

        final Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("Gradle Properties: hello world"));
    }

    @Test @WithPlugins("gradle@1.27-SNAPSHOT")
    public void run_gradle_add_system_properties(){
        GradleInstallation.installLatestGradleVersion(jenkins);
        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");

        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT_VERSION_NAME);
        step.setSystemProperties("TEST_PARAM_1=hello\nTEST_PARAM_2=world");
        step.setTasks("jobParametersAsSystemProperties");
        job.save();

        final Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("Gradle Properties: hello world"));
    }

    private static final String JENKINS_FILE = "/gradle_plugin/pipeline_test.txt";

    @Test
    public void pipeline_test () {
        final Build build = setUpAndRunPipelineBuild(JENKINS_FILE);
        assertThat(build.getConsole(), containsString("Hello world!"));
    }

    @Test
    public void pipeline_build_scan_link_test () {
        final Build build = setUpAndRunPipelineBuild(JENKINS_FILE);
        build.openStatusPage();
        final WebElement buildScanLink = build.find(By.partialLinkText("Gradle Build Scan"));
        assertThat(buildScanLink.getAttribute("href"), containsString("https://gradle.com/"));
    }

    private Build setUpAndRunPipelineBuild(final String jenkinsFile) {
        GradleInstallation.installLatestGradleVersion(jenkins);
        final WorkflowJob workflowJob = jenkins.jobs.create(WorkflowJob.class);

        String file = workflowJob.copyResourceStep("/gradle_plugin/script.gradle");
        String test = "" +
            "pipeline {\n" +
                "agent any\n" +
                "stages {\n" +
                    "stage ('prepare_build') {\n" +
                        "steps {\n" +
                            file +
                        "}\n" +
                    "}\n"
                    + resource(jenkinsFile).asText() +
                "}\n" +
            "}";
        test = test.replaceAll("script.gradle", "build.gradle");

        workflowJob.script.set(test);

        workflowJob.sandbox.check();
        workflowJob.save();
        return workflowJob.startBuild().shouldSucceed();
    }

    @Test @WithPlugins("gradle@1.27-SNAPSHOT")
    public void gradle_tasks_link(){
        GradleInstallation.installLatestGradleVersion(jenkins);
        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");

        final GradleStep step = job.addBuildStep(GradleStep.class);

        step.setTasks("firstTask secondTask");
        step.setVersion(GradleInstallation.DEFAULT_VERSION_NAME);
        job.save();

        final Build build = job.startBuild().shouldSucceed();

        final WebElement firstTaskLink = build.find(By.partialLinkText("firstTask"));
        final WebElement secondTaskLink = build.find(By.partialLinkText("secondTask"));

        assertThat(firstTaskLink.getAttribute("href"), containsString("#gradle-task-0"));
        assertThat(secondTaskLink.getAttribute("href"), containsString("#gradle-task-1"));
        assertThat(build.getConsole(), containsString("First!"));
        assertThat(build.getConsole(), containsString("Second!"));
    }
}
