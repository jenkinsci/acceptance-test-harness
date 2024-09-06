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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.containsRegexp;
import static org.jenkinsci.test.acceptance.Matchers.containsString;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.gradle.GradleInstallation;
import org.jenkinsci.test.acceptance.plugins.gradle.GradleStep;
import org.jenkinsci.test.acceptance.plugins.gradle.GradleTask;
import org.jenkinsci.test.acceptance.plugins.gradle.GradleWrapper;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.StringParameter;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Test;

@WithPlugins("gradle")
public class GradlePluginTest extends AbstractJUnitTest {

    private static final String GRADLE_SCRIPT = "/gradle_plugin/script.gradle";
    private static final String GRADLE_SCRIPT_NO_PLUGIN = "/gradle_plugin/scriptNoPlugins.gradle";
    private static final String SUCCESSFUL_BUILD = "Finished: SUCCESS";

    @Test
    public void run_gradle_script() {
        GradleInstallation.installGradle(jenkins);
        FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource(GRADLE_SCRIPT), "gradle/hello.gradle");

        GradleStep step = job.addBuildStep(GradleStep.class);
        step.setFile("hello.gradle");
        step.setVersion(GradleInstallation.DEFAULT);
        step.setTasks("environmentVariables");
        step.setDir("gradle");
        step.setSwitches("--quiet --no-daemon");
        job.save();

        final Build build = job.startBuild();
        build.shouldSucceed();
        assertThat(build.getConsole(), containsRegexp("gradle.* --quiet"));
        assertThat(build.getConsole(), containsString("Build Number: " + build.getNumber()));
        assertThat(build.getConsole(), containsString("Build Name: " + build.getName()));
    }

    /**
     * Verify the execution of gradle build script as gradle wrapper localized divergent to default folder.
     */
    @Test
    public void run_gradle_wrapper() {
        GradleInstallation.installGradle(jenkins);
        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource(GRADLE_SCRIPT), "build.gradle");

        final String location = "test";
        GradleWrapper.downloadWrapperFiles(job);
        GradleWrapper.moveWrapperFiles(job, location);
        GradleWrapper.addWrapperStep(job, location, GradleTask.HELLO.getName());

        job.save();
        final Build build = job.startBuild().shouldSucceed();

        assertThat(build.getConsole(), containsString(SUCCESSFUL_BUILD));
        assertThat(build.getConsole(), containsString(GradleTask.HELLO.getPrintln()));
    }

    @Test
    public void run_gradle_job_parameters_as_project_properties() {
        GradleInstallation.installGradle(jenkins);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource(GRADLE_SCRIPT), "build.gradle");
        job.addParameter(StringParameter.class).setName("PROJ_PARAM_1").setDefault("hello");
        job.addParameter(StringParameter.class).setName("SYS_PARAM_1").setDefault("hello");

        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(GradleInstallation.DEFAULT);
        step.setTasks("jobParametersAsProjectProperties jobParametersAsSystemProperties");
        step.setProjectProperties("PROJ_PARAM_2=world");
        step.setSystemProperties("SYS_PARAM_2=world");
        step.setPassAllAsProjectProperties();
        step.setPassAllAsSystemProperties();
        step.setSwitches("--no-daemon");
        job.save();

        final Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("Project Properties: hello world"));
        assertThat(build.getConsole(), containsString("System Properties: hello world"));
    }

    /**
     * Runs a pipeline gradle build and verifies the build was executed successfully
     */
    @Test
    @WithPlugins("workflow-aggregator")
    public void run_gradle_pipeline_basic() {
        final Build build = setUpAndRunPipelineBuild("/gradle_plugin/pipeline_test_multiple_tasks.txt", GRADLE_SCRIPT);
        assertThat(build.getConsole(), containsString(GradleTask.SECOND.getPrintln()));
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

        String copyResourceStep = workflowJob.copyResourceStep(gradleScript);
        String test = "" + "pipeline {\n"
                + "agent any\n"
                + "stages {\n"
                + "stage ('prepare_build') {\n"
                + "steps {\n"
                + copyResourceStep
                + "}\n"
                + "}\n"
                + resource(jenkinsFile).asText() + "}\n"
                + "}";
        test = test.replaceAll("script.gradle", "build.gradle");

        workflowJob.script.set(test);

        workflowJob.sandbox.check();
        workflowJob.save();
        return workflowJob.startBuild().shouldSucceed();
    }
}
