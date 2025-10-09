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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.junit.Assert.assertTrue;

import jakarta.inject.Inject;
import java.io.IOException;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
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
import org.jenkinsci.test.acceptance.plugins.git_client.ssh_host_key_verification.NoVerificationStrategy;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.plugins.workflow_multibranch.GithubBranchSource;
import org.jenkinsci.test.acceptance.plugins.workflow_shared_library.WorkflowGithubSharedLibrary;
import org.jenkinsci.test.acceptance.plugins.workflow_shared_library.WorkflowSharedLibraryGlobalConfig;
import org.jenkinsci.test.acceptance.po.Artifact;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@WithPlugins("command-launcher")
public class WorkflowPluginTest extends AbstractJUnitTest {
    private static final String CREDENTIALS_ID = "pipeline";
    private static final String KEY_FILENAME = "/org/jenkinsci/test/acceptance/docker/fixtures/GitContainer/unsafe";
    private static final String SHARED_LIBRARY_NAME = "Greeting";
    private static final String NAME = "myname";
    private static final String EXPECTED_OUTPUT_FROM_LIBRARY_VARS = "Hello from vars my friend " + NAME;

    @Inject
    private SlaveController slaveController;

    @Inject
    DockerContainerHolder<GitContainer> gitServer;

    @Inject
    DockerContainerHolder<SvnContainer> svn;

    @Inject
    JenkinsController controller;

    @Before
    public void useNoVerificationSshHostKeyStrategy() {
        GlobalSecurityConfig sc = new GlobalSecurityConfig(jenkins);
        sc.open();
        sc.useSshHostKeyVerificationStrategy(NoVerificationStrategy.class);
        sc.save();
    }

