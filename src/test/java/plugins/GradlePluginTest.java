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
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Parameter;
import org.jenkinsci.test.acceptance.po.StringParameter;
import org.junit.Test;

import static org.jenkinsci.test.acceptance.Matchers.containsRegexp;
import static org.jenkinsci.test.acceptance.Matchers.containsString;
import static org.junit.Assert.assertThat;

@WithPlugins("gradle")
public class GradlePluginTest extends AbstractJUnitTest {

    @Test
    public void run_gradle_script() {
        final String gradleInstallationName = "Default";
        GradleInstallation.installGradle(jenkins, gradleInstallationName, GradleInstallation.LATEST_VERSION);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.useVersion(gradleInstallationName);
        step.tasks.set("hello");
        step.switches.set("--quiet");
        job.save();

        final Build build = job.startBuild();
        build.shouldSucceed();
        assertThat(build.getConsole(), containsString("Hello world!"));
        assertThat(build.getConsole(), containsRegexp("gradle.* --quiet"));
    }

    @Test
    public void run_gradle_script_in_dir() {
        GradleInstallation.installGradle(jenkins, "gradle-1.5", "1.5");

        FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "gradle/hello.gradle");
        GradleStep step = job.addBuildStep(GradleStep.class);
        step.file.set("hello.gradle");
        step.useVersion("gradle-1.5");
        step.tasks.set("hello");
        step.dir.set("gradle");
        job.save();

        final Build build = job.startBuild();
        build.shouldSucceed();
        assertThat(build.getConsole(), containsString("Hello world!"));
    }


    @Test
    public void run_gradle_script_multiple_tasks() {
        final String gradleInstallationName = "Default";
        GradleInstallation.installGradle(jenkins, gradleInstallationName, GradleInstallation.LATEST_VERSION);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.useVersion(gradleInstallationName);
        step.tasks.set("firstTask secondTask");
        job.save();

        final Build build = job.startBuild();
        build.shouldSucceed();
        assertThat(build.getConsole(), containsString("First!"));
        assertThat(build.getConsole(), containsString("Second!"));
    }

    @Test
    public void run_gradle_job_parameters(){
        final String gradleInstallationName = "Default";
        GradleInstallation.installGradle(jenkins, gradleInstallationName, GradleInstallation.LATEST_VERSION);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        job.addParameter(StringParameter.class).setName("TEST_PARAM_1").setDefault("hello");
        job.addParameter(StringParameter.class).setName("TEST_PARAM_2").setDefault("world");

        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.useVersion(gradleInstallationName);
        step.tasks.set("jobParameters");
        step.passAsProperties.click();
        job.save();

        final Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("Gradle Properties: hello world"));
    }

}
