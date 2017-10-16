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
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.jenkinsci.test.acceptance.Matchers.containsRegexp;
import static org.jenkinsci.test.acceptance.Matchers.containsString;
import static org.jenkinsci.test.acceptance.plugins.gradle.GradleTask.HELLO;
import static org.jenkinsci.test.acceptance.plugins.gradle.GradleTask.FIRST;
import static org.jenkinsci.test.acceptance.plugins.gradle.GradleTask.SECOND;
import static org.jenkinsci.test.acceptance.plugins.gradle.GradleTask.ENVIRONMENT_VARIABLES;
import static org.jenkinsci.test.acceptance.plugins.gradle.GradleTask.JOB_PARAM_AS_PROJECT_PROPERTIES;
import static org.jenkinsci.test.acceptance.plugins.gradle.GradleTask.JOB_PARAM_AS_SYSTEM_PROPERTIES;
import static org.junit.Assert.assertThat;

@WithPlugins("gradle")
public class GradlePluginTest extends AbstractJUnitTest {

    private static final String GRADLE_SCRIPT = "/gradle_plugin/script.gradle";
    private static final String GRADLE_SCRIPT_NO_PLUGIN = "/gradle_plugin/scriptNoPlugins.gradle";
    private static final String SUCCESSFUL_BUILD = "Finished: SUCCESS";

    /**
     * Verify the execution of a basic gradle build script.
     */
    @Test
    public void run_gradle_script() {
        GradleInstallation.installGradle(jenkins);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource(GRADLE_SCRIPT), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT);
        job.save();

