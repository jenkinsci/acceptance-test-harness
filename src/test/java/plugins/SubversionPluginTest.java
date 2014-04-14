package plugins;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.SvnContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionScm;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import java.io.IOException;

/**
 * Feature: Subversion support
 * As a user
 * I want to be able to check out source code from Subversion
 */
@WithPlugins("subversion")
@Native("docker")
public class SubversionPluginTest extends AbstractJUnitTest {
    @Inject
    DockerContainerHolder<SvnContainer> svn;

    /**
     * Scenario: Run basic Subversion build
     * Given I have installed the "subversion" plugin
     * And a job
     * When I check out code from Subversion repository "<svnContainerUrl>/svn"
     * And I add a shell build step "test -d .svn"
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And console output should contain "test -d .svn"
     */
    @Test
    public void run_basic_subversion_build() throws IOException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        f.configure();
        f.useScm(SubversionScm.class).url.set(svnContainer.getUrl());
        f.addShellStep("test -d .svn");
        f.save();

        f.queueBuild().shouldSucceed().shouldContainsConsoleOutput("test -d .svn");
    }

    /**
     * Scenario: Check out specified Subversion revision
     * Given I have installed the "subversion" plugin
     * And a job
     * When I check out code from Subversion repository "<svnContainerUrl>/svn" @ revision 0
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And console output should contain "At revision 0"
     */
    @Test
    public void checkout_specific_revision() throws IOException {
        final String revision = "0";
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        f.configure();
        f.useScm(SubversionScm.class).url.set(svnContainer.getUrl() + "@" + revision);
        f.save();

        f.queueBuild().shouldSucceed().shouldContainsConsoleOutput("At revision " + revision);
    }

    /**
     * Scenario: Always check out fresh copy
     * Given I have installed the "subversion" plugin
     * And a job
     * When I check out code from Subversion repository "<svnContainerUrl>/svn"
     * And I select "Always check out a fresh copy" as a "Check-out Strategy"
     * And I save the job
     * And I build 2 jobs
     * Then the build should succeed
     * And console output should contain "Checking out <svnContainerUrl>"
     */
    @Test
    public void always_checkout_fresh_copy() throws IOException {
        final SvnContainer svnContainer = svn.get();
        final FreeStyleJob f = jenkins.jobs.create();
        f.configure();

        final SubversionScm subversionScm = f.useScm(SubversionScm.class);
        subversionScm.url.set(svnContainer.getUrl());
        subversionScm.checkoutStrategy.select("Always check out a fresh copy");
        f.save();

        f.queueBuild().shouldSucceed();

        f.queueBuild().shouldSucceed()
                .shouldContainsConsoleOutput("Checking out " + svnContainer.getUrl());
    }
}
