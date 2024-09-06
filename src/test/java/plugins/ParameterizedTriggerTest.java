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

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.compress_artifacts.CompressingArtifactManager;
import org.jenkinsci.test.acceptance.plugins.parameterized_trigger.FileBuildParameters;
import org.jenkinsci.test.acceptance.plugins.parameterized_trigger.ParameterizedTrigger;
import org.jenkinsci.test.acceptance.plugins.parameterized_trigger.TriggerConfig;
import org.jenkinsci.test.acceptance.po.ArtifactArchiver;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.StringParameter;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

@WithPlugins("parameterized-trigger")
public class ParameterizedTriggerTest extends AbstractJUnitTest {

    @Test
    @WithPlugins({"compress-artifacts", "matrix-project" /*JENKINS-33910*/})
    @Issue("JENKINS-28980")
    public void triggerWithNonStandardArchiver() {
        CompressingArtifactManager.setup(jenkins);

        FreeStyleJob target = jenkins.jobs.create();
        target.configure();
        target.addParameter(StringParameter.class).setName("PARAM");
        target.addShellStep("test 'value' = $PARAM");
        target.save();

        FreeStyleJob trigger = jenkins.jobs.create();
        trigger.configure();
        trigger.addShellStep("echo 'PARAM=value' > my.properties");

        ArtifactArchiver archiver = trigger.addPublisher(ArtifactArchiver.class);
        archiver.includes("my.properties");

        ParameterizedTrigger step = trigger.addPublisher(ParameterizedTrigger.class);
        TriggerConfig config = step.getTriggerConfig(0);
        config.projects.set(target.name);
        FileBuildParameters params = config.addParameter(FileBuildParameters.class);
        params.file.set("my.properties");
        params.failIfMissing.check();
        trigger.save();

        trigger.startBuild().shouldSucceed();
        Build downstream = target.build(1);
        downstream.shouldSucceed();
    }
}
