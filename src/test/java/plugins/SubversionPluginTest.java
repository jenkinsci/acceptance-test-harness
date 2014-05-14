package plugins;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.SvnContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionCredentialUserPwd;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionPluginTestException;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionScm;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

/**
 * Feature: Subversion support
 * As a user
 * I want to be able to check out source code from Subversion
 *
 * @author Matthias Karl
 */
@WithPlugins("subversion@1.54")
@Native("docker")
public class SubversionPluginTest extends AbstractJUnitTest {
    @Inject
    DockerContainerHolder<SvnContainer> svn;

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
        f.configure();
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
        f.configure();
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
        f.configure();

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
     * When I check out code from protected Subversion repository "UrlUserPwdSaveRepo"
     * And I click the link to enter credentials
     * And I enter the right username and the right password
     * And I save the credentials
     * And I save the job
     * And I build the job
     * Then the build should succeed
     */
    @Test
    public void run_basic_subversion_build_userPwd() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();

        final FreeStyleJob f = jenkins.jobs.create();
        f.configure();

        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUserPwdSaveRepo());

        final SubversionCredentialUserPwd credentialPage = subversionScm.getCredentialPage(SubversionCredentialUserPwd.class);
        credentialPage.setUsername(SvnContainer.USER);
        credentialPage.setPassword(SvnContainer.PWD);
        credentialPage.confirmDialog();
        f.save();

        f.startBuild().shouldSucceed();

    }

    /**
     * Scenario:basic Checkout with svn protocol
     * Given I have installed the "subversion" plugin
     * And a job
     * When I check out code from protected Subversion repository "SvnUrl"
     * And I click the link to enter credentials
     * And I enter the right username and the right password
     * And I save the credentials
     * And I save the job
     * And I build the job
     * Then the build should succeed
     */
    @Test
    public void run_basic_subversion_build_svn_userPwd() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();

        final FreeStyleJob f = jenkins.jobs.create();
        f.configure();

        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getSvnUrl());

        final SubversionCredentialUserPwd credentialPage = subversionScm.getCredentialPage(SubversionCredentialUserPwd.class);
        credentialPage.setUsername(SvnContainer.USER);
        credentialPage.setPassword(SvnContainer.PWD);
        credentialPage.confirmDialog();
        f.save();

        f.startBuild().shouldSucceed();

    }


    /**
     * Test not running atm. Plugin does not seem to work with credentials plugin
     * @throws SubversionPluginTestException
     */
    /*
    @Test
    public void run_basic_subversion_build_userPwd_credentials_already_added() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();

        //preconfigure credentials
        final ManagedCredentials c = new ManagedCredentials(jenkins);
        c.open();
        final UserPwdCredential upc = c.add(UserPwdCredential.class);
        upc.username.set(SvnContainer.USER);
        upc.password.set(SvnContainer.PWD);
        c.save();
        jenkins.visit("credentials");

        //add Job
        final FreeStyleJob f = jenkins.jobs.create();
        f.configure();

        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUserPwdSaveRepo());


        f.save();

        f.startBuild().shouldSucceed();

    }
    */
}
