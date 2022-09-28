package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Test;

import javax.inject.Inject;

import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.containsRegexp;

@Since("2.7.1")
@WithPlugins("command-launcher")
public class DeclarativePipelineTest extends AbstractJUnitTest {
    @Inject
    private SlaveController slaveController;

    @WithPlugins("pipeline-model-definition")
    @Test
    public void basicDeclarativeTests() throws Exception {
        WorkflowJob helloWorldJob = jenkins.jobs.create(WorkflowJob.class);
        helloWorldJob.script.set(
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

        helloWorldJob.sandbox.check();
        helloWorldJob.save();

        Build helloWorldBuild = helloWorldJob.startBuild().shouldSucceed();
        assertThat(helloWorldBuild.getConsole(), containsRegexp("Hello world", Pattern.MULTILINE));

        MavenInstallation.installMaven(jenkins, "M3", "3.1.0");
        final DumbSlave slave = (DumbSlave) slaveController.install(jenkins).get();
        slave.configure((Callable<Void>) () -> {
            slave.labels.set("remote");
            return null;
        });
        WorkflowJob toolsEnvAgentJob = jenkins.jobs.create(WorkflowJob.class);
        toolsEnvAgentJob.script.set(
                "pipeline {\n" +
                "  agent { label 'remote' }\n" +
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

        toolsEnvAgentJob.sandbox.check();
        toolsEnvAgentJob.save();

        Build toolsEnvAgentBuild = toolsEnvAgentJob.startBuild().shouldSucceed();
        String toolsEnvAgentConsole = toolsEnvAgentBuild.getConsole();
        assertThat(toolsEnvAgentConsole, containsRegexp("\\(first\\)", Pattern.MULTILINE));
        assertThat(toolsEnvAgentConsole, containsRegexp("Apache Maven 3\\.1\\.0", Pattern.MULTILINE));
        assertThat(toolsEnvAgentConsole, containsRegexp("\\(second\\)", Pattern.MULTILINE));
        assertThat(toolsEnvAgentConsole, containsRegexp("FOO is BAR", Pattern.MULTILINE));

        WorkflowJob missingAgentJob = jenkins.jobs.create(WorkflowJob.class);
        missingAgentJob.script.set(
                "pipeline {\n" +
                "  stages {\n" +
                "    stage('foo') {\n" +
                "      steps {\n" +
                "        echo 'Hello world'\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}\n"
        );

        missingAgentJob.sandbox.check();
        missingAgentJob.save();

        Build missingAgentBuild = missingAgentJob.startBuild().shouldFail();
        String missingAgentConsole = missingAgentBuild.getConsole();
        assertThat(missingAgentConsole, containsRegexp("Missing required section ['\"]agent['\"]"));
        assertThat(missingAgentConsole, not(containsRegexp("Hello world")));

        WorkflowJob missingToolVersionJob = jenkins.jobs.create(WorkflowJob.class);
        missingToolVersionJob.script.set(
                "pipeline {\n" +
                "  agent { label 'remote' }\n" +
                "  tools {\n" +
                "    maven 'some-other-version'\n" +
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

        missingToolVersionJob.sandbox.check();
        missingToolVersionJob.save();

        Build missingToolVersionBuild = missingToolVersionJob.startBuild().shouldFail();
        String missingToolVersionConsole = missingToolVersionBuild.getConsole();
        assertThat(missingToolVersionConsole, containsRegexp("Tool type ['\"]maven['\"] does not have an install of ['\"]some-other-version['\"] configured"));
        assertThat(missingToolVersionConsole, not(containsRegexp("FOO is BAR")));
    }
}
