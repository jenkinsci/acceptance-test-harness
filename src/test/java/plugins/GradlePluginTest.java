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
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.ToolInstallation;
import org.junit.Test;

@WithPlugins("gradle")
public class GradlePluginTest extends AbstractJUnitTest {

    @Test
    public void run_gradle_scirpt() {
        install("gradle-1.5", "1.5");

        FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "build.gradle");
        GradleStep step = job.addBuildStep(GradleStep.class);
        step.useVersion("gradle-1.5");
        step.tasks.set("hello");
        step.switches.set("--quiet");
        job.save();

        job.startBuild().shouldSucceed()
                .shouldContainsConsoleOutput("Hello world!")
                //reg expression gradle[\w\\/"]* --quiet
                .shouldContainsConsoleOutput("gradle[\\w\\\\/\"]* --quiet")
        ;
    }

    @Test
    public void run_gradle_scirpt_in_dir() {
        install("gradle-1.5", "1.5");

        FreeStyleJob job = jenkins.jobs.create();
        job.copyResource(resource("/gradle_plugin/script.gradle"), "gradle/hello.gradle");
        GradleStep step = job.addBuildStep(GradleStep.class);
        step.file.set("hello.gradle");
        step.useVersion("gradle-1.5");
        step.tasks.set("hello");
        step.dir.set("gradle");
        job.save();

        job.startBuild().shouldSucceed().shouldContainsConsoleOutput("Hello world!");
    }

    private void install(String name, String version) {
        ToolInstallation.waitForUpdates(jenkins, GradleInstallation.class);
        JenkinsConfig global = jenkins.getConfigPage();
        global.configure();
        ToolInstallation tool = global.addTool(GradleInstallation.class);
        tool.name.set(name);
        tool.installVersion(version);
        global.save();
    }
}
