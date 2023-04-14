/*
 * The MIT License
 *
 * Copyright (c) 2023 CloudBees, Inc.
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
 *
 */

package plugins;

import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.GitListContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.FailureDiagnostics;
import org.jenkinsci.test.acceptance.junit.WithCredentials;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithOS;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.git.GitLabScm;
import org.jenkinsci.test.acceptance.plugins.git.GitRepo;
import org.jenkinsci.test.acceptance.plugins.git.GitScm;
import org.jenkinsci.test.acceptance.plugins.git.GitblitScm;
import org.jenkinsci.test.acceptance.plugins.git.PhabricatorScm;
import org.jenkinsci.test.acceptance.plugins.git.ViewgitScm;
import org.jenkinsci.test.acceptance.plugins.git_client.ssh_host_key_verification.NoVerificationStrategy;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.utils.RepositoryBrowserUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;
import java.net.URL;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

/**
 * Tests git plugin adding repository navigators.
 */
@WithDocker
@Category(DockerTest.class)
@WithPlugins("git")
@WithCredentials(credentialType = WithCredentials.SSH_USERNAME_PRIVATE_KEY, values = {"gitplugin", "/org/jenkinsci/test/acceptance/docker/fixtures/GitListContainer/unsafe"})
@WithOS(os = {WithOS.OS.LINUX, WithOS.OS.MAC}) // GitRepo does not work on windows https://github.com/jenkinsci/acceptance-test-harness/issues/745
public class GitPluginRepositoryBrowserTest extends AbstractJUnitTest {

    private final Logger LOGGER = Logger.getLogger(GitPluginTest.class.getName());

    private static final String USERNAME = "gitplugin";
    private static final String REPOSITORY_BROWSER = "gitlist";
    private static final String REPOSITORY_BROWSER_GIT_LAB = "gitlab";
    private static final String REPOSITORY_BROWSER_PHABRICATOR = "phabricator";
    private static final String REPOSITORY_BROWSER_VIEWGIT = "viewgit";
    private static final String REPOSITORY_BROWSER_GITBLIT = "gitblit";

    private static final String TEST_COMMIT_MESSAGE = "Second commit";
    private static final String TEST_BRANCH = "testBranch";
    private static final String REMOTE = "origin";

    private static final int ELASTIC_SLEEP = 1000;
    private static final int POLLING_TRIES = 30;

    @Inject
    DockerContainerHolder<GitListContainer> gitServer;

    private Job job;

    private GitListContainer container;
    private String repoUrl;
    private String urlRepositoryBrowser;
    private String hostSSH;
    private int portSSH;
    private String hostHTTP;
    private int portHTTP;
    @Inject
    private FailureDiagnostics diag;

    @Before
    public void init() {
        useNoVerificationSshHostKeyStrategy();
        container = gitServer.get();
        repoUrl = container.getRepoUrl();
        urlRepositoryBrowser = container.getUrlRepositoryBrowser();
        hostSSH = container.hostSSH();
        portSSH = container.portSSH();
        hostHTTP = container.hostHTTP();
        portHTTP = container.portHTTP();
        job = jenkins.jobs.create();
        job.configure();
        job.waitFor();
    }

    @After
    public void end() {
        //DEBUG Trials
        container.cp("/var/log/php8.1-fqm/log", diag.touch("php-fqm.log").getAbsolutePath());
    }

    public void useNoVerificationSshHostKeyStrategy() {
        GlobalSecurityConfig sc = new GlobalSecurityConfig(jenkins);
        sc.open();
        sc.useSshHostKeyVerificationStrategy(NoVerificationStrategy.class);
        sc.save();
    }

    @Test
    public void repository_browser_test() {
        URL changesUrl;
        Build b;
        buildGitRepo();

        GitScm scm = job.useScm(GitScm.class)
                .url(repoUrl)
                .credentials(USERNAME);
        newCalculateCangelog(scm, REMOTE, TEST_BRANCH);

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
                not(Matchers.containsRegexp(urlRepositoryBrowser, Pattern.MULTILINE))
        );

        // Configuring a repository browser
        job.configure();
        job.useScm(GitScm.class)
                .repositoryBrowser(REPOSITORY_BROWSER)
                .urlRepositoryBrowser(urlRepositoryBrowser);

        job.save();

        b = job.startBuild();
        b.shouldSucceed();
        changesUrl = b.url("changes");
        assertThat(
                b.getConsole(),
                Matchers.containsRegexp("Using 'Changelog to branch' strategy.", Pattern.MULTILINE)
        );

