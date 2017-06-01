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
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.jenkinsci.test.acceptance.Matchers.containsRegexp;
import static org.jenkinsci.test.acceptance.Matchers.containsString;
import static org.junit.Assert.assertThat;

@WithPlugins("gradle")
public class GradlePluginTest extends AbstractJUnitTest {

    final private static String successFullBuild = "Finished: SUCCESS";

    @Test
    public void run_gradle_script() {
        final String gradleInstallationName = "Default";
        GradleInstallation.installGradle(jenkins, gradleInstallationName, GradleInstallation.LATEST_VERSION);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(gradleInstallationName);
        step.setTasks("hello");
        step.setSwitches("--quiet");
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
        step.setFile("hello.gradle");
        step.setVersion("gradle-1.5");
        step.setTasks("hello");
        step.setDir("gradle");
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
        step.setVersion(gradleInstallationName);
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
        final String gradleInstallationName = "Default";
        GradleInstallation.installGradle(jenkins, gradleInstallationName, GradleInstallation.LATEST_VERSION);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(gradleInstallationName);
        step.setTasks("hello");
        job.save();

        final Build build = job.startBuild();
        build.shouldSucceed();
        assertThat(build.getConsole(), containsString("Invoke Gradle script"));
    }

    @Test @WithPlugins("gradle@1.27")
    public void run_gradle_script_build_scan_link() {
        final String gradleInstallationName = "Default";
        GradleInstallation.installGradle(jenkins, gradleInstallationName, GradleInstallation.LATEST_VERSION);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(gradleInstallationName);
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
        GradleInstallation.installGradle(jenkins, "Default", GradleInstallation.LATEST_VERSION);
        final FreeStyleJob job = jenkins.jobs.create();
        GradleWrapper.addWrapperStep(job);
        job.save();
        
        final Build build = job.startBuild();
        build.shouldSucceed();
        assertThat(build.getConsole(), containsString(successFullBuild));
    }

    @Test
    public void run_gradle_environment_variables(){
        final String gradleInstallationName = "Default";
        GradleInstallation.installGradle(jenkins, gradleInstallationName, GradleInstallation.LATEST_VERSION);

        final FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        final GradleStep step = job.addBuildStep(GradleStep.class);
        step.setVersion(gradleInstallationName);

        step.setVersion(gradleInstallationName);
        step.setTasks("environmentVariables");
        job.save();

        final Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("Build Number: " + build.getNumber()));
        assertThat(build.getConsole(), containsString("Build Name: " + build.getName()));
    }
}
