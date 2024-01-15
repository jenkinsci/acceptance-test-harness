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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import jakarta.inject.Inject;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.GitContainer;
import org.jenkinsci.test.acceptance.junit.*;
import org.jenkinsci.test.acceptance.plugins.git.GitRepo;
import org.jenkinsci.test.acceptance.plugins.git.GitScm;
import org.jenkinsci.test.acceptance.plugins.git_client.ssh_host_key_verification.NoVerificationStrategy;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.Job;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;

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

        useNoVerificationSshHostKeyStrategy();

        job = jenkins.jobs.create();
        job.configure();
    }

    private void useNoVerificationSshHostKeyStrategy() {
        GlobalSecurityConfig sc = new GlobalSecurityConfig(jenkins);
        sc.open();
        sc.useSshHostKeyVerificationStrategy(NoVerificationStrategy.class);
        sc.save();
    }

    @Test
    public void simple_checkout() {
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
    public void checkout_branch() {
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
    public void name_remote_repo() {
        buildGitRepo()
                .transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .remoteName("custom_origin");
        job.addShellStep("test -f foo && git remote -v");
        job.save();

        job.startBuild().shouldSucceed().shouldContainsConsoleOutput("custom_origin\\s+" + 
                repoUrl.replace("[", "\\[").replace("]", "\\]"));
    }   
    
    @Test
    public void checkout_local_branch() {
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
    public void checkout_to_local_dir() {
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
    public void poll_for_changes() {
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
    public void check_revision() {
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
    @Ignore("Fails on CI for unknown reasons")
    public void update_submodules_recursively() {
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
    public void calculate_changelog() {
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
    public void clean_after_checkout() {
        test_clean_while_checkout(false);
    }

    @Test
    public void clean_before_checkout() {
        test_clean_while_checkout(true);
    }

    @Test
    public void create_tag_for_build() {
        GitRepo repo = buildGitRepo();
        repo.transferToDockerContainer(host, port);
        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .customNameAndMail("fake", "fake@mail.com")
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
    public void custom_scm_name() {
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

        // Git plugin 4.0 switched from <b> to <strong>. Accept either bold or strong.
        assertThat(
                visit(b.url("git")).getPageSource(),
                Matchers.containsRegexp("<[^>]+>SCM:?</[^>]+>:? " + SCM_NAME, Pattern.MULTILINE)
        );
    }

    @Test
    public void sparse_checkout() {
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
    public void inverse_strategy_to_choose_build() {
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


    @Test
    public void merge_before_build_test() {
        final String TEST_COMMIT_MESSAGE = "Branch test";
        final String BRANCH_NAME = "testBranch";
        final String TEST_FILE = "foo_test_branch";

        Build b;
        GitRepo repo = buildGitRepo();

        repo.createBranch(BRANCH_NAME);
        repo.touch(TEST_FILE);
        repo.git("add", TEST_FILE);
        repo.git("commit", "-m", TEST_COMMIT_MESSAGE);

        repo.transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .mergeBeforeBuild()
                .setTxtMergeRemote("origin")
                .setTxtMergeTarget(BRANCH_NAME);

        job.save();

        b = job.startBuild();
        b.shouldSucceed();

        String console = b.getConsole();
        String revision = getRevisionFromConsole(console);

        assertThat(
                console,
                Matchers.containsRegexp(
                        "Merging Revision .* \\(refs/remotes/origin/master\\) to origin/"+BRANCH_NAME,
                        Pattern.MULTILINE
                )
        );
        assertThat(
                console,
                Matchers.containsRegexp("git merge --ff "+revision, Pattern.MULTILINE)
        );
        assertThat(
                console,
                Matchers.containsRegexp(
                        "Checking out Revision .* \\(origin/master, origin/"+BRANCH_NAME+"\\)", Pattern.MULTILINE
                )
        );
    }

    @Test
    public void commit_author_in_changelog_test() {
        URL changesUrl;
        final String TEST_COMMIT_MESSAGE = "Second commit";
        final String BRANCH_NAME = "testBranch";
        final String TEST_FILE = "foo_test_branch";

        Build b;
        GitRepo repo = buildGitRepo();

        repo.createBranch(BRANCH_NAME);
        repo.touch(TEST_FILE);
        repo.git("add", TEST_FILE);
        repo.git("commit", "-m", TEST_COMMIT_MESSAGE, "--author", "New-Author <other-email@example.org>");
        repo.transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .calculateChangelog("origin", BRANCH_NAME);
        job.save();

        b = job.startBuild();
        b.shouldSucceed();
        changesUrl = b.url("changes");

        assertThat(
                visit(changesUrl).getPageSource(),
                Matchers.containsRegexp("/user/jenkins-ath/", Pattern.MULTILINE)
        );

        job.configure();
        job.useScm(GitScm.class).commitAuthorInChangelog();

        job.save();

        b = job.startBuild();
        b.shouldSucceed();
        changesUrl = b.url("changes");

        assertThat(
                visit(changesUrl).getPageSource(),
                Matchers.containsRegexp("/user/other-email/", Pattern.MULTILINE)
        );
    }

    @Test
    public void advanced_clone_test() {
        final String TEST_INITIAL_COMMIT = "Initial commit";
        final String TEST_COMMIT1_MESSAGE = "First commit";
        final String TEST_COMMIT2_MESSAGE = "Second commit";
        Build b;

        GitRepo repo = buildGitRepo();
        repo.changeAndCommitFoo(TEST_COMMIT1_MESSAGE);
        repo.changeAndCommitFoo(TEST_COMMIT2_MESSAGE);
        repo.transferToDockerContainer(host, port);

        GitScm gitScm = job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME);
        job.addShellStep("git log");
        job.save();

        b = job.startBuild();
        b.shouldSucceed();
        String console = b.getConsole();
        assertThat(
                console,
                Matchers.containsRegexp(TEST_INITIAL_COMMIT, Pattern.MULTILINE)
        );
        assertThat(
                console,
                Matchers.containsRegexp(TEST_COMMIT1_MESSAGE, Pattern.MULTILINE)
        );
        assertThat(
                console,
                Matchers.containsRegexp(TEST_COMMIT2_MESSAGE, Pattern.MULTILINE)
        );

        job.configure();
        GitScm.AdvancedClone behaviour = gitScm.advancedClone()
                .checkShallowClone(true)
                .setNumDepth("2");
        job.save();

        b = job.startBuild();
        b.shouldSucceed();
        console = b.getConsole();
        assertThat(
                console,
                not(Matchers.containsRegexp(TEST_INITIAL_COMMIT, Pattern.MULTILINE))
        );
        assertThat(
                console,
                Matchers.containsRegexp(TEST_COMMIT1_MESSAGE, Pattern.MULTILINE)
        );
        assertThat(
                console,
                Matchers.containsRegexp(TEST_COMMIT2_MESSAGE, Pattern.MULTILINE)
        );

        job.configure();
        behaviour.setNumDepth("1");
        job.save();

        b = job.startBuild();
        b.shouldSucceed();
        console = b.getConsole();
        assertThat(
                console,
                not(Matchers.containsRegexp(TEST_INITIAL_COMMIT, Pattern.MULTILINE))
        );
        assertThat(
                console,
                not(Matchers.containsRegexp(TEST_COMMIT1_MESSAGE, Pattern.MULTILINE))
        );
        assertThat(
                console,
                Matchers.containsRegexp(TEST_COMMIT2_MESSAGE, Pattern.MULTILINE)
        );
    }

    @Test
    public void advanced_checkout_test() {
        final String TEST_COMMIT_MESSAGE = "First commit";
        final String TEST_TIME_OUT = "2";
        Build b;

        GitRepo repo = buildGitRepo();
        repo.changeAndCommitFoo(TEST_COMMIT_MESSAGE);
        repo.transferToDockerContainer(host, port);

        GitScm gitScm = job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME);
        job.save();

        b = job.startBuild();
        b.shouldSucceed();
        String console = b.getConsole();
        String revision = getRevisionFromConsole(console);
        assertThat(
                console,
                not(Matchers.containsRegexp("git checkout -f "+revision+" # timeout="+TEST_TIME_OUT, Pattern.MULTILINE))
        );

        job.configure();
        gitScm.advancedCheckout().setTimeOut(TEST_TIME_OUT);
        job.save();

        b = job.startBuild();
        b.shouldSucceed();
        console = b.getConsole();
        assertThat(
                console,
                Matchers.containsRegexp("git checkout -f "+revision+" # timeout="+TEST_TIME_OUT, Pattern.MULTILINE)
        );
    }


    @Test
    public void default_strategy_to_choose_build() {
        final String BRANCH_NAME = "secondBranch";

        GitRepo repo = buildGitRepo();
        repo.createBranch(BRANCH_NAME);
        repo.transferToDockerContainer(host, port);

        // Default Strategy for master
        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .chooseBuildStrategy("Default");

        job.save();
        Build b = job.startBuild();
        b.shouldSucceed();

        assertThat(
                b.getConsole(),
                Matchers.containsRegexp(
                        "Checking out Revision .* \\(refs/remotes/origin/master\\)",
                        Pattern.MULTILINE
                )
        );

        // Default Strategy for secondBranch
        job.configure();
        job.useScm(GitScm.class)
                .branch(BRANCH_NAME);

        job.save();
        b = job.startBuild();
        b.shouldSucceed();

        assertThat(
                b.getConsole(),
                Matchers.containsRegexp(
                        "Checking out Revision .* \\(origin/"+BRANCH_NAME+"\\)",
                        Pattern.MULTILINE
                )
        );

        // Default Strategy for any branch
        job.configure();
        job.useScm(GitScm.class)
                .branch("");

        job.save();
        b = job.startBuild();
        b.shouldSucceed();

        // 2 branches discovered
        assertThat(
                b.getConsole(),
                Matchers.containsRegexp(
                        "Checking out Revision .* \\(origin/.*, origin/.*\\)",
                        Pattern.MULTILINE
                )
        );
    }

    @Test
    public void custom_name_and_email() {
        final String USER_NAME = "fake";
        final String EMAIL = "fake@mail.net";
        GitRepo repo = buildGitRepo();
        repo.transferToDockerContainer(host, port);

        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .customNameAndMail(USER_NAME, EMAIL);
        job.addShellStep("touch test.txt &&\n" +
                "git add test.txt &&\n" +
                "git commit -m \"Next commit\" &&\n" +
                "git show");

        job.save();
        Build b = job.startBuild();
        b.shouldSucceed();

        String console = b.getConsole();
        assertThat(
                console,
                Matchers.containsRegexp(USER_NAME, Pattern.MULTILINE)
        );
        assertThat(
                console,
                Matchers.containsRegexp(EMAIL, Pattern.MULTILINE)
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

    private GitRepo buildGitRepo() {
        GitRepo repo = new GitRepo();
        repo.changeAndCommitFoo("Initial commit");
        return repo;
    }

    /**
     * Invoked by {@link #clean_after_checkout()} and {@link #clean_before_checkout()}
     *
     * @param before Select "clean before" or "clean after"
     */
    private void test_clean_while_checkout(boolean before) {
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
