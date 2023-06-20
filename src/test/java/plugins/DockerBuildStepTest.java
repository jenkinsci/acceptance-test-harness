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

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.*;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.docker_build_step.DockerBuildStep;
import org.jenkinsci.test.acceptance.plugins.docker_build_step.DockerCommand;
import org.jenkinsci.test.acceptance.plugins.docker_build_step.GlobalDockerConfig;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@WithPlugins("docker-build-step")
@Category(DockerTest.class)
@WithDocker
public class DockerBuildStepTest extends AbstractJUnitTest {

    @Inject
    SlaveController slaveController;

    // Address in form http://<HOST>:<PORT>
    private static final String DOCKER_DAEMON_TCP = System.getenv("DOCKER_DAEMON_TCP");

    @Before
    public void configureTcpEndpoint() {
        assumeNotNull(DOCKER_DAEMON_TCP);

        final JenkinsConfig global = jenkins.getConfigPage();
        global.configure();
        GlobalDockerConfig pluginConfig = new GlobalDockerConfig(global);
        pluginConfig.restApiUrl(DOCKER_DAEMON_TCP);
        pluginConfig.testConnection();
        global.save();
    }

    @Test
    public void start_and_stop_container_created_from_dockerfile() {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.copyDir(resource("/docker_build_step/context.dir"));
        command(job, DockerCommand.CreateImage.class).contextFolder("$WORKSPACE").tag("my_image");
        command(job, DockerCommand.CreateContainer.class).name("my_image");
        command(job, DockerCommand.StartContainers.class).containerIds("$DOCKER_CONTAINER_IDS");
        command(job, DockerCommand.RemoveContainers.class).containerIds("$DOCKER_CONTAINER_IDS");
        job.save();

        Build build = job.startBuild().waitUntilFinished();
        assertTrue(build.isSuccess());
    }

    @Test
    public void run_commands_remotelly() throws Exception {
        Slave slave = slaveController.install(jenkins).get();

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.setLabelExpression(slave.getName());
        job.copyDir(resource("/docker_build_step/context.dir"));
        command(job, DockerCommand.CreateImage.class).contextFolder("$WORKSPACE").tag("my_image");
        job.save();

        Build build = job.startBuild().waitUntilFinished();
        assertTrue(build.isSuccess());
    }

    private <T extends DockerCommand> T command(Job job, Class<T> type) {
        return job.addBuildStep(DockerBuildStep.class).command(type);
    }
}
