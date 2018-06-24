package plugins;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.warnings.IssuesRecorder;
import org.jenkinsci.test.acceptance.plugins.warnings.IssuesRecorder.StaticAnalysisTool;
import org.jenkinsci.test.acceptance.plugins.warnings.SummaryBoxPageAreaAssert;
import org.jenkinsci.test.acceptance.plugins.warnings.SummaryPage;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsResultDetailsPage;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsResultDetailsPage.Tabs;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.assertj.core.api.Assertions.*;

/**
 * Acceptance tests for the White Mountains release of the warnings plug-in.
 *
 * @author Ullrich Hafner
 * @author Michaela Reitschuster
 * @author Alexandra Wenzel
 * @author Manuel Hampp
 * @author Anna-Maria Hardi
 * @author Stephan Plöderl
 */
@WithPlugins("warnings")
public class AnalysisPluginsTest extends AbstractJUnitTest {
    private static final String WARNINGS_PLUGIN_PREFIX = "/warnings_plugin/white-mountains/";

    /**
     * Simple test to check that there are some duplicate code warnings.
     */
    @Test
    public void should_have_duplicate_code_warnings() {
        FreeStyleJob job = createFreeStyleJob("duplicate_code/cpd.xml", "duplicate_code/Main.java");

        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        StaticAnalysisTool tool = recorder.setTool("CPD");
        tool.setNormalThreshold(1);
        tool.setHighThreshold(2);
        job.save();

        Build build = job.startBuild().waitUntilFinished();

        WarningsResultDetailsPage page = getWarningsResultDetailsPage("cpd", build);
        page.openTab(Tabs.DETAILS);
        List<Map<String, WebElement>> issuesTable = page.getIssuesTable();
        Map<String, WebElement> firstRowOfIssuesTable = issuesTable.get(0);
        assertThat(firstRowOfIssuesTable.keySet()).contains("Details");
    }

    private WarningsResultDetailsPage getWarningsResultDetailsPage(final String id, final Build build) {
        WarningsResultDetailsPage resultPage = new WarningsResultDetailsPage(build, id);
        resultPage.open();
        return resultPage;
    }

