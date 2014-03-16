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
import org.jenkinsci.test.acceptance.plugins.envinject.EnvInjectBuildAction;
import org.jenkinsci.test.acceptance.plugins.envinject.EnvInjectConfig;
import org.jenkinsci.test.acceptance.plugins.envinject.EnvInjectStep;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

@WithPlugins("envinject")
public class EnvInjectPluginTest extends AbstractJUnitTest {

    @Test
    public void inject_in_step() {
        final FreeStyleJob job = getJob();
        job.addBuildStep(EnvInjectStep.class).properties.sendKeys("ENV_VAR_TEST=injected variable test");
        job.addShellStep("echo value=$ENV_VAR_TEST");
        job.save();

        assertVariableUsed(job.queueBuild());
    }

    @Test
    public void inject_variables_to_the_build() {
        final FreeStyleJob job = getJob();
        new EnvInjectConfig.Environment(job).properties.sendKeys("ENV_VAR_TEST=injected variable test");
        job.addShellStep("echo value=$ENV_VAR_TEST");
        job.save();

        assertVariableUsed(job.queueBuild());
    }

    @Test
    public void prepare_environment_for_run() {
        final FreeStyleJob job = getJob();
        new EnvInjectConfig.Property(job).properties.sendKeys("ENV_VAR_TEST=injected variable test");
        job.addShellStep("echo value=$ENV_VAR_TEST");
        job.save();

        assertVariableUsed(job.queueBuild());
    }

    private FreeStyleJob getJob() {
        final FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class);
        job.configure();
        return job;
    }

    private void assertVariableUsed(Build build) {
        build.shouldSucceed();
        build.shouldContainsConsoleOutput("ENV_VAR_TEST=injected variable test");
        build.shouldContainsConsoleOutput("value=injected variable test");
        new EnvInjectBuildAction(injector, build).shouldContain("ENV_VAR_TEST", "injected variable test");
    }
}