        String revision = getRevisionFromConsole(b.getConsole());
        RepositoryBrowserUtils rbUtils = new RepositoryBrowserUtils(driver, hostHTTP, portHTTP);
        String revisionUrl = rbUtils.getCommitUrl(revision, RepositoryBrowserUtils.RepositoryBrowserType.DEFAULT_BROWSER);
        assertThat(
                visit(changesUrl).getPageSource(),
                Matchers.containsRegexp(revisionUrl, Pattern.MULTILINE)
        );
        String pageSource = rbUtils.visit(revision).getPageSource();
        assertThat(
                pageSource,
                Matchers.containsRegexp("Commit "+revision, Pattern.MULTILINE)
        );
        assertThat(
                pageSource,
                Matchers.containsRegexp(TEST_COMMIT_MESSAGE, Pattern.MULTILINE)
        );
    }

    @Test
    public void gitlab_repository_browser_test() {
        buildGitRepo();

        configureGitScm(GitLabScm.class, REPOSITORY_BROWSER_GIT_LAB).gitlabVersion("2.0");

        job.save();

        assertUrlCommit(RepositoryBrowserUtils.RepositoryBrowserType.GITLAB);
    }

    @Test
    public void phabricator_repository_browser_test() {
        buildGitRepo();

        configureGitScm(PhabricatorScm.class, REPOSITORY_BROWSER_PHABRICATOR).phabricatorRepo(GitListContainer.REPO_NAME);

        job.save();

        assertUrlCommit(RepositoryBrowserUtils.RepositoryBrowserType.PHABRICATOR);
    }

    @Test
    public void viewgit_repository_browser_test() {
        buildGitRepo();

        configureGitScm(ViewgitScm.class, REPOSITORY_BROWSER_VIEWGIT).projectName(GitListContainer.REPO_NAME);

        job.save();

        assertUrlCommit(RepositoryBrowserUtils.RepositoryBrowserType.VIEWGIT);
    }

    @Test
    public void gitblit_repository_browser_test() {
        buildGitRepo();

        configureGitScm(GitblitScm.class, REPOSITORY_BROWSER_GITBLIT).projectName(GitListContainer.REPO_NAME);

        job.save();

        assertUrlCommit(RepositoryBrowserUtils.RepositoryBrowserType.GITBLIT);
    }

    ////////////////////
    // HELPER METHODS //
    ////////////////////

    private void buildGitRepo() {
        GitRepo repo = new GitRepo();
        repo.changeAndCommitFoo("Initial commit");
        repo.createBranch(TEST_BRANCH);
        repo.changeAndCommitFoo(TEST_COMMIT_MESSAGE);
        repo.transferToDockerContainer(hostSSH, portSSH);
    }

    private String getRevisionFromConsole(String console) {
        Pattern p = Pattern.compile("(?<=\\bRevision\\s)(\\w+)");
        Matcher m = p.matcher(console);
        assertThat(m.find(), is(true));
        return m.group(0);
    }

    private void assertUrlCommit(RepositoryBrowserUtils.RepositoryBrowserType repositoryBrowserType) {
        Build b = job.startBuild();
        b.shouldSucceed();

        URL changesUrl = b.url("changes");
        String revision = getRevisionFromConsole(b.getConsole());
        RepositoryBrowserUtils rbUtils = new RepositoryBrowserUtils(driver, hostHTTP, portHTTP);
        String revisionUrl = rbUtils.getCommitUrl(revision, repositoryBrowserType);
        assertThat(
                visit(changesUrl).getPageSource(),
                Matchers.containsRegexp(revisionUrl, Pattern.MULTILINE)
        );
    }

    private <T extends GitScm> T configureGitScm(Class<T> scmClass, String repositoryBrowser) {
        T scm = (T) job.useScm(scmClass).url(repoUrl).credentials(USERNAME).repositoryBrowser(repositoryBrowser).urlRepositoryBrowser(urlRepositoryBrowser);

        return newCalculateCangelog(scm, REMOTE, TEST_BRANCH);
    }

    private <T extends GitScm> T newCalculateCangelog(T scm, String remote, String branch) {
        Control behaviourButton = scm.control("hetero-list-add[extensions]");
        waitFor(behaviourButton);
        javascriptLoadClick(behaviourButton.resolve(), POLLING_TRIES);
        FinderDropDownMenuItem findDropDownMenuItem = new FinderDropDownMenuItem(behaviourButton);
        WebElement caption = findDropDownMenuItem.find("Calculate changelog against a specific branch");
        scm.waitFor(caption);
        javascriptLoadClick(caption, POLLING_TRIES);

        scm.control("/extensions/options/compareRemote").set(remote);
        scm.control("/extensions/options/compareTarget").set(branch);

        return scm;
    }

    // polling while yui javascript library is loaded
    private void javascriptLoadClick(WebElement element, int tries) {
        try {
            element.click();
            elasticSleep(ELASTIC_SLEEP);
            LOGGER.info(String.format("%d tries were necessary to click element", (POLLING_TRIES - tries + 1)));
        } catch (WebDriverException e) {
            if (tries == 1 || !e.getMessage().contains("Element is not clickable")) {
                throw e;
            } else {
                javascriptLoadClick(element, tries--);
            }
        }
    }

    private class FinderDropDownMenuItem extends CapybaraPortingLayerImpl.Finder<WebElement> {
        private Control button;

        protected FinderDropDownMenuItem(Control button) {
            this.button = button;
        }

        @Override
        protected WebElement find(String caption) {
            // With enough implementations registered the one we are looking for might
            // require scrolling in menu to become visible. This dirty hack stretch
            // yui menu so that all the items are visible.
            executeScript("" +
                    "YAHOO.util.Dom.batch(" +
                    "    document.querySelector('.yui-menu-body-scrolled')," +
                    "    function (el) {" +
                    "        el.style.height = 'auto';" +
                    "        YAHOO.util.Dom.removeClass(el, 'yui-menu-body-scrolled');" +
                    "    }" +
                    ");"
            );

            return button.find(by.link(caption));
        }
    };
}
