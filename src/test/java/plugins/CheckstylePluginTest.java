package plugins;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckstyleAction;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckstylePublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import static org.hamcrest.CoreMatchers.is;
import static org.jenkinsci.test.acceptance.Matchers.hasAction;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

/**
 * Feature: Allow publishing of Checkstyle report
   In order to be able to check code style of my project
   As a Jenkins user
   I want to be able to publish Checkstyle report
 */
@WithPlugins("checkstyle")
public class CheckstylePluginTest extends AbstractCodeStylePluginHelper {
    /**
     * Scenario: Record Checkstyle report
         Given I have installed the "checkstyle" plugin
         And a job
         When I configure the job
         And I copy resource "checkstyle_plugin/checkstyle-result.xml" into workspace
         And I add "Publish Checkstyle analysis results" post-build action
         And I set up "checkstyle-result.xml" as the Checkstyle results
         And I save the job
         And I build the job
         Then the build should have "Checkstyle Warnings" action
         And the job should have "Checkstyle Warnings" action
     */
    @Test
    public void record_checkstyle_report() {
        FreeStyleJob job = setupJob("/checkstyle_plugin/checkstyle-result.xml", CheckstylePublisher.class, "checkstyle-result.xml");
        buildJobWithSuccess(job);

        assertThat(job.getLastBuild(), hasAction("Checkstyle Warnings"));
        assertThat(job, hasAction("Checkstyle Warnings"));
    }

    /**
     *   Scenario: View Checkstyle report
         Given I have installed the "checkstyle" plugin
         And a job
         When I configure the job
         And I copy resource "checkstyle_plugin/checkstyle-result.xml" into workspace
         And I add "Publish Checkstyle analysis results" post-build action
         And I set up "checkstyle-result.xml" as the Checkstyle results
         And I save the job
         And I build the job
         Then the build should succeed
         When I visit Checkstyle report
         Then I should see there are 776 warnings
         And I should see there are 776 new warnings
         And I should see there are 0 fixed warnings
         And I should see there are 776 high priority warnings
         And I should see there are 0 normal priority warnings
         And I should see there are 0 low priority warnings
     */
    @Test
    public void view_checkstyle_report() {
        FreeStyleJob job = setupJob("/checkstyle_plugin/checkstyle-result.xml", CheckstylePublisher.class, "checkstyle-result.xml");

        Build lastBuild = buildJobWithSuccess(job);
        assertThat(lastBuild, hasAction("Checkstyle Warnings"));
        WebDriver lastBuildOpened = lastBuild.open();
        assertThat(lastBuildOpened, hasContent("776 warnings"));

        CheckstyleAction ca = new CheckstyleAction(job);
        assertThat(ca.getWarningNumber(), is(776));
        assertThat(ca.getNewWarningNumber(), is(776));
        assertThat(ca.getFixedWarningNumber(), is(0));
        assertThat(ca.getHighWarningNumber(), is(776));
        assertThat(ca.getNormalWarningNumber(), is(0));
        assertThat(ca.getLowWarningNumber(), is(0));
    }

    /**
     * Runs job two times to check if new and fixed warnings are displayed.
     */
    @Test
    public void view_checkstyle_report_two_runs_and_changed_results() {
        FreeStyleJob job = setupJob("/checkstyle_plugin/checkstyle-result.xml", CheckstylePublisher.class, "checkstyle-result.xml");
        buildJobAndWait(job);
        editJobAndChangeLastRessource(job, "/checkstyle_plugin/checkstyle-result-2.xml", "checkstyle-result.xml");

        Build lastBuild = buildJobWithSuccess(job);
        assertThat(lastBuild, hasAction("Checkstyle Warnings"));
        WebDriver lastBuildOpened = lastBuild.open();
        assertThat(lastBuildOpened, hasContent("679 warnings"));

        CheckstyleAction ca = new CheckstyleAction(job);
        assertThat(ca.getWarningNumber(), is(679));
        assertThat(ca.getNewWarningNumber(), is(3));
        assertThat(ca.getFixedWarningNumber(), is(100));
        assertThat(ca.getHighWarningNumber(), is(679));
        assertThat(ca.getNormalWarningNumber(), is(0));
        assertThat(ca.getLowWarningNumber(), is(0));
    }

}
