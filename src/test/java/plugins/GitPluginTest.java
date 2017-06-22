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

import javax.inject.Inject;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.GitContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.junit.WithCredentials;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.git.GitRepo;
import org.jenkinsci.test.acceptance.plugins.git.GitScm;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Job;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;

@WithDocker
@Category(DockerTest.class)
@WithPlugins("git")
@WithCredentials(credentialType = WithCredentials.SSH_USERNAME_PRIVATE_KEY, values = {"gitplugin", "/org/jenkinsci/test/acceptance/docker/fixtures/GitContainer/unsafe"})
public class GitPluginTest extends AbstractJUnitTest {

    private static final String USERNAME = "gitplugin";

    @Inject
    DockerContainerHolder<GitContainer> gitServer;

    private Job job;

    private GitContainer container;
    private String repoUrl;
    private String host;
    private int port;

    @Before
    public void init() {
        container = gitServer.get();
        repoUrl = container.getRepoUrl();
        host = container.host();
        port = container.port();
        job = jenkins.jobs.create();
        job.configure();
    }

    @Test
    @Category(SmokeTest.class)
    public void simple_checkout() throws InterruptedException, JSchException, SftpException, IOException {
        buildGitRepo()
                .transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME);
        job.addShellStep("test -f foo");
        job.save();

