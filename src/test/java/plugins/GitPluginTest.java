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

import java.net.URL;

import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.GitContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.git.GitScm;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.po.Job;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

@Native("docker")
@WithPlugins("git")
public class GitPluginTest extends AbstractJUnitTest {

    private static final String USERNAME = "gitplugin";

    @Inject
    DockerContainerHolder<GitContainer> gitServer;

    private Job job;

    private GitContainer container;
    private String repoUrl;

    @Before
    public void init() {
        container = gitServer.get();
        repoUrl = container.getRepoUrl();
        addCredentials();
        job = jenkins.jobs.create();
        job.configure();
    }

    @Test
    public void simple_checkout() {
        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME);
        job.addShellStep("test -f pom.xml");
        job.save();

        job.startBuild().shouldSucceed();
    }

    @Test
    public void checkout_branch() {
        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .branch.set("svn");
        job.addShellStep("test `git rev-parse origin/svn` = `git rev-parse HEAD`");
        job.save();

        job.startBuild().shouldSucceed();
    }

    @Test
    public void name_remote_repo() {
        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .remoteName("custom_origin");
        job.addShellStep("test -f pom.xml && git remote -v");
        job.save();

        job.startBuild().shouldSucceed().shouldContainsConsoleOutput("custom_origin\\s+" + repoUrl);
    }

    @Test
    public void checkout_local_branch() {
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
        job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME)
                .localDir("local_dir");
        job.addShellStep("cd local_dir && test -f pom.xml");
        job.save();

        job.startBuild().shouldSucceed();
    }

    @Test
    public void poll_for_changes() {
        job.useScm(GitScm.class) //
                .url(container.getRepoUrl())
                .credentials(USERNAME);
        job.pollScm().schedule("* * * * *");
        job.addShellStep("test -f pom.xml");
        job.save();

        sleep(70000);

        // We should have some build after 70 seconds
        job.getLastBuild().shouldSucceed().shouldExist();
    }

    private void addCredentials() {
        ManagedCredentials c = new ManagedCredentials(jenkins);
        c.open();
        SshPrivateKeyCredential sc = c.add(SshPrivateKeyCredential.class);
        sc.username.set(USERNAME);
        sc.selectEnterDirectly().privateKey.set(resource("/ssh_keys/unsafe").asText());
        c.save();
    }
}
