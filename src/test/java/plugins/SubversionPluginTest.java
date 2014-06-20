package plugins;

import com.google.inject.Inject;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.SvnContainer;
import org.jenkinsci.test.acceptance.junit.*;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionPluginTestException;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionScm;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionSvmAdvanced;
import org.jenkinsci.test.acceptance.plugins.subversion.SvnRepositoryBrowserWebSvn;
import org.jenkinsci.test.acceptance.po.Changes;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

/**
 * Feature: Subversion support
 * As a user
 * I want to be able to check out source code from Subversion
 *
 * @author Matthias Karl
 */
@WithPlugins("subversion@2.3")
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
     * Scenario:basic Checkout with svn protocol
     * Given I have installed the "subversion" plugin
     * And I have added the right username and password for svn as credentials
     * And a job
     * And I add a shell build step "test -d .svn"
     * When I check out code from protected Subversion repository "SvnUrl"
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And console output should contain "test -d .svn"
     */
    @Test
    @WithCredentials(credentialType = WithCredentials.USERNAME_PASSWORD, values = {SvnContainer.USER, SvnContainer.PWD})
    public void run_basic_subversion_build_userPwd() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();

        final FreeStyleJob f = jenkins.jobs.create();
        f.addShellStep("test -d .svn");

        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUserPwdSaveRepo());
        subversionScm.credentials.select(SvnContainer.USER);
        f.save();

        f.startBuild().shouldSucceed().shouldContainsConsoleOutput("test -d .svn");
    }


    /**
     * Scenario:basic Checkout with svn protocol
     * Given I have installed the "subversion" plugin
     * And I have added the right username and password for svn as credentials
     * And a job
     * And I add a shell build step "test -d .svn"
     * When I check out code from protected Subversion repository "SvnUrl"
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And console output should contain "test -d .svn"
     */
    @Test
    @WithCredentials(credentialType = WithCredentials.USERNAME_PASSWORD, values = {SvnContainer.USER, SvnContainer.PWD})
    public void run_basic_subversion_build_svn_userPwd() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();

        final FreeStyleJob f = jenkins.jobs.create();
        f.addShellStep("test -d .svn");
        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getSvnUrl());
        subversionScm.credentials.select(SvnContainer.USER);
        f.save();

        f.startBuild().shouldSucceed().shouldContainsConsoleOutput("test -d .svn");
    }


    /**
     * Scenario: clean check out
     * Given I have installed the "subversion" plugin
     * And a job
     * When I check out code from Subversion repository "UrlUnsaveRepo"
     * And I select a Emulated clean checkout
     * And I add a shell step to generate an unversioned file
     * And I save the job
     * And I build the job
     * And I remove the shell step
     * And I add a shell step to check if the unversioned file is present
     * And I build the job again
     * Then the build should succeed
     */
    @Test
    public void clean_checkout() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUnsaveRepo());
        subversionScm.checkoutStrategy.select(SubversionScm.CLEAN_CHECKOUT);

        f.addShellStep("echo test > unversioned.txt");
        f.save();
        f.startBuild().shouldSucceed();

        f.configure();
        f.removeFirstBuildStep();
        f.addShellStep("! test -f unversioned.txt");
        f.save();
        f.startBuild().shouldSucceed();
    }

    /**
     * Scenario: Build has changes
     * Given I have installed the "subversion" plugin
     * And a job
     * When I check out code from Subversion repository at specific Revision
     * And I save the job
     * And I build the job
     * And I check out code from Subversion repository at a different Revision with changes
     * And I save the job
     * And I build the job
     * Then changes should be visible on the Changes site
     */
    @Test
    public void build_has_changes() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUnsaveRepoAtRevision(1));

        f.save();
        f.startBuild().shouldSucceed();

        f.configure();
        subversionScm.url.set(svnContainer.getUrlUnsaveRepoAtRevision(2));
        f.save();
        f.startBuild().shouldSucceed();
        final Changes changes = f.getLastBuild().getChanges();
        assertTrue("Build has no changes.", changes.hasChanges());
    }

    /**
     * Scenario: Build has no changes
     * Given I have installed the "subversion" plugin
     * And a job
     * When I check out code from Subversion repository at specific Revision
     * And I save the job
     * And I build the job
     * Then no changes should be visible on the Changes site
     */
    @Test
    public void build_has_no_changes() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUnsaveRepo());

        f.save();
        f.startBuild();
        final Changes changes = f.getLastBuild().getChanges();
        assertFalse("Build has changes.", changes.hasChanges());
    }

    /**
     * Scenario: Build has changes and there is a link to view the diff of the changed file.
     * Given I have installed the "subversion" plugin
     * And a job
     * And the repository is accessible with the repository browser websvn
     * When I check out code from Subversion repository at specific Revision
     * And I select websvn as repository browser
     * And I enter the URL to websvn
     * And I save the job
     * And I build the job
     * And I check out code from Subversion repository at a different Revision with changes
     * And I save the job
     * And I build the job
     * Then a changed file with a diff link to websvn should be visible on the Changes site
     */
    @Test
    @Category(SmokeTest.class)
    public void build_has_changes_and_repoBrowser() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUnsaveRepoAtRevision(1));
        final SvnRepositoryBrowserWebSvn repositoryBrowserWebSvn = subversionScm.useRepositoryBrowser(SvnRepositoryBrowserWebSvn.class);
        repositoryBrowserWebSvn.url.set(svnContainer.getUrlWebSVN());
        f.save();
        f.startBuild().shouldSucceed();

        f.configure();
        subversionScm.url.set(svnContainer.getUrlUnsaveRepoAtRevision(2));
        f.save();
        f.startBuild().shouldSucceed();
        final Changes changes = f.getLastBuild().getChanges();
        assertTrue("Build has no diff link.", changes.hasDiffFileLink("testOne.txt"));
    }


    /**
     * Given I have installed the "subversion" plugin
     * And a job
     * When I check out code from Subversion repository at specific Revision
     * And I save the job
     * And I build the job
     * And I change the Url to a different Revision with changes
     * And I add a polling for changes every minute
     * And I save the job
     * Then there should be a second build after 70 seconds
     */
    @Test
    public void poll_for_changes() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUnsaveRepoAtRevision(1));
        f.save();
        f.startBuild().shouldSucceed();

        f.configure();
        subversionScm.url.set(svnContainer.getUrlUnsaveRepoAtRevision(2));
        f.pollScm().schedule("* * * * *");
        f.addShellStep("test -d .svn");
        f.save();

        sleep(70000);

        // We should have a second build after 70 seconds
        assertThat(f.getNextBuildNumber(), CoreMatchers.is(3));

    }

    /**
     * Given I have installed the "subversion" plugin
     * And a job
     * When I check out code from Subversion repository at specific Revision
     * And I save the job
     * And I build the job
     * And I change the Url to a different Revision with changes in a *.txt
     * And I add a polling for changes every minute
     * And I exclude changes of *.txt
     * And I save the job
     * Then there should not be a second build after 70 seconds
     */
    @Test
    public void poll_for_changes_excluded() throws SubversionPluginTestException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrlUnsaveRepoAtRevision(1));
        final SubversionSvmAdvanced scmAdvanced = subversionScm.advanced();
        scmAdvanced.excludedRegions.set(".*\\.txt");
        f.save();
        f.startBuild().shouldSucceed();

        f.configure();
        subversionScm.url.set(svnContainer.getUrlUnsaveRepoAtRevision(2));
        f.pollScm().schedule("* * * * *");
        f.save();

        sleep(70000);

        // We should not have a second build after 70 seconds
        assertThat(f.getNextBuildNumber(), CoreMatchers.is(2));
    }

}
