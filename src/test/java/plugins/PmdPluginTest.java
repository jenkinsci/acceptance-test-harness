package plugins;

import org.jenkinsci.test.acceptance.junit.Bug;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdAction;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.is;
import static org.jenkinsci.test.acceptance.Matchers.hasAction;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

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
        FreeStyleJob job = setupJob("/pmd_plugin/pmd.xml", PmdPublisher.class, "pmd.xml");
        Build lastBuild = buildJobWithSuccess(job);

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

        Build b = job.startBuild().waitUntilFinished().shouldFail();

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
        FreeStyleJob job = setupJob("/pmd_plugin/pmd-warnings.xml", PmdPublisher.class, "pmd-warnings.xml");

        Build lastBuild = buildJobWithSuccess(job);
        assertThat(lastBuild, hasAction("PMD Warnings"));
        lastBuild.open();
        PmdAction pa = new PmdAction(job);
        assertThat(pa.getResultLinkByXPathText("9 warnings"), is("pmdResult"));
        assertThat(pa.getResultLinkByXPathText("9 new warnings"), is("pmdResult/new"));
        assertThat(pa.getWarningNumber(), is(9));
        assertThat(pa.getNewWarningNumber(), is(9));
        assertThat(pa.getFixedWarningNumber(), is(0));
        assertThat(pa.getHighWarningNumber(), is(0));
        assertThat(pa.getNormalWarningNumber(), is(3));
        assertThat(pa.getLowWarningNumber(), is(6));
        assertFileTab(pa);
        assertTypeTab(pa);
        assertWarningsTab(pa);
    }

    private void assertFileTab(PmdAction pa) {
        final SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("ChannelContentAPIClient.m", 6);
        expectedContent.put("ProductDetailAPIClient.m", 2);
        expectedContent.put("ViewAllHoldingsAPIClient.m", 1);
        assertThat(pa.getFileTabContents(), is(expectedContent));
    }

    private void assertTypeTab(PmdAction pa) {
        final SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("long line", 6);
        expectedContent.put("unused method parameter", 3);
        assertThat(pa.getTypesTabContents(), is(expectedContent));
    }

    private void assertWarningsTab(PmdAction pa) {
        final SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("ChannelContentAPIClient.m:28", 28);
        expectedContent.put("ChannelContentAPIClient.m:28", 28);
        expectedContent.put("ChannelContentAPIClient.m:28", 28);
        expectedContent.put("ChannelContentAPIClient.m:32", 32);
        expectedContent.put("ChannelContentAPIClient.m:36", 36);
        expectedContent.put("ChannelContentAPIClient.m:40", 40);
        expectedContent.put("ProductDetailAPIClient.m:37", 37);
        expectedContent.put("ProductDetailAPIClient.m:38", 38);
        expectedContent.put("ViewAllHoldingsAPIClient.m:23", 23);
        assertThat(pa.getWarningsTabContents(), is(expectedContent));
    }

    /*
     * Builds a job and tests if the PMD api (with depth=0 parameter set) responds with the expected output.
     * Difference in whitespaces are ok.
     */
    @Test
    public void xml_api_report_depth_0() throws IOException, SAXException, ParserConfigurationException {
        final FreeStyleJob job = setupJob("/pmd_plugin/pmd-warnings.xml", PmdPublisher.class, "pmd-warnings.xml");
        final Build build = buildJobWithSuccess(job);
        final String apiUrl = "pmdResult/api/xml?depth=0";
        final String expectedXmlPath = "/pmd_plugin/api_depth_0.xml";
        assertXmlApiMatchesExpected(build, apiUrl, expectedXmlPath);
    }

    /**
     * Runs job two times to check if new and fixed warnings are displayed.
     */
    @Test
    public void configure_a_job_with_PMD_post_build_steps_which_display_some_warnings_two_runs() {
        FreeStyleJob job = setupJob("/pmd_plugin/pmd-warnings.xml", PmdPublisher.class, "pmd-warnings.xml");
        buildJobAndWait(job);
        editJobAndChangeLastRessource(job, "/pmd_plugin/pmd-warnings-2.xml", "pmd-warnings.xml");

        Build lastBuild = buildJobWithSuccess(job);
        assertThat(lastBuild, hasAction("PMD Warnings"));
        lastBuild.open();
        PmdAction pa = new PmdAction(job);
        assertThat(pa.getResultLinkByXPathText("8 warnings"), is("pmdResult"));
        assertThat(pa.getResultLinkByXPathText("1 new warning"), is("pmdResult/new"));
        assertThat(pa.getResultLinkByXPathText("2 fixed warnings"), is("pmdResult/fixed"));
        assertThat(pa.getWarningNumber(), is(8));
        assertThat(pa.getNewWarningNumber(), is(1));
        assertThat(pa.getFixedWarningNumber(), is(2));
        assertThat(pa.getHighWarningNumber(), is(0));
        assertThat(pa.getNormalWarningNumber(), is(2));
        assertThat(pa.getLowWarningNumber(), is(6));
    }

    /**
     * Runs job two times to check if the links of the graph are relative.
     */
    @Test @Bug("21723") @Ignore("Until JENKINS-21723 is fixed")
    public void view_pmd_report_job_graph_links() {
        FreeStyleJob job = setupJob("/pmd_plugin/pmd-warnings.xml", PmdPublisher.class, "pmd-warnings.xml");
        buildJobAndWait(job);
        editJobAndChangeLastRessource(job, "/pmd_plugin/pmd-warnings-2.xml", "pmd-warnings.xml");
        buildJobWithSuccess(job);

        assertAreaLinksOfJobAreLike(job, "^\\d+/pmdResult");
    }

    /*
     * Runs a job with warning threshold configured once and validates that build is marked as unstable.
     */
    @Test @Bug("19614")
    public void build_with_warning_threshold_set_should_be_unstable() {
        final FreeStyleJob job = setupJob("/pmd_plugin/pmd-warnings.xml", PmdPublisher.class, "pmd-warnings.xml", "0", "0", true);
        final Build build = buildJobAndWait(job);
        assertThat(build.isUnstable(), is(true));
    }
}