    /**
     * Simple test to check that warnings of checkstyle and pmd file are handled separately if aggregation is not
     * activated.
     */
    @Test
    public void should_log_ok_in_console_with_not_activated_aggregation() {
        FreeStyleJob job = createFreeStyleJob("aggregation/checkstyle.xml", "aggregation/pmd.xml");

        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle", "**/checkstyle.xml");
            recorder.addTool("PMD", "**/pmd.xml");
            recorder.setEnabledForAggregation(false);
        });

        job.save();

        Build build = job.startBuild().waitUntilFinished();

        assertThat(build.getConsole()).contains("[CheckStyle] Created analysis result for 6 issues");
        assertThat(build.getConsole()).contains("[PMD] Created analysis result for 4 issues");
    }

    /**
     * Simple test to check that warnings of checkstyle and pmd file are summed up if aggregation is activated.
     */
    @Test
    public void should_log_ok_in_console_with_activated_aggregation() {
        FreeStyleJob job = createFreeStyleJob("aggregation/checkstyle.xml", "aggregation/pmd.xml");

        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle", "**/checkstyle.xml");
            recorder.addTool("PMD", "**/pmd.xml");
            recorder.setEnabledForAggregation(true);
        });

        job.save();

        Build build = job.startBuild().waitUntilFinished();

        assertThat(build.getConsole()).contains("[Static Analysis] Created analysis result for 10 issues");
    }

    private FreeStyleJob createFreeStyleJob(final String... resourcesToCopy) {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        for (String resource : resourcesToCopy) {
            job.copyResource(resource(WARNINGS_PLUGIN_PREFIX + resource));
        }
        return job;
    }

    /**
     * Test to check that the issue filter can be configured and is applied.
     */
    @Test
    public void should_log_filter_applied_in_console() {
        FreeStyleJob job = createFreeStyleJob("issue_filter/checkstyle-result.xml");

        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle");
            recorder.openAdvancedOptions();
            recorder.setEnabledForFailure(true);
            recorder.addIssueFilter("Exclude categories", "Checks");
            recorder.addIssueFilter("Include types", "JavadocMethodCheck");
        });

        job.save();

        Build build = job.startBuild().waitUntilFinished();

        assertThat(build.getConsole()).contains(
                "[CheckStyle] Applying 2 filters on the set of 4 issues (3 issues have been removed)");
    }

    /**
     * Tests the result overview by running two builds with three issue parsers enabled. Checks if the result boxes for
     * each parser contain the expected contents.
     */
    @Test
    public void should_show_correct_plugin_result_boxes() {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        job.copyResource(WARNINGS_PLUGIN_PREFIX + "build_status_test/build_01");
        applyIssueRecorder(job);
        job.save();

        Build build1 = job.startBuild().waitUntilFinished();

        visit(job.getConfigUrl());
        job.copyResource(WARNINGS_PLUGIN_PREFIX + "build_status_test/build_02");
        job.save();

        Build build2 = job.startBuild().waitUntilFinished();
        visit(build2.url);

        List<String> plugins = Arrays.asList("checkstyle", "pmd", "findbugs");

        SummaryPage summaryPage = new SummaryPage(build2, plugins, false);

        // assert that all configured plugins have a corresponding summary box
        SummaryBoxPageAreaAssert.assertThat(summaryPage.getSummaryBoxByName("checkstyle")).hasWarningDiv();
        SummaryBoxPageAreaAssert.assertThat(summaryPage.getSummaryBoxByName("pmd")).hasWarningDiv();
        SummaryBoxPageAreaAssert.assertThat(summaryPage.getSummaryBoxByName("findbugs")).hasWarningDiv();

        // assert that boxes contain correct links and content
        summaryPage.getSummaryBoxByName("checkstyle").getTitleDivResultLink().click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build2.url + "checkstyleResult/");

        //TODO: Check if there is a possibility to keep the pageobject after visting a different page, for example by opening a tab to visit the second url
        visit(build2.url);
        summaryPage = new SummaryPage(build2, plugins, false);

        summaryPage.getSummaryBoxByName("checkstyle").getTitleDivResultInfoLink().click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build2.url + "checkstyleResult/info/");

        visit(build2.url);
        summaryPage = new SummaryPage(build2, plugins, false);

        summaryPage.getSummaryBoxByName("checkstyle").findClickableResultEntryByNamePart("new").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build2.url + "checkstyleResult/new/");

        visit(build2.url);
        summaryPage = new SummaryPage(build2, plugins, false);

        summaryPage.getSummaryBoxByName("checkstyle").findClickableResultEntryByNamePart("Reference").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build1.url + "checkstyleResult/");

        visit(build2.url);
        summaryPage = new SummaryPage(build2, plugins, false);

        String noWarningsResult = summaryPage.getSummaryBoxByName("findbugs")
                .findResultEntryTextByNamePart("No warnings for");
        assertThat(noWarningsResult).isEqualTo("No warnings for 2 builds, i.e. since build 1");
    }

    /**
     * Tests the result overview with aggregated results by running two builds with three issue parsers.
     */
    @Test
    public void should_show_expected_aggregations_in_result_box() {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        job.copyResource(WARNINGS_PLUGIN_PREFIX + "build_status_test/build_01");
        IssuesRecorder recorder = applyIssueRecorder(job);
        recorder.setEnabledForAggregation(true);
        job.save();

        Build build1 = job.startBuild().waitUntilFinished();
        visit(job.getConfigUrl());
        job.copyResource(WARNINGS_PLUGIN_PREFIX + "build_status_test/build_02");
        job.save();

        Build build2 = job.startBuild().waitUntilFinished();

        visit(build2.url);

        List<String> plugins = Arrays.asList("checkstyle", "pmd", "findbugs");

        SummaryPage summaryPage = new SummaryPage(build2, plugins, true);

        SummaryBoxPageAreaAssert.assertThat(summaryPage.getSummaryBoxByName("analysis")).hasWarningDiv();
        // FIXME: Field should also contain findbugs, even if there is no issue...
        //String resultsFrom = summaryPage.getSummaryBoxByName("analysis")
        //        .findResultEntryTextByNamePart("Static analysis results from");
        //assertThat(resultsFrom.toLowerCase()).contains(plugins);

        summaryPage.getSummaryBoxByName("analysis").getTitleDivResultLink().click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build2.url + "analysisResult/");

        visit(build2.url);
        summaryPage = new SummaryPage(build2, plugins, true);

        summaryPage.getSummaryBoxByName("analysis").getTitleDivResultInfoLink().click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build2.url + "analysisResult/info/");

        visit(build2.url);
        summaryPage = new SummaryPage(build2, plugins, true);

        summaryPage.getSummaryBoxByName("analysis").findClickableResultEntryByNamePart("2 new warnings").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build2.url + "analysisResult/new/");

        visit(build2.url);
        summaryPage = new SummaryPage(build2, plugins, true);

        summaryPage.getSummaryBoxByName("analysis").findClickableResultEntryByNamePart("One fixed warning").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build2.url + "analysisResult/fixed/");

        visit(build2.url);
        summaryPage = new SummaryPage(build2, plugins, true);

        summaryPage.getSummaryBoxByName("analysis").findClickableResultEntryByNamePart("Reference build").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build1.url + "analysisResult/");
    }

    /**
     * Tests the functionality of the result overview with qualitygate enabled.
     */
    @Test
    public void should_contain_expected_qualitygate_results() {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        job.copyResource(WARNINGS_PLUGIN_PREFIX + "build_status_test/build_01");
        IssuesRecorder recorder = applyIssueRecorder(job);
        recorder.addQualityGateConfiguration(2);

        job.save();

        Build build1 = job.startBuild().waitUntilFinished();

        List<String> plugins = Arrays.asList("checkstyle", "pmd", "findbugs");
        visit(build1.url);

        SummaryPage summaryPage = new SummaryPage(build1, plugins, false);

        //Checks if the whole build is marked as failed (in the title)
        assertThat(summaryPage.getBuildState()).isEqualTo("Failed");

        //Checks if the issue parser boxes contain the expected quality gate states
        SummaryBoxPageAreaAssert.assertThat(summaryPage.getSummaryBoxByName("checkstyle"))
                .hasQualityGateState("Success");
        SummaryBoxPageAreaAssert.assertThat(summaryPage.getSummaryBoxByName("findbugs")).hasQualityGateState("Success");
        SummaryBoxPageAreaAssert.assertThat(summaryPage.getSummaryBoxByName("pmd")).hasQualityGateState("Failed");

    }

    private IssuesRecorder applyIssueRecorder(final FreeStyleJob job) {
        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);

        recorder.setTool("CheckStyle");
        recorder.addTool("FindBugs");
        recorder.addTool("PMD");
        recorder.openAdvancedOptions();
        recorder.setEnabledForFailure(true);
        return recorder;
    }

    /**
     * Simple test to check that the console log shows that build was a failure when thresholds of qualitygate have been
     * reached.
     */
    @Test
    public void should_log_failure__when_qualitygate_thresholds_are_reached() {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        job.copyResource("/warnings_plugin/checkstyle-result.xml");
        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        recorder.setTool("CheckStyle");
        recorder.openAdvancedOptions();
        recorder.setEnabledForFailure(true);
        recorder.addQualityGateConfiguration(5);
        job.save();

        Build build = job.startBuild().waitUntilFinished();
        assertThat(build.getConsole()).contains("Finished: FAILURE");
    }

}