    @Category(DockerTest.class)
    @WithDocker
    @WithCredentials(
            credentialType = WithCredentials.SSH_USERNAME_PRIVATE_KEY,
            values = {CREDENTIALS_ID, KEY_FILENAME})
    @WithPlugins({"workflow-job", "workflow-cps", "workflow-basic-steps", "git"})
    @Test
    public void hello_world_from_git() throws IOException {
        String gitRepositoryUrl = createGitRepositoryInDockerContainer();

        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.setJenkinsFileRepository(gitRepositoryUrl, CREDENTIALS_ID);
        job.save();
        Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("Hello Jenkinsfile in Git"));
    }

    private String createGitRepositoryInDockerContainer() throws IOException {
        try (GitRepo repo = new GitRepo()) {
            repo.addFilesIn(getClass().getResource("/pipelines/hello-world"));
            repo.commit("Initial commit.");

            GitContainer container = gitServer.get();
            repo.transferToDockerContainer(container.host(), container.port());

            return container.getRepoUrl();
        }
    }

    @WithPlugins({
        "workflow-job",
        "workflow-cps",
        "workflow-basic-steps",
        "workflow-durable-task-step",
        "pipeline-input-step",
        "junit",
        "git"
    })
    @Test
    public void linearFlow() throws Exception {
        MavenInstallation.installMaven(jenkins, "M3", "3.9.4");
        final DumbSlave slave = (DumbSlave) slaveController.install(jenkins).get();
        slave.configure(() -> slave.labels.set("remote"));
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set("node('remote') {\n" + "  git 'https://github.com/jenkinsci/hello-world-maven-builder.git'\n"
                + "  def v = version()\n"
                + "  if (v) {\n"
                + "    echo(/Building version $v/)\n"
                + "  }\n"
                + "  def mvnHome = tool 'M3'\n"
                + "  withEnv([\"PATH+MAVEN=$mvnHome/bin\", \"M2_HOME=$mvnHome\"]) {\n"
                + "    sh 'mvn -B -Dmaven.test.failure.ignore verify'\n"
                + "  }\n"
                + "  input 'Ready to go?'\n"
                + "  archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true\n"
                + "  junit '**/target/surefire-reports/TEST-*.xml'\n"
                + "}\n"
                + "def version() {\n"
                + "  def matcher = readFile('pom.xml') =~ '<version>(.+)</version>'\n"
                + "  matcher ? matcher[0][1] : null\n"
                + "}");
        job.sandbox.check();
        job.save();
        final Build build = job.startBuild();
        waitFor().until(new Wait.Predicate<Boolean>() {
            @Override
            public Boolean apply() throws Exception {
                return build.getConsole().contains("Ready to go?");
            }

            @Override
            public String diagnose(Throwable lastException, String message) {
                return "Console output:\n" + build.getConsole() + "\n";
            }
        });
        assertThat(build.getConsole(), containsString("Building version 1.0-SNAPSHOT"));

        jenkins.restart();
        // Default 120s timeout of Build.waitUntilFinished sometimes expires waiting for RetentionStrategy.Always to
        // tick (after initial failure of CommandLauncher.launch: EOFException: unexpected stream termination):
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
        new Artifact(build, "target/example-1.0-SNAPSHOT.jar").assertThatExists(true);
        build.open();
        clickLink("Tests");
        assertThat(driver, hasContent("All Tests"));
    }

    @WithPlugins({
        "workflow-job",
        "workflow-cps",
        "workflow-basic-steps",
        "workflow-durable-task-step",
        "parallel-test-executor",
        "junit",
        "git"
    })
    @Native("mvn")
    @Test
    public void parallelTests() throws Exception {
        for (int i = 0; i < 3; i++) {
            slaveController.install(jenkins);
        }
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set("node('built-in || master') {\n" +
                // TODO could be switched to multibranch, in which case this initial `node` is unnecessary, and each
                // branch can just `checkout scm`
                "  git 'https://github.com/jenkinsci/parallel-test-executor-plugin-sample.git'\n"
                + "  stash 'sources'\n"
                + "}\n"
                + "def splits = splitTests parallelism: count(3), estimateTestsFromFiles: true\n"
                + "def branches = [:]\n"
                + "for (int i = 0; i < splits.size(); i++) {\n"
                + "  def exclusions = splits.get(i);\n"
                + "  branches[\"split${i}\"] = {\n"
                + "    node('!master') {\n"
                + "      sh 'rm -rf *'\n"
                + "      unstash 'sources'\n"
                + "      writeFile file: 'exclusions.txt', text: exclusions.join(\"\\n\")\n"
                +
                // Do not bother with ${tool 'M3'}; would take too long to unpack Maven on all slaves.
                // TODO would be useful for ToolInstallation to support the URL installer, hosting the tool ZIP
                // ourselves somewhere cached.
                "      sh 'mvn -B -Dmaven.test.failure.ignore test'\n"
                + "      junit 'target/surefire-reports/*.xml'\n"
                + "    }\n"
                + "  }\n"
                + "}\n"
                + "parallel branches");
        job.sandbox.check();
        job.save();
        Build build = job.startBuild();
        try {
            build.shouldSucceed();
        } catch (AssertionError x) { // cf. linearFlow
            build.shouldBeUnstable();
        }
        assertThat(build.getConsole(), containsString("No record available"));

        build = job.startBuild();
        try {
            build.shouldSucceed();
        } catch (AssertionError x) {
            build.shouldBeUnstable();
        }
        assertThat(build.getConsole(), containsString("divided into 3 sets"));
    }

    /** Pipeline analogue of {@link SubversionPluginTest#build_has_changes}. */
    @Category(DockerTest.class)
    @WithDocker
    @WithPlugins({"workflow-cps", "workflow-job", "workflow-durable-task-step", "subversion"})
    @Test
    public void subversion() throws Exception {
        final SvnContainer svnContainer = svn.get();
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set("node {svn '" + svnContainer.getUrlUnauthenticatedRepoAtRevision(1) + "'}");
        job.save();
        job.startBuild().shouldSucceed();
        job.configure();
        job.script.set("node {svn '" + svnContainer.getUrlUnauthenticatedRepoAtRevision(2) + "'}");
        job.save();
        Build b2 = job.startBuild().shouldSucceed();
        assertTrue(b2.getChanges().hasChanges());
    }

    @WithPlugins({
        "git",
        "workflow-job",
        "workflow-cps",
        "workflow-basic-steps",
        "workflow-durable-task-step",
        "workflow-multibranch",
        "github-branch-source",
        "pipeline-groovy-lib"
    })
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

        WorkflowGithubSharedLibrary sharedLibrary =
                new WorkflowSharedLibraryGlobalConfig(jenkins).addSharedLibrary(WorkflowGithubSharedLibrary.class);
        sharedLibrary.name.set(SHARED_LIBRARY_NAME);
        final GithubBranchSource source = sharedLibrary.selectSCM();

        source.repoUrl("https://github.com/varyvoltest/pipeline-basic-shared-library.git");

        jenkins.save();
    }

    private WorkflowJob configureJob() {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set("@Library('" + SHARED_LIBRARY_NAME + "@master') _\n" + "\n" + "otherGreeting('" + NAME + "')");
        job.sandbox.check();
        job.save();

        return job;
    }
}
