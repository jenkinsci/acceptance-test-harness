/*
 * The MIT License
 *
 * Copyright (c) 2015 Red Hat, Inc.
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
import static org.hamcrest.Matchers.containsString;

import javax.inject.Named;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.TestActivation;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.beaker_builder.BeakerBuilder;
import org.jenkinsci.test.acceptance.plugins.beaker_builder.BeakerGlobalConfig;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import com.google.inject.Inject;

/**
 * @author ogondza
 */
@WithPlugins("beaker-builder")
@TestActivation({"URL", "LOGIN", "PASSWORD"})
public class BeakerBuilderPluginTest extends AbstractJUnitTest {

    @Inject(optional = true) @Named("BeakerBuilderPluginTest.URL") private String URL;
    @Inject(optional = true) @Named("BeakerBuilderPluginTest.LOGIN") private String LOGIN;
    @Inject(optional = true) @Named("BeakerBuilderPluginTest.PASSWORD") private String PASSWORD;

    @Test
    public void runBeakerTask() {
        setupGlobalConfig();

        FreeStyleJob job = jenkins.jobs.create();
        {
            job.configure();
            job.copyResource(resource("/beaker_builder_plugin/job.xml"));

            BeakerBuilder beaker = job.addBuildStep(BeakerBuilder.class);
            beaker.fileXml("beaker_job_name", "job.xml");

            job.addShellStep("echo BEAKER_JOB_ID=$BEAKER_JOB_ID");
        }
        job.save();

        Build build = job.scheduleBuild().waitUntilFinished(60*60*60);
        assertThat(build.getConsole(), containsString("Job successfully submitted to Beaker"));
        build.shouldSucceed();
    }

    private void setupGlobalConfig() {
        jenkins.configure();
        BeakerGlobalConfig config = new BeakerGlobalConfig(jenkins.getConfigPage());
        config.setUrl(URL).setLogin(LOGIN).setPassword(PASSWORD);
        jenkins.save();
    }
}
