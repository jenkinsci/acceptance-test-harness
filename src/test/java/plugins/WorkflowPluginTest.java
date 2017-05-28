/*
 * The MIT License
 *
 * Copyright 2014 Jesse Glick.
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

import java.io.File;
import java.util.concurrent.Callable;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.controller.LocalController;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.DockerAgentContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.GitContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.SvnContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.Wait;
import org.jenkinsci.test.acceptance.junit.WithCredentials;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.git.GitRepo;
import org.jenkinsci.test.acceptance.plugins.git_client.JGitInstallation;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.plugins.ssh_slaves.SshSlaveLauncher;
import org.jenkinsci.test.acceptance.plugins.workflow_multibranch.GithubBranchSource;
import org.jenkinsci.test.acceptance.plugins.workflow_shared_library.WorkflowGithubSharedLibrary;
import org.jenkinsci.test.acceptance.plugins.workflow_shared_library.WorkflowSharedLibraryGlobalConfig;
import org.jenkinsci.test.acceptance.po.Artifact;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.jenkinsci.utils.process.CommandBuilder;
import static org.junit.Assert.*;
import org.junit.Assume;
import static org.junit.Assume.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.jvnet.hudson.test.Issue;

/**
 * Roughly follows <a href="https://github.com/jenkinsci/workflow-plugin/blob/master/TUTORIAL.md">the tutorial</a>.
 */
public class WorkflowPluginTest extends AbstractJUnitTest {

    private static final String SHARED_LIBRARY_NAME = "Greeting";
    private static final String NAME = "myname";
    private static final String EXPECTED_OUTPUT_FROM_LIBRARY_VARS = "Hello from vars my friend " + NAME;

    @Inject private SlaveController slaveController;
    @Inject DockerContainerHolder<GitContainer> gitServer;
    @Inject DockerContainerHolder<SvnContainer> svn;
    @Inject DockerContainerHolder<DockerAgentContainer> agent;
    @Inject JenkinsController controller;

    @WithPlugins("workflow-aggregator@1.1")
    @Test public void helloWorld() throws Exception {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set("echo 'hello from Workflow'");
        job.sandbox.check();
        job.save();
        job.startBuild().shouldSucceed().shouldContainsConsoleOutput("hello from Workflow");
    }

    @WithPlugins({"workflow-aggregator@2.0", "workflow-cps@2.10", "workflow-basic-steps@2.1", "junit@1.18", "git@2.3"})
    @Test public void linearFlow() throws Exception {
        assumeTrue("This test requires a restartable Jenkins", jenkins.canRestart());
        MavenInstallation.installMaven(jenkins, "M3", "3.1.0");
        final DumbSlave slave = (DumbSlave) slaveController.install(jenkins).get();
        slave.configure(new Callable<Void>() {
            @Override public Void call() throws Exception {
                slave.labels.set("remote");
                return null;
            }
        });
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(
            "node('remote') {\n" +
            "  git 'https://github.com/jglick/simple-maven-project-with-tests.git'\n" +
            "  def v = version()\n" +
            "  if (v) {\n" +
            "    echo \"Building version ${v}\"\n" +
            "  }\n" +
            "  def mvnHome = tool 'M3'\n" +
            "  withEnv([\"PATH+MAVEN=${mvnHome}/bin\", \"M2_HOME=${mvnHome}\"]) {\n" +
            "    sh 'mvn -B -Dmaven.test.failure.ignore verify'\n" +
            "  }\n" +
            "  input 'Ready to go?'\n" +
            "  step([$class: 'ArtifactArchiver', artifacts: '**/target/*.jar', fingerprint: true])\n" + // TODO Jenkins 2.2+: archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
            "  junit '**/target/surefire-reports/TEST-*.xml'\n" +
            "}\n" +
            "def version() {\n" +
            "  def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'\n" +
            "  matcher ? matcher[0][1] : null\n" +
            "}");
        job.sandbox.check();
        job.save();
        final Build build = job.startBuild();
        waitFor().until(new Wait.Predicate<Boolean>() {
            @Override public Boolean apply() throws Exception {
                return build.getConsole().contains("Ready to go?");
            }
            @Override public String diagnose(Throwable lastException, String message) {
                return "Console output:\n" + build.getConsole() + "\n";
            }
        });
        build.shouldContainsConsoleOutput("Building version 1.0-SNAPSHOT");
        jenkins.restart();
        // Default 120s timeout of Build.waitUntilFinished sometimes expires waiting for RetentionStrategy.Always to tick (after initial failure of CommandLauncher.launch: EOFException: unexpected stream termination):
        slave.waitUntilOnline(); // TODO rather wait for build output: "Ready to run"
        visit(build.getConsoleUrl());
        clickLink("Proceed");
        try {
            build.shouldSucceed();
        } catch (AssertionError x) {
            // Tests in this project are currently designed to fail at random, so either status is OK.
            // TODO if resultIs were public and there were a disjunction combinator for Matcher we could use it here.
            build.shouldBeUnstable();
        }
        new Artifact(build, "target/simple-maven-project-with-tests-1.0-SNAPSHOT.jar").assertThatExists(true);
        build.open();
        clickLink("Test Result");
        assertThat(driver, hasContent("All Tests"));
    }

