package plugins;

import com.google.inject.Inject;
import hudson.util.VersionNumber;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.SvnContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionCredentialUserPwd;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionPluginTestException;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionScm;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assume.assumeTrue;

/**
 * Feature: Subversion support
 * For plugin core version 1.54.
 * Obsolete when core version gets raised to later version than 1.54.
 * As a user
 * I want to be able to check out source code from Subversion
 *
 * @author Matthias Karl
 */
@WithPlugins("subversion")
@Native("docker")
@Deprecated
public class Subversion_Version154_PluginTest extends AbstractJUnitTest {
    @Inject
    DockerContainerHolder<SvnContainer> svn;

    @Before
    public void setUp() {
        assumeTrue(jenkins.getPlugin("subversion").getVersion().compareTo(new VersionNumber("1.55"))<0);
    }

    /**
     * Scenario: Run basic Subversion build
     * Given I have installed the "subversion" plugin
     * And a job
     * When I check out code from Subversion repository "UrlUnsaveRepo"
     * And I add a shell build step "test -d .svn"
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And console output should contain "test -d .svn"
     */
    @Test
    public void run_basic_subversion_build() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        f.useScm(SubversionScm.class).url.set(svnContainer.getUrlUnsaveRepo());
        f.addShellStep("test -d .svn");
        f.save();

        f.startBuild().shouldSucceed().shouldContainsConsoleOutput("test -d .svn");
    }

    /**
     * Scenario: Check out specified Subversion revision
     * Given I have installed the "subversion" plugin
     * And a job
     * When I check out code from Subversion repository "UnsaveRepoAtRevision 0
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And console output should contain "At revision 0"
     */
    @Test
    public void checkout_specific_revision() throws SubversionPluginTestException {
        final int revision = 0;
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        f.useScm(SubversionScm.class).url.set(svnContainer.getUrlUnsaveRepoAtRevision(revision));
        f.save();

        f.startBuild().shouldSucceed().shouldContainsConsoleOutput("At revision " + revision);
    }

    /**
     * Scenario: Always check out fresh copy
     * Given I have installed the "subversion" plugin
     * And a job
     * When I check out code from Subversion repository "UrlUnsaveRepo"
     * And I select "Always check out a fresh copy" as a "Check-out Strategy"
     * And I save the job
     * And I build 2 jobs
     * Then the build should succeed
     * And console output should contain "Checking out UrlUnsaveRepo"
     */
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

    /**
     * Scenario: http:// user/pwd basic Checkout
     * Given I have installed the "subversion" plugin
     * And a job
     * And I add a shell build step "test -d .svn"
     * When I check out code from protected Subversion repository "UrlUserPwdSaveRepo"
     * And I click the link to enter credentials
     * And I enter the right username and the right password
     * And I save the credentials
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And console output should contain "test -d .svn"
     */
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

    /**
     * Scenario:basic Checkout with svn protocol user/pwd
     * Given I have installed the "subversion" plugin
     * And a job
     * And I add a shell build step "test -d .svn"
     * When I check out code from protected Subversion repository "SvnUrl"
     * And I click the link to enter credentials
     * And I enter the right username and the right password
     * And I save the credentials
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And console output should contain "test -d .svn"
     */
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
