package plugins;

import com.google.inject.Inject;

import hudson.util.VersionNumber;

import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.SvnContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionCredentialUserPwd;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionPluginTestException;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionScm;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assume.assumeTrue;

/**
 * @author Matthias Karl
 */
@WithPlugins("subversion")
@Category(DockerTest.class)
@WithDocker
@Deprecated
public class Subversion_Version154_PluginTest extends AbstractJUnitTest {
    @Inject
    DockerContainerHolder<SvnContainer> svn;

    @Before
    public void setUp() {
        assumeTrue(jenkins.getPlugin("subversion").getVersion().compareTo(new VersionNumber("1.55"))<0);
    }

    @Test
    public void run_basic_subversion_build() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        f.useScm(SubversionScm.class).url.set(svnContainer.getUrlUnsaveRepo());
        f.addShellStep("test -d .svn");
        f.save();

        f.startBuild().shouldSucceed().shouldContainsConsoleOutput("test -d .svn");
    }

    @Test
    public void checkout_specific_revision() throws SubversionPluginTestException {
        final int revision = 0;
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        f.useScm(SubversionScm.class).url.set(svnContainer.getUrlUnsaveRepoAtRevision(revision));
        f.save();

        f.startBuild().shouldSucceed().shouldContainsConsoleOutput("At revision " + revision);
    }

    @Test
    public void always_checkout_fresh_copy() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();

        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUnsaveRepo());
        subversionScm.checkoutStrategy.select(SubversionScm.ALWAYS_FRESH_COPY);
        f.save();

        f.startBuild().shouldSucceed();

        f.startBuild().shouldSucceed()
                .shouldContainsConsoleOutput("Checking out " + svnContainer.getUrlUnsaveRepo());
    }

    @Test
    public void run_basic_subversion_build_userPwd() throws SubversionPluginTestException, InterruptedException {
        final SvnContainer svnContainer = svn.get();

        final FreeStyleJob f = jenkins.jobs.create();
        f.addShellStep("test -d .svn");
        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUserPwdSaveRepo());

        final SubversionCredentialUserPwd credentialPage = subversionScm.getCredentialPage(SubversionCredentialUserPwd.class);
        credentialPage.setUsername(SvnContainer.USER);
        credentialPage.setPassword(SvnContainer.PWD);
        credentialPage.confirmDialog();
        f.save();

        f.startBuild().shouldSucceed().shouldContainsConsoleOutput("test -d .svn");
    }

    @Test
    public void run_basic_subversion_build_svn_userPwd() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();

        final FreeStyleJob f = jenkins.jobs.create();
        f.addShellStep("test -d .svn");
        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getSvnUrl());

        final SubversionCredentialUserPwd credentialPage = subversionScm.getCredentialPage(SubversionCredentialUserPwd.class);
        credentialPage.setUsername(SvnContainer.USER);
        credentialPage.setPassword(SvnContainer.PWD);
        credentialPage.confirmDialog();
        f.save();

        f.startBuild().shouldSucceed().shouldContainsConsoleOutput("test -d .svn");
    }
}