    @WithPlugins({"workflow-aggregator@2.0", "workflow-cps@2.10", "workflow-basic-steps@2.1", "parallel-test-executor@1.9", "junit@1.18", "git@2.3"})
    @Native("mvn")
    @Test public void parallelTests() throws Exception {
        for (int i = 0; i < 3; i++) {
            slaveController.install(jenkins);
        }
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(
            "node('master') {\n" +
            // TODO could be switched to multibranch, in which case this initial `node` is unnecessary, and each branch can just `checkout scm`
            "  git 'https://github.com/jenkinsci/parallel-test-executor-plugin-sample.git'\n" +
            "  stash 'sources'\n" +
            "}\n" +
            "def splits = splitTests count(3)\n" +
            "def branches = [:]\n" +
            "for (int i = 0; i < splits.size(); i++) {\n" +
            "  def exclusions = splits.get(i);\n" +
            "  branches[\"split${i}\"] = {\n" +
            "    node('!master') {\n" +
            "      sh 'rm -rf *'\n" +
            "      unstash 'sources'\n" +
            "      writeFile file: 'exclusions.txt', text: exclusions.join(\"\\n\")\n" +
            // Do not bother with ${tool 'M3'}; would take too long to unpack Maven on all slaves.
            // TODO would be useful for ToolInstallation to support the URL installer, hosting the tool ZIP ourselves somewhere cached.
            "      sh 'mvn -B -Dmaven.test.failure.ignore test'\n" +
            "      junit 'target/surefire-reports/*.xml'\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "parallel branches");
        job.sandbox.check();
        job.save();
        Build build = job.startBuild();
        try {
            build.shouldSucceed();
        } catch (AssertionError x) { // cf. linearFlow
            build.shouldBeUnstable();
        }
        build.shouldContainsConsoleOutput("No record available"); // first run
        build = job.startBuild();
        try {
            build.shouldSucceed();
        } catch (AssertionError x) {
            build.shouldBeUnstable();
        }
        build.shouldContainsConsoleOutput("divided into 3 sets");
    }

    @Category(DockerTest.class)
    @WithDocker
    @WithPlugins({"workflow-cps@2.0", "workflow-job@2.0", "workflow-durable-task-step@2.0", "docker-workflow@1.5", "git", "ssh-agent@1.10", "ssh-slaves@1.11"})
    @WithCredentials(credentialType=WithCredentials.SSH_USERNAME_PRIVATE_KEY, values={"git", "/org/jenkinsci/test/acceptance/docker/fixtures/GitContainer/unsafe"}, id="gitcreds")
    @Issue("JENKINS-27152")
    @Test public void sshGitInsideDocker() throws Exception {
        GitContainer gitContainer = gitServer.get();
        GitRepo repo = new GitRepo();
        repo.changeAndCommitFoo("Initial commit");
        repo.transferToDockerContainer(gitContainer.host(), gitContainer.port());
        DumbSlave slave = jenkins.slaves.create(DumbSlave.class);
        slave.setExecutors(1);
        slave.remoteFS.set("/home/test"); // TODO perhaps should be a constant in SshdContainer
        SshSlaveLauncher launcher = slave.setLauncher(SshSlaveLauncher.class);
        Process proc = new ProcessBuilder("stat", "-c", "%g", "/var/run/docker.sock").start();
        String group = IOUtils.toString(proc.getInputStream()).trim();
        Assume.assumeThat("docker.sock can be statted", proc.waitFor(), is(0));
        try {
            Integer.parseInt(group);
        } catch (NumberFormatException x) {
            Assume.assumeNoException("unexpected output from stat on docker.sock", x);
        }
        // Note that we need to link to the Git container both on the agent, for the benefit of JGit (TODO pending JENKINS-30600), and in the build container, for the benefit of git-pull:
        DockerAgentContainer agentContainer = agent.starter().withOptions(new CommandBuilder("--volume=/var/run/docker.sock:/var/run/docker.sock", "--env=DOCKER_GROUP=" + group, "--link=" + gitContainer.getCid() + ":git")).start();
        launcher.host.set(agentContainer.ipBound(22));
        launcher.port(agentContainer.port(22));
        launcher.pwdCredentials("test", "test");
        launcher.setSshHostKeyVerificationStrategy(SshSlaveLauncher.NonVerifyingKeyVerificationStrategy.class);
        slave.save();
        { // TODO JENKINS-30600 workaround
            JGitInstallation.addJGit(jenkins);
            find(by.button("Save")).click();
        }
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(
            "node('" + slave.getName() + "') {\n" +
            "  docker.image('cloudbees/java-build-tools').inside('--link=" + gitContainer.getCid() + ":git') {\n" +
            // TODO JENKINS-30600: "    git url: '" + gitContainer.getRepoUrlInsideDocker("git") + "', credentialsId: 'gitcreds'\n" +
            "    checkout([$class: 'GitSCM', userRemoteConfigs: [[url: '" + gitContainer.getRepoUrlInsideDocker("git") + "', credentialsId: 'gitcreds']], gitTool: 'jgit'])\n" +
            "    sh 'mkdir ~/.ssh && echo StrictHostKeyChecking no > ~/.ssh/config'\n" +
            "    sshagent(['gitcreds']) {sh 'ls -l $SSH_AUTH_SOCK && git pull origin master'}\n" +
            "  }\n" +
            "}");
        job.sandbox.check();
        job.save();
        assertThat(job.startBuild().shouldSucceed().getConsole(), containsString("-> FETCH_HEAD"));
    }

    @WithPlugins({"workflow-cps-global-lib@2.3", "workflow-basic-steps@2.1", "workflow-job@2.5"})
    @Issue("JENKINS-26192")
    @Test public void grapeLibrary() throws Exception {
        assumeThat("TODO otherwise we would need to set up SSH access to push via Git, which seems an awful hassle", controller, instanceOf(LocalController.class));
        File workflowLibs = /* WorkflowLibRepository.workspace() */ new File(((LocalController) controller).getJenkinsHome(), "workflow-libs");
        // Cf. GrapeTest.useBinary using JenkinsRule:
        FileUtils.write(new File(workflowLibs, "src/pkg/Lists.groovy"),
            "package pkg\n" +
            "@Grab('commons-primitives:commons-primitives:1.0')\n" +
            "import org.apache.commons.collections.primitives.ArrayIntList\n" +
            "static def arrayInt() {new ArrayIntList()}");
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set("echo(/got ${pkg.Lists.arrayInt()}/)");
        job.sandbox.check();
        job.save();
        assertThat(job.startBuild().shouldSucceed().getConsole(), containsString("got []"));
    }

    /** Pipeline analogue of {@link SubversionPluginTest#build_has_changes}. */
    @Category(DockerTest.class)
    @WithDocker
    @WithPlugins({"workflow-cps@2.12", "workflow-job@2.5", "workflow-durable-task-step@2.4", "subversion@2.6"})
    @Test public void subversion() throws Exception {
        final SvnContainer svnContainer = svn.get();
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set("node {svn '" + svnContainer.getUrlUnsaveRepoAtRevision(1) + "'}");
        job.save();
        job.startBuild().shouldSucceed();
        job.configure();
        job.script.set("node {svn '" + svnContainer.getUrlUnsaveRepoAtRevision(2) + "'}");
        job.save();
        Build b2 = job.startBuild().shouldSucceed();
        assertTrue(b2.getChanges().hasChanges());
    }

    @WithPlugins({"git@3.0.1", "workflow-job", "workflow-cps", "workflow-basic-steps", "workflow-durable-task-step", "workflow-multibranch", "github-branch-source", "workflow-cps-global-lib"})
    @Test
    public void testSharedLibraryFromGithub() {
        this.configureSharedLibrary();

        WorkflowJob job = configureJob();
        Build b = job.startBuild().shouldSucceed();

        String consoleOutput = b.getConsole();
        assertThat(consoleOutput, containsString(EXPECTED_OUTPUT_FROM_LIBRARY_VARS));
    }

    private void configureSharedLibrary() {
        jenkins.configure();

        WorkflowGithubSharedLibrary sharedLibrary = new WorkflowSharedLibraryGlobalConfig(jenkins).addSharedLibrary(WorkflowGithubSharedLibrary.class);
        sharedLibrary.name.set(SHARED_LIBRARY_NAME);
        final GithubBranchSource source = sharedLibrary.selectSCM();

        source.owner("varyvoltest");
        source.selectRepository("pipeline-basic-shared-library");

        jenkins.save();
    }

    private WorkflowJob configureJob() {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(
                "@Library('" + SHARED_LIBRARY_NAME + "@master') _\n" +
                        "\n" +
                        "otherGreeting('" + NAME + "')");
        job.sandbox.check();
        job.save();

        return job;
    }

}
