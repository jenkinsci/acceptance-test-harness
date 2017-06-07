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
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

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
}
