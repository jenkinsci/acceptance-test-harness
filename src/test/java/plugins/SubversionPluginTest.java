package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.inject.Inject;
import org.hamcrest.Matchers;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.SvnContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.WithCredentials;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionPluginTestException;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionScm;
import org.jenkinsci.test.acceptance.plugins.subversion.SvnRepositoryBrowserWebSvn;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Changes;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.junit.Test;
import org.junit.experimental.categories.Category;


/**
 * @author Matthias Karl
 */
@WithPlugins("subversion")
@Category(DockerTest.class)
@WithDocker
public class SubversionPluginTest extends AbstractJUnitTest {
    @Inject
    DockerContainerHolder<SvnContainer> svn;

    @Test
    public void run_basic_subversion_build() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        f.useScm(SubversionScm.class).url.set(svnContainer.getUrlUnauthenticatedRepo());
        f.addShellStep("test -d .svn");
        f.save();

        Build b = f.startBuild().shouldSucceed();
        assertThat(b.getConsole(), Matchers.containsString("test -d .svn"));
    }

    @Test
    public void checkout_specific_revision() throws SubversionPluginTestException {
        final int revision = 0;
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        f.useScm(SubversionScm.class).url.set(svnContainer.getUrlUnauthenticatedRepoAtRevision(revision));
        f.save();

        Build b = f.startBuild().shouldSucceed();
        assertThat(b.getConsole(), Matchers.containsString("At revision " + revision));
    }

    @Test
    public void always_checkout_fresh_copy() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();

        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUnauthenticatedRepo());
        subversionScm.checkoutStrategy.select(SubversionScm.ALWAYS_FRESH_COPY);
        f.save();

        f.startBuild().shouldSucceed();

        Build b = f.startBuild().shouldSucceed();
        assertThat(b.getConsole(), Matchers.containsString("Checking out " + svnContainer.getUrlUnauthenticatedRepo()));
    }

    @Test
    @WithCredentials(credentialType = WithCredentials.USERNAME_PASSWORD, values = {SvnContainer.USER, SvnContainer.PWD}, id = SvnContainer.USER)
    public void run_basic_subversion_build_userPwd() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();

        final FreeStyleJob f = jenkins.jobs.create();
        f.addShellStep("test -d .svn");

        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlAuthenticatedRepo());
        subversionScm.credentials.select(SvnContainer.USER);
        f.save();

        Build b = f.startBuild().shouldSucceed();
        assertThat(b.getConsole(), Matchers.containsString("test -d .svn"));
    }

    @Test
    @WithCredentials(credentialType = WithCredentials.USERNAME_PASSWORD, values = {SvnContainer.USER, SvnContainer.PWD}, id = SvnContainer.USER)
    public void run_basic_subversion_build_svn_userPwd() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();

        final FreeStyleJob f = jenkins.jobs.create();
        f.addShellStep("test -d .svn");
        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getSvnUrl());
        subversionScm.credentials.select(SvnContainer.USER);
        f.save();

        Build b = f.startBuild().shouldSucceed();
        assertThat(b.getConsole(), Matchers.containsString("test -d .svn"));
    }

    @Test
    public void clean_checkout() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUnauthenticatedRepo());
        subversionScm.checkoutStrategy.select(SubversionScm.CLEAN_CHECKOUT);

        f.addShellStep("echo test > unversioned.txt");
        f.save();
        f.startBuild().shouldSucceed();

        f.configure();
        f.removeFirstBuildStep();
        f.addShellStep("test ! -f unversioned.txt");
        f.save();
        f.startBuild().shouldSucceed();
    }

    @Test
    public void build_has_changes() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUnauthenticatedRepoAtRevision(1));

        f.save();
        f.startBuild().shouldSucceed();

        f.configure();
        subversionScm.url.set(svnContainer.getUrlUnauthenticatedRepoAtRevision(2));
        f.save();
        f.startBuild().shouldSucceed();
        final Changes changes = f.getLastBuild().getChanges();
        assertTrue("Build has no changes.", changes.hasChanges());
    }

    @Test
    public void build_has_no_changes() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUnauthenticatedRepo());

        f.save();
        f.startBuild();
        final Changes changes = f.getLastBuild().getChanges();
        assertFalse("Build has changes.", changes.hasChanges());
    }

    @Test
    public void build_has_changes_and_repoBrowser() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUnauthenticatedRepoAtRevision(1));
        final SvnRepositoryBrowserWebSvn repositoryBrowserWebSvn = subversionScm.useRepositoryBrowser(SvnRepositoryBrowserWebSvn.class);
        repositoryBrowserWebSvn.url.set(svnContainer.getUrlViewVC());
        f.save();
        f.startBuild().shouldSucceed();

        f.configure();
        subversionScm.url.set(svnContainer.getUrlUnauthenticatedRepoAtRevision(2));
        f.save();
        f.startBuild().shouldSucceed();
        final Changes changes = f.getLastBuild().getChanges();
        assertTrue("Build has no diff link.", changes.hasDiffFileLink("testOne.txt"));
    }

    @Test
    public void poll_for_changes() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();

        JenkinsConfig jc = new JenkinsConfig(jenkins);
        jc.configure();
        jc.setQuietPeriod(0);
        jc.save();

        final FreeStyleJob f = jenkins.jobs.create();
        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUnauthenticatedRepoAtRevision(1));
        f.save();
        f.startBuild().shouldSucceed();

        f.configure();
        subversionScm.url.set(svnContainer.getUrlUnauthenticatedRepoAtRevision(2));
        f.pollScm().schedule("* * * * *");
        f.addShellStep("test -d .svn");
        f.save();

        elasticSleep(10000);

        f.build(1).waitUntilFinished().shouldSucceed();
    }
}
