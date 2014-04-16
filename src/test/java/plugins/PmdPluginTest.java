package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 Feature: Tests for PMD plugin
 */
@WithPlugins("pmd")
public class PmdPluginTest extends AbstractJUnitTest {

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
        FreeStyleJob job = setupJob("/pmd_plugin/pmd.xml", "pmd.xml");

        Build b = job.queueBuild().waitUntilFinished().shouldSucceed();

        assertThat(b.open(), hasContent("0 warnings"));
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
        FreeStyleJob j = jenkins.jobs.create();
        j.configure();
        j.copyResource(resource("/pmd_plugin/pmd.xml"));
        j.addShellStep("false");
        PmdPublisher pmd = j.addPublisher(PmdPublisher.class);
        pmd.pattern.set("pmd.xml");
        pmd.advanced.click();
        pmd.canRunOnFailed.check();
        j.save();

        Build b = j.queueBuild().waitUntilFinished().shouldFail();

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
        FreeStyleJob job = setupJob("/pmd_plugin/pmd-warnings.xml", "pmd-warnings.xml");

        Build b = job.queueBuild().waitUntilFinished().shouldSucceed();

        assertThat(b, hasAction("PMD Warnings"));
        assertThat(b.open(), hasContent("9 warnings"));
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
        FreeStyleJob job = setupJob("/pmd_plugin/pmd-warnings.xml", "pmd-warnings.xml");
        Build b1 = job.queueBuild().waitUntilFinished().shouldSucceed();

        job.configure();
        job.removeFirstBuildStep();
        job.copyResource(resource("/pmd_plugin/pmd-warnings-2.xml"), "pmd-warnings.xml");
        job.save();
        Build b2 = job.queueBuild().waitUntilFinished().shouldSucceed();

        assertThat(b2, hasAction("PMD Warnings"));
        WebDriver b2opened = b2.open();
        assertThat(b2opened, hasContent("8 warnings"));
        assertThat(b2opened, hasContent("1 new warning"));
        assertThat(b2opened, hasContent("2 fixed warnings"));
    }

    private FreeStyleJob setupJob(String resource, String pattern) {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.copyResource(resource(resource));
        PmdPublisher pmd = job.addPublisher(PmdPublisher.class);
        pmd.pattern.set(pattern);
        job.save();
        return job;
    }
}