        final Build build = job.startBuild();
        build.shouldSucceed();
        assertThat(build.getConsole(), containsString(SUCCESSFUL_BUILD));
    }

    /**
     * Verify the execution of gradle build script with switch parameter.
     */
    @Test
    public void run_gradle_with_switches(){
        GradleInstallation.installGradle(jenkins);
        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource(GRADLE_SCRIPT), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT);
        step.setSwitches("--quiet");
        step.setTasks(FIRST.getName()+ " " + SECOND.getName());
        job.save();

        final Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsRegexp("gradle.* --quiet"));
        assertThat(build.getConsole(), containsString(SUCCESSFUL_BUILD));
    }

    /**
     * Verify the execution of gradle build script localized divergent to default folder.
     */
    @Test
    public void run_gradle_script_in_dir() {
        final String gradleInstallationName = "gradle-1.5";
        GradleInstallation.installGradle(jenkins, gradleInstallationName, "1.5");

        FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource(GRADLE_SCRIPT_NO_PLUGIN), "gradle/hello.gradle");
        GradleStep step = job.addBuildStep(GradleStep.class);
        step.setFile("hello.gradle");
        step.setVersion(gradleInstallationName);
        step.setTasks(HELLO.getName());
        step.setDir("gradle");
        job.save();

        final Build build = job.startBuild();
        build.shouldSucceed();
        assertThat(build.getConsole(), containsString(HELLO.getPrintln()));
    }

    /**
     * Verify the execution of gradle build and check if build scan links are available
     */
    @Test
    public void run_gradle_script_build_scan_link() {
        GradleInstallation.installGradle(jenkins);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource(GRADLE_SCRIPT), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT);
        step.setTasks(HELLO.getName());
        step.setSwitches("--scan");
        job.save();

        final Build build = job.startBuild();
        build.shouldSucceed();
        build.openStatusPage();
        final WebElement buildScanLink = build.find(By.partialLinkText("Gradle Build Scan"));
        assertThat(buildScanLink.getAttribute("href"), containsString("https://gradle.com/"));
    }

    /**
     * Verify the execution of gradle build script as gradle wrapper.
     */
    @Test
    public void run_gradle_script_with_wrapper(){
        GradleInstallation.installGradle(jenkins);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(job.resource(GRADLE_SCRIPT), "build.gradle");

        GradleWrapper.downloadWrapperFiles(job);
        GradleWrapper.addWrapperStep(job, null, HELLO.getName());
        job.save();

        final Build build = job.startBuild();
        build.shouldSucceed();
        assertThat(build.getConsole(), containsString(SUCCESSFUL_BUILD));
        assertThat(build.getConsole(), containsString(HELLO.getPrintln()));
    }

    /**
     * Verify the execution of gradle build script as gradle wrapper localized divergent to default folder.
     */
    @Test
    public void run_gradle_wrapper_location_param(){
        GradleInstallation.installGradle(jenkins);
        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource(GRADLE_SCRIPT), "build.gradle");

        final String location = "test";
        GradleWrapper.downloadWrapperFiles(job);
        GradleWrapper.moveWrapperFiles(job, location);
        GradleWrapper.addWrapperStep(job, location, HELLO.getName());

        job.save();
        final Build build = job.startBuild().shouldSucceed();

        assertThat(build.getConsole(), containsString(SUCCESSFUL_BUILD));
        assertThat(build.getConsole(), containsString(HELLO.getPrintln()));
    }

    /**
     * Verify the execution of gradle build script with Jenkins environment variables as gradle parameters.
     */
    @Test
    public void run_gradle_environment_variables(){
        GradleInstallation.installGradle(jenkins);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource(GRADLE_SCRIPT), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT);
        step.setTasks(ENVIRONMENT_VARIABLES.getName());
        job.save();

        final Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("Build Number: " + build.getNumber()));
        assertThat(build.getConsole(), containsString("Build Name: " + build.getName()));
    }

    /**
     * Verify the execution of gradle build script with committed parameters as gradle project properties.
     */
    @Test
    public void run_gradle_job_parameters_as_project_properties(){
        GradleInstallation.installGradle(jenkins);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource(GRADLE_SCRIPT), "build.gradle");
        job.addParameter(StringParameter.class).setName("TEST_PARAM_1").setDefault("hello");
        job.addParameter(StringParameter.class).setName("TEST_PARAM_2").setDefault("world");

        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT);
        step.setTasks(JOB_PARAM_AS_PROJECT_PROPERTIES.getName());
        step.setPassAllAsProjectProperties();
        job.save();

        final Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("Gradle Properties: hello world"));
    }

    /**
     * Verify the execution of gradle build script with committed parameters as gradle system properties.
     */
    @Test
    public void run_gradle_job_parameters_as_system_properties(){
        GradleInstallation.installGradle(jenkins);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource(GRADLE_SCRIPT), "build.gradle");
        job.addParameter(StringParameter.class).setName("TEST_PARAM_1").setDefault("hello");
        job.addParameter(StringParameter.class).setName("TEST_PARAM_2").setDefault("world");

        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT);
        step.setTasks(JOB_PARAM_AS_SYSTEM_PROPERTIES.getName());
        step.setPassAllAsSystemProperties();
        job.save();

        final Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("Gradle Properties: hello world"));
    }

    /**
     * Verify the execution of gradle build script with gradle installation files in the job workspace.
     */
    @Test
    public void run_gradle_user_home_workspace(){
        GradleInstallation.installGradle(jenkins);
        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource(GRADLE_SCRIPT), "build.gradle");

        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setTasks(HELLO.getName());
        step.setVersion(GradleInstallation.DEFAULT);
        step.setForceGradleHomeToUseWorkspace();
        job.save();

        job.startBuild().shouldSucceed();
        assertThat(job, Workspace.workspaceContains("caches"));
    }

    /**
     * Verify the execution of gradle build script with committed gradle project properties.
     */
    @Test
    public void run_gradle_add_project_properties(){
        GradleInstallation.installGradle(jenkins);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource(GRADLE_SCRIPT), "build.gradle");

        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT);
        step.setProjectProperties("TEST_PARAM_1=hello\nTEST_PARAM_2=world");
        step.setTasks(JOB_PARAM_AS_PROJECT_PROPERTIES.getName());
        job.save();

        final Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("Gradle Properties: hello world"));
    }

    /**
     * Verify the execution of gradle build script with committed gradle system properties.
     */
    @Test
    public void run_gradle_add_system_properties(){
        GradleInstallation.installGradle(jenkins);
        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource(GRADLE_SCRIPT), "build.gradle");

        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT);
        step.setSystemProperties("TEST_PARAM_1=hello\nTEST_PARAM_2=world");
        step.setTasks(JOB_PARAM_AS_SYSTEM_PROPERTIES.getName());
        job.save();

        final Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("Gradle Properties: hello world"));
    }

    /**
     * Verify the existence of links for executed tasks.
     */
    @Test
    public void run_gradle_tasks_link(){
        GradleInstallation.installGradle(jenkins);
        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource(GRADLE_SCRIPT), "build.gradle");

        final GradleStep step = job.addBuildStep(GradleStep.class);

        step.setTasks(FIRST.getName() + " " + SECOND.getName());
        step.setVersion(GradleInstallation.DEFAULT);
        job.save();

        final Build build = job.startBuild().shouldSucceed();

        final WebElement firstTaskLink = build.find(By.partialLinkText(FIRST.getName()));
        final WebElement secondTaskLink = build.find(By.partialLinkText(SECOND.getName()));

        assertThat(firstTaskLink.getAttribute("href"), containsString("#gradle-task-0"));
        assertThat(secondTaskLink.getAttribute("href"), containsString("#gradle-task-1"));
        assertThat(build.getConsole(), containsString(FIRST.getPrintln()));
        assertThat(build.getConsole(), containsString(SECOND.getPrintln()));
    }

    //Gradle Plugin Pipeline Tests

    /**
     * Jenkins File to run a gradle task with the build scan option
     */
    private static final String JENKINS_FILE_WITH_BUILD_SCAN = "/gradle_plugin/pipeline_test_build_scan.txt";

    /**
     * Jenkins File to run multiple gradle tasks
     */
    private static final String JENKINS_FILE_MULTIPLE_TASKS = "/gradle_plugin/pipeline_test_multiple_tasks.txt";

    /**
     * Runs a pipeline gradle build and verifies the build was executed successfully
     */
    @Test @WithPlugins("workflow-aggregator")
    public void run_gradle_pipeline_basic() {
        final Build build = setUpAndRunPipelineBuild(JENKINS_FILE_WITH_BUILD_SCAN, GRADLE_SCRIPT);
        assertThat(build.getConsole(), containsString(HELLO.getPrintln()));
    }

    /**
     * Runs a pipeline gradle build and verifies that the build scan links are existent
     */
    @Test @WithPlugins("workflow-aggregator") @Ignore("JENKINS-45205")
    public void run_gradle_pipeline_build_scan_link() {
        final Build build = setUpAndRunPipelineBuild(JENKINS_FILE_WITH_BUILD_SCAN, GRADLE_SCRIPT);
        build.openStatusPage();
        final WebElement buildScanLink = build.find(By.partialLinkText("Gradle Build Scan"));
        assertThat(buildScanLink.getAttribute("href"), containsString("https://gradle.com/"));
    }

    /**
     * Runs a pipeline gradle build and verifies that the task links are existent
     */
    @Test @WithPlugins("workflow-aggregator") @Ignore("JENKINS-45206")
    public void run_gradle_pipeline_build_task_links() {
        final Build build = setUpAndRunPipelineBuild(JENKINS_FILE_MULTIPLE_TASKS, GRADLE_SCRIPT);

        final WebElement firstTaskLink = build.find(By.partialLinkText(FIRST.getName()));
        final WebElement secondTaskLink = build.find(By.partialLinkText(SECOND.getName()));

        assertThat(firstTaskLink.getAttribute("href"), containsString("#gradle-task-0"));
        assertThat(secondTaskLink.getAttribute("href"), containsString("#gradle-task-1"));
        assertThat(build.getConsole(), containsString(FIRST.getPrintln()));
        assertThat(build.getConsole(), containsString(SECOND.getPrintln()));
    }

    /**
     * Method to setup and run a pipeline build with a gradle file.
     * Copies a gradle file using the pipeline step to have it available.
     * Also sets up the necessary WorkflowJob runs it and checks for successful execution.
     * @param jenkinsFile Jenkins File to use.
     * @param gradleScript Gradle Script to use.
     * @return The build created by the WorkflowJob
     */
    private Build setUpAndRunPipelineBuild(final String jenkinsFile, final String gradleScript) {
        GradleInstallation.installGradle(jenkins);
        final WorkflowJob workflowJob = jenkins.jobs.create(WorkflowJob.class);

        String file = workflowJob.copyResourceStep(gradleScript);
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

}
