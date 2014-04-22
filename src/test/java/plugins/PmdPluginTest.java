package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdAction;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static org.jenkinsci.test.acceptance.Matchers.*;
import static org.hamcrest.CoreMatchers.*;

/**
 Feature: Tests for PMD plugin
 */
@WithPlugins("pmd")
public class PmdPluginTest extends AbstractCodeStylePluginHelper {

    /**
     Scenario: Configure a job with PMD post-build steps
       Given I have installed the "pmd" plugin
       And a job
       When I configure the job
       And I add "Publish PMD analysis results" post-build action
       And I copy resource "pmd_plugin/pmd.xml" into workspace
       And I set path to the pmd result "pmd.xml"
       And I save the job
       And I build the job
       Then the build should succeed
       And build page should has pmd summary "0 warnings"
     */
    @Test
    public void configure_a_job_with_PMD_post_build_steps() {
        FreeStyleJob job = setupJobAndRunOnceShouldSucceed("/pmd_plugin/pmd.xml", PmdPublisher.class, "pmd.xml");

        Build lastBuild = job.getLastBuild();
        lastBuild.shouldSucceed();

        assertThat(lastBuild.open(), hasContent("0 warnings"));
    }

    /**
     Scenario: Configure a job with PMD post-build steps to run always
       Given I have installed the "pmd" plugin
       And a job
       When I configure the job
       And I add "Publish PMD analysis results" post-build action
       And I copy resource "pmd_plugin/pmd.xml" into workspace
       And I set path to the pmd result "pmd.xml"
       And I add always fail build step
       And I set publish always pdm
       And I save the job
       And I build the job
       Then the build should fail
       And build page should has pmd summary "0 warnings"
     */
    @Test
    public void configure_a_job_with_PMD_post_build_steps_run_always() {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.copyResource(resource("/pmd_plugin/pmd.xml"));
        job.addShellStep("false");
        PmdPublisher pmd = job.addPublisher(PmdPublisher.class);
        pmd.pattern.set("pmd.xml");
        pmd.advanced.click();
        pmd.canRunOnFailed.check();
        job.save();

        Build b = job.queueBuild().waitUntilFinished().shouldFail();

        assertThat(b.open(), hasContent("0 warnings"));
    }

    /**
     Scenario: Configure a job with PMD post-build steps which display some warnings
       Given I have installed the "pmd" plugin
       And a job
       When I configure the job
       And I add "Publish PMD analysis results" post-build action
       And I copy resource "pmd_plugin/pmd-warnings.xml" into workspace
       And I set path to the pmd result "pmd-warnings.xml"
       And I save the job
       And I build the job
       Then the build should succeed
       And the build should have "PMD Warnings" action
       And build page should has pmd summary "9 warnings"
     */
    @Test
    public void configure_a_job_with_PMD_post_build_steps_which_display_some_warnings() {
        FreeStyleJob job = setupJobAndRunOnceShouldSucceed("/pmd_plugin/pmd-warnings.xml", PmdPublisher.class, "pmd-warnings.xml");

        Build lastBuild = job.getLastBuild();
        lastBuild.shouldSucceed();
        assertThat(lastBuild, hasAction("PMD Warnings"));
        assertThat(lastBuild.open(), hasContent("9 warnings"));

        PmdAction pa = new PmdAction(job);
        assertThat(pa.getWarningNumber(), is(9));
        assertThat(pa.getNewWarningNumber(), is(9));
        assertThat(pa.getFixedWarningNumber(), is(0));
        assertThat(pa.getHighWarningNumber(), is(0));
        assertThat(pa.getNormalWarningNumber(), is(3));
        assertThat(pa.getLowWarningNumber(), is(6));
    }

    /**
     Scenario: Configure a job with PMD post-build steps which display some warnings
     Given I have installed the "pmd" plugin
     And a job
     When I configure the job
     And I add "Publish PMD analysis results" post-build action
     And I copy resource "pmd_plugin/pmd-warnings.xml" into workspace
     And I set path to the pmd result "pmd-warnings.xml"
     And I save the job
     And I build the job
     Then the build should succeed
     When I configure the job
     And I remove the First Bild Step
     And I copy resource "pmd_plugin/pmd-warnings-2.xml" into workspace
     And I save the job
     And I build the job
     Then the build should succeed
     And the build should have "PMD Warnings" action
     And build page should has pmd summary "8 warnings"
     And build page should has pmd summary "1 new warning"
     And build page should has pmd summary "2 fixed warnings"
     */
    @Test
    public void configure_a_job_with_PMD_post_build_steps_which_display_some_warnings_two_runs() {
        FreeStyleJob job = setupJobAndRunTwiceShouldSucceed("/pmd_plugin/pmd-warnings.xml", PmdPublisher.class, "pmd-warnings.xml", "/pmd_plugin/pmd-warnings-2.xml");

        Build lastBuild = job.getLastBuild();
        assertThat(lastBuild, hasAction("PMD Warnings"));
        WebDriver lastBuildOpened = lastBuild.open();
        assertThat(lastBuildOpened, hasContent("8 warnings"));
        assertThat(lastBuildOpened, hasContent("1 new warning"));
        assertThat(lastBuildOpened, hasContent("2 fixed warnings"));

        PmdAction pa = new PmdAction(job);
        assertThat(pa.getWarningNumber(), is(8));
        assertThat(pa.getNewWarningNumber(), is(1));
        assertThat(pa.getFixedWarningNumber(), is(2));
        assertThat(pa.getHighWarningNumber(), is(0));
        assertThat(pa.getNormalWarningNumber(), is(2));
        assertThat(pa.getLowWarningNumber(), is(6));
    }
}
