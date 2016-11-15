package plugins;

import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.GitContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.inject.Inject;

import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.containsRegexp;

@Since("2.7.1")
public class DeclarativePipelineTest extends AbstractJUnitTest {
    @Inject
    private SlaveController slaveController;
    @Inject
    DockerContainerHolder<GitContainer> gitServer;
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @WithPlugins("pipeline-model-definition")
    @Test
    public void basicDeclarativeTests() throws Exception {
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

        MavenInstallation.installMaven(jenkins, "M3", "3.1.0");
        final DumbSlave slave = (DumbSlave) slaveController.install(jenkins).get();
        slave.configure(new Callable<Void>() {
            @Override public Void call() throws Exception {
                slave.labels.set("remote");
                return null;
            }
        });
        WorkflowJob secondJob = jenkins.jobs.create(WorkflowJob.class);
        secondJob.script.set(
                "pipeline {\n" +
                "  agent label:'remote'\n" +
                "  tools {\n" +
                "    maven 'M3'\n" +
                "  }\n" +
                "  environment {\n" +
                "    FOO = 'BAR'\n" +
                "  }\n" +
                "  stages {\n" +
                "    stage('first') {\n" +
                "      steps {\n" +
                "        sh 'mvn --version'\n" +
                "      }\n" +
                "    }\n" +
                "    stage('second') {\n" +
                "      steps {\n" +
                "        echo \"FOO is ${env.FOO}\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n"
        );

        secondJob.sandbox.check();
        secondJob.save();

        Build secondBuild = secondJob.startBuild().shouldSucceed();
        String console = secondBuild.getConsole();
        assertThat(console, containsRegexp("\\(first\\)", Pattern.MULTILINE));
        assertThat(console, containsRegexp("Apache Maven 3\\.1\\.0", Pattern.MULTILINE));
        assertThat(console, containsRegexp("\\(second\\)", Pattern.MULTILINE));
        assertThat(console, containsRegexp("FOO is BAR", Pattern.MULTILINE));
    }
}
