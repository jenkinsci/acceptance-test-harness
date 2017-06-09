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
package plugins.no_docker;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.git.GitScm;
import org.jenkinsci.test.acceptance.plugins.git_client.JGitInstallation;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Job;
import org.junit.Test;

// TODO: remove once docker based tests are run regularly
@WithPlugins("git")
public class GitPluginNoDockerTest extends AbstractJUnitTest {

    private static final String REPO_URL = "git://github.com/jenkinsci/git-plugin.git";
    private enum GIT_IMPL {GIT, JGIT};

    @Test
    public void simple_checkout_git() {
        assertExecutesFine(generateSimpleJob(GIT_IMPL.GIT), "test -f pom.xml");
    }

    @Test
    public void simple_checkout_jgit() {
        assertExecutesFine(generateSimpleJob(GIT_IMPL.JGIT), "test -f pom.xml");
    }

    @Test
    public void checkout_branch_git() {
        assertExecutesFine(generateJobWithBranch(GIT_IMPL.GIT), "test `git rev-parse origin/recovery` = `git rev-parse HEAD`");
    }

    @Test
    public void checkout_branch_jgit() {
        assertExecutesFine(generateJobWithBranch(GIT_IMPL.JGIT), "test `git rev-parse origin/recovery` = `git rev-parse HEAD`");
    }

    @Test
    public void name_remote_repo_git() {
        assertExecutesFine(generateJobWithRemoteName(GIT_IMPL.GIT), "test -f pom.xml && git remote -v").shouldContainsConsoleOutput("custom_origin\\s+" + REPO_URL);
    }

    @Test
    public void name_remote_repo_jgit() {
        assertExecutesFine(generateJobWithRemoteName(GIT_IMPL.JGIT), "test -f pom.xml && git remote -v").shouldContainsConsoleOutput("custom_origin\\s+" + REPO_URL);
    }

    @Test
    public void checkout_local_branch_git() {
        assertExecutesFine(generateJobWithLocalBranch(GIT_IMPL.GIT), "test `git rev-parse selenium_test_branch` = `git rev-parse HEAD`");
    }

    @Test
    public void checkout_local_branch_jgit() {
        assertExecutesFine(generateJobWithLocalBranch(GIT_IMPL.JGIT), "test `git rev-parse selenium_test_branch` = `git rev-parse HEAD`");
    }

    @Test
    public void checkout_to_local_dir_git() {
        assertExecutesFine(generateJobWithLocalDir(GIT_IMPL.GIT), "cd local_dir && test -f pom.xml");
    }

    @Test
    public void checkout_to_local_dir_jgit() {
        assertExecutesFine(generateJobWithLocalDir(GIT_IMPL.JGIT), "cd local_dir && test -f pom.xml");
    }

    @Test
    public void poll_for_changes_git() {
        assertPoolingWorks(generateSimpleJob(GIT_IMPL.GIT));
    }

    @Test
    public void poll_for_changes_jgit() {
        assertPoolingWorks(generateSimpleJob(GIT_IMPL.JGIT));
    }

    private Job generateJob(GIT_IMPL type) {
        if (type.equals(GIT_IMPL.JGIT)) {
            JGitInstallation.addJGit(jenkins);
            find(by.button("Save")).click();
        }
        Job job = jenkins.jobs.create();
        return job;
    }

    private GitScm generateSCM(Job job) {
        return job.useScm(GitScm.class).url(REPO_URL);
    }

    private void useJGitIfNeccesary(GIT_IMPL type, GitScm scm) {
        if (type.equals(GIT_IMPL.JGIT)) {
            scm.tool("jgit");
        }
    }

    private Job generateSimpleJob(GIT_IMPL type) {
        Job job = generateJob(type);
        GitScm scm = generateSCM(job);
        useJGitIfNeccesary(type, scm);
        return job;
    }

    private Job generateJobWithBranch(GIT_IMPL type) {
        Job job = generateJob(type);
        GitScm scm = generateSCM(job).branch("recovery");
        useJGitIfNeccesary(type, scm);
        return job;
    }

    private Job generateJobWithRemoteName(GIT_IMPL type) {
        Job job = generateJob(type);
        GitScm scm = generateSCM(job).remoteName("custom_origin");
        useJGitIfNeccesary(type, scm);
        return job;
    }

    private Job generateJobWithLocalBranch(GIT_IMPL type) {
        Job job = generateJob(type);
        GitScm scm = generateSCM(job).localBranch("selenium_test_branch");
        useJGitIfNeccesary(type, scm);
        return job;
    }

    private Job generateJobWithLocalDir(GIT_IMPL type) {
        Job job = generateJob(type);
        GitScm scm = generateSCM(job).localDir("local_dir");
        useJGitIfNeccesary(type, scm);
        return job;
    }

    private Build assertExecutesFine(Job job, String shellStep) {
        job.addShellStep(shellStep);
        job.save();

        return job.startBuild().shouldSucceed();
    }

    private void assertPoolingWorks(Job job) {
        job.pollScm().schedule("* * * * *");
        job.addShellStep("test -f pom.xml");
        job.save();

        elasticSleep(70000);

        // We should have some build after 70 seconds
        job.getLastBuild().shouldSucceed().shouldExist();
    }
}