        job.startBuild().shouldSucceed();
    }

    @Test
    public void checkout_branch() throws InterruptedException, JSchException, SftpException, IOException {
        GitRepo repo = buildGitRepo();
        repo.git("branch", "svn");
        repo.transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .branch("svn");
        job.addShellStep("test `git rev-parse origin/svn` = `git rev-parse HEAD`");
        job.save();

        job.startBuild().shouldSucceed();
    }

    @Test
    public void name_remote_repo() throws IOException, InterruptedException, SftpException, JSchException {
        buildGitRepo()
                .transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .remoteName("custom_origin");
        job.addShellStep("test -f foo && git remote -v");
        job.save();

        job.startBuild().shouldSucceed().shouldContainsConsoleOutput("custom_origin\\s+" + repoUrl);
    }


    @Test
    public void checkout_local_branch() throws IOException, InterruptedException, SftpException, JSchException {
        buildGitRepo()
                .transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .localBranch("selenium_test_branch");
        job.addShellStep("test `git rev-parse selenium_test_branch` = `git rev-parse HEAD`");
        job.save();

        job.startBuild().shouldSucceed();
    }

    @Test
    public void checkout_to_local_dir() throws IOException, InterruptedException, SftpException, JSchException {
        buildGitRepo()
                .transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .localDir("local_dir");
        job.addShellStep("cd local_dir && test -f foo");
        job.save();

        job.startBuild().shouldSucceed();
    }

    @Test
    public void poll_for_changes() throws IOException, InterruptedException, SftpException, JSchException {
        buildGitRepo()
                .transferToDockerContainer(host, port);

        job.useScm(GitScm.class) //
                .url(container.getRepoUrl())
                .credentials(USERNAME);
        job.pollScm().schedule("* * * * *");
        job.addShellStep("test -f foo");
        job.save();

        elasticSleep(70000);

        // We should have some build after 70 seconds
        job.getLastBuild().shouldSucceed().shouldExist();
    }

    @Test
    public void check_revision() throws IOException, InterruptedException, SftpException, JSchException {
        buildGitRepo()
                .transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME);
        job.save();
        job.startBuild().waitUntilFinished();

        Build build = job.getLastBuild();
        String revision = getRevisionFromConsole(build.getConsole());

        build.openStatusPage();
        build.control(By.xpath("//*[contains(text(),'" + revision + "')]")).check();
    }

    @Test
    public void update_submodules_recursively() throws IOException, InterruptedException, JSchException, SftpException {
        String name = "submodule";
        buildGitRepo()
                .addSubmodule(name)
                .transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .enableRecursiveSubmoduleProcessing();

        job.addShellStep("cd " + name + " && test -f foo");
        job.save();
        job.startBuild().shouldSucceed();
    }

    @Test
    public void calculate_changelog() throws IOException, InterruptedException, SftpException, JSchException {
        final URL changesUrl;
        final String TEST_COMMIT_MESSAGE = "Second commit";
        Build b;
        GitRepo repo = buildGitRepo();

        repo.createBranch("testBranch");
        repo.changeAndCommitFoo(TEST_COMMIT_MESSAGE);
        repo.transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .calculateChangelog("origin", "testBranch");

        job.save();

        b = job.startBuild();
        b.shouldSucceed();
        changesUrl = b.url("changes");

        assertThat(
                b.getConsole(),
                Matchers.containsRegexp("Using 'Changelog to branch' strategy.", Pattern.MULTILINE)
        );

        assertThat(
                visit(changesUrl).getPageSource(),
                Matchers.containsRegexp(TEST_COMMIT_MESSAGE, Pattern.MULTILINE)
        );
    }

    @Test
    public void clean_after_checkout() throws IOException, InterruptedException {
        test_clean_while_checkout(false);
    }

    @Test
    public void clean_before_checkout() throws IOException, InterruptedException {
        test_clean_while_checkout(true);
    }

    @Test
    public void create_tag_for_build() throws IOException, InterruptedException, SftpException, JSchException {
        GitRepo repo = buildGitRepo();
        repo.transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .createTagForBuild();

        job.addShellStep("git tag -n1");
        job.save();
        Build b = job.startBuild();
        b.shouldSucceed();

        assertThat(
                b.getConsole(),
                Matchers.containsRegexp("jenkins-"+job.name +"-1 Jenkins Build #1", Pattern.MULTILINE)
        );

    }

    @Test
    public void custom_scm_name() throws IOException, InterruptedException, SftpException, JSchException {
        final String SCM_NAME = "halligalli";
        GitRepo repo = buildGitRepo();
        repo.transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .customScmName(SCM_NAME);

        job.save();
        Build b = job.startBuild();
        b.shouldSucceed();

        assertThat(
                visit(b.url("git")).getPageSource(),
                Matchers.containsRegexp("<b>SCM:</b> " + SCM_NAME, Pattern.MULTILINE)
        );
    }

    @Test
    public void sparse_checkout() throws IOException, InterruptedException {
        final String SUB_DIR = "testDir";
        final String TEST_FILE = "testFile.txt";

        GitRepo repo = buildGitRepo();
        repo.touch(SUB_DIR + "/" + TEST_FILE);
        repo.touch(TEST_FILE);
        repo.addFilesIn(getClass().getResource("."));
        repo.commit("Added two test files.");
        repo.transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .sparseCheckout().addPath(SUB_DIR);

        job.addShellStep("test ! -f " + TEST_FILE + " && test -f " + SUB_DIR + "/" + TEST_FILE);

        job.save();
        Build b = job.startBuild();
        b.shouldSucceed();
    }

    @Test
    public void ancestry_strategy_to_choose_build() throws IOException, InterruptedException {
        final String TEST_BRANCH = "testBranch";

        GitRepo repo = buildGitRepo();
        repo.createBranch(TEST_BRANCH);
        repo.changeAndCommitFoo("Commit1 on master");
        repo.checkout(TEST_BRANCH);
        repo.changeAndCommitFoo("commit1 on " + TEST_BRANCH);
        String sha1 = repo.getLastSha1();
        repo.changeAndCommitFoo("commit2 on " + TEST_BRANCH);

        repo.transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .branch("")
                .credentials(USERNAME)
                .chooseBuildStrategy("Ancestry", 1, sha1);

        job.save();
        Build b = job.startBuild();
        b.shouldSucceed();
        // TODO Multiple selected branches create multiple builds, these should also be verified

        assertThat(
                b.getConsole(),
                Matchers.containsRegexp(
                        "Checking out Revision .* \\(origin/"+TEST_BRANCH+"\\)",
                        Pattern.MULTILINE
                )
        );
    }

    @Test
    public void inverse_strategy_to_choose_build() throws IOException, InterruptedException {
        final String BRANCH_NAME = "secondBranch";

        GitRepo repo = buildGitRepo();
        repo.createBranch(BRANCH_NAME);
        repo.transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .chooseBuildStrategy("Inverse");

        job.save();
        Build b = job.startBuild();
        b.shouldSucceed();

        assertThat(
                b.getConsole(),
                Matchers.containsRegexp(
                        "Checking out Revision .* \\(origin/"+BRANCH_NAME+"\\)",
                        Pattern.MULTILINE
                )
        );
    }

    ////////////////////
    // HELPER METHODS //
    ////////////////////

    private String getRevisionFromConsole(String console) {
        Pattern p = Pattern.compile("(?<=\\bRevision\\s)(\\w+)");
        Matcher m = p.matcher(console);
        assertThat(m.find(), is(true));
        return m.group(0);
    }

    private GitRepo buildGitRepo() throws IOException, InterruptedException {
        GitRepo repo = new GitRepo();
        repo.changeAndCommitFoo("Initial commit");
        return repo;
    }

    /**
     * Invoked by {@link #clean_after_checkout()} and {@link #clean_before_checkout()}
     *
     * @param before Select "clean before" or "clean after"
     * @throws IOException
     * @throws InterruptedException
     */
    private void test_clean_while_checkout(boolean before) throws IOException, InterruptedException {
        GitRepo repo = buildGitRepo();
        repo.transferToDockerContainer(host, port);

        // configure and build to create untrackedFile.txt

        GitScm git = job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME);

        if (before) {
            git.cleanBeforeCheckout();
        } else {
            git.cleanAfterCheckout();
        }

        job.addShellStep("touch untrackedFile.txt");
        job.save();

        job.startBuild().shouldSucceed();

        // configure and build to test if file has been removed

        job.configure();
        job.removeFirstBuildStep();
        job.addShellStep("ls && test ! -f untrackedFile.txt");
        job.save();

        job.startBuild().shouldSucceed();
    }
}
