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

import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.gradle.GradleInstallation;
import org.jenkinsci.test.acceptance.plugins.gradle.GradleStep;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import java.util.regex.Pattern;

@WithPlugins("gradle")
public class GradlePluginTest extends AbstractJUnitTest {

    @Test
    public void run_gradle_scirpt() {
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
        Matchers.containsRegexp("Hello world!", Pattern.MULTILINE);
        Matchers.containsRegexp("gradle.* --quiet", Pattern.MULTILINE);
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

        job.startBuild().shouldSucceed();
        Matchers.containsRegexp("Hello world!", Pattern.MULTILINE);
    }
}
