package plugins;

import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.GitContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.inject.Inject;

import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.containsRegexp;

@WithPlugins("pipeline-model-definition@0.5")
@Since("2.7.1")
public class DeclarativePipelineTest extends AbstractJUnitTest {
    @Inject
    private SlaveController slaveController;
    @Inject
    DockerContainerHolder<GitContainer> gitServer;
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void helloWorld() {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(
                "pipeline {\n" +
                "  agent none\n" +
                "  stages {\n" +
                "    stage('foo') {\n" +
                "      steps {\n" +
                "        echo 'Hello world'\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n"
        );

        job.sandbox.check();
        job.save();

        Build b = job.startBuild().shouldSucceed();
        assertThat(b.getConsole(), containsRegexp("Hello world", Pattern.MULTILINE));
    }
}
