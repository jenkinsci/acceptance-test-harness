package plugins;

import java.util.List;
import java.util.Map;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.warnings.IssuesRecorder;
import org.jenkinsci.test.acceptance.plugins.warnings.IssuesRecorder.StaticAnalysisTool;
import org.jenkinsci.test.acceptance.plugins.warnings.SummaryPage;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsPriorityChart;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsResultDetailsPage;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsResultDetailsPage.Tabs;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsTrendChart;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.MessageBox;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static plugins.warnings.assertions.Assertions.*;

/**
 * Acceptance tests for the White Mountains release of the warnings plug-in.
 *
 * @author Ullrich Hafner
 * @author Manuel Hampp
 * @author Anna-Maria Hardi
 * @author Elvira Hauer
 * @author Stephan Plöderl
 * @author Alexander Praegla
 * @author Michaela Reitschuster
 * @author Arne Schöntag
 * @author Alexandra Wenzel
 * @author Nikolai Wohlgemuth
 */
@WithPlugins("warnings")
public class AnalysisPluginsTest extends AbstractJUnitTest {
    private static final String WARNINGS_PLUGIN_PREFIX = "/warnings_plugin/white-mountains/";
    private static final String CHECKSTYLE_XML = "checkstyle-result.xml";
    private static final String CHECKSTYLE_ID = "checkstyle";

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

        WarningsResultDetailsPage page = getWarningsResultDetailsPage(build, "cpd");
        page.openTab(Tabs.DETAILS);
        List<Map<String, WebElement>> issuesTable = page.getIssuesTable();
        Map<String, WebElement> firstRowOfIssuesTable = issuesTable.get(0);
        assertThat(firstRowOfIssuesTable.keySet()).contains("Details");
    }

    private WarningsResultDetailsPage getWarningsResultDetailsPage(final Build build, final String id) {
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
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_01");
        applyIssueRecorder(job);
        job.save();

        Build referenceBuild = job.startBuild().waitUntilFinished();

        reconfigureJobWithResource(job, "build_status_test/build_02");

        Build build = job.startBuild().waitUntilFinished();
        build.open();

        SummaryPage summaryPage = new SummaryPage(build, false);

        assertThat(summaryPage.getSummaryBoxByName(CHECKSTYLE_ID)).hasSummary();
        assertThat(summaryPage.getSummaryBoxByName("pmd")).hasSummary();
        assertThat(summaryPage.getSummaryBoxByName("findbugs")).hasSummary();

        summaryPage.getSummaryBoxByName(CHECKSTYLE_ID).getTitleResultLink().click();
        WarningsResultDetailsPage checkstyleDetails = getWarningsResultDetailsPage(build, CHECKSTYLE_ID);
        assertThat(checkstyleDetails.getTrendChart())
                .hasNewIssues(3)
                .hasFixedIssues(0)
                .hasOutstandingIssues(0);
        assertThat(jenkins.getCurrentUrl()).isEqualTo(checkstyleDetails.url.toString());
        
        build.open();

        summaryPage.getSummaryBoxByName(CHECKSTYLE_ID).getTitleResultInfoLink().click();
        MessageBox messageBox = new MessageBox(build, CHECKSTYLE_ID);
        assertThat(messageBox).containsInfoMessage("checkstyle-result.xml: found 3 issues");
        assertThat(jenkins.getCurrentUrl()).isEqualTo(messageBox.url.toString());

        build.open();

        summaryPage.getSummaryBoxByName(CHECKSTYLE_ID).findClickableResultEntryByNamePart("new").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "checkstyleResult/new/");

        build.open();

        summaryPage.getSummaryBoxByName(CHECKSTYLE_ID).findClickableResultEntryByNamePart("Reference").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(referenceBuild.url + "checkstyleResult/");

        build.open();

        String noWarningsResult = summaryPage.getSummaryBoxByName("findbugs")
                .findResultEntryTextByNamePart("No warnings for");
        assertThat(noWarningsResult).isEqualTo("No warnings for 2 builds, i.e. since build 1");
    }

    /**
     * Tests the result overview with aggregated results by running two builds with three issue parsers.
     */
    @Test
    public void should_show_expected_aggregations_in_result_box() {
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_01");
        IssuesRecorder recorder = applyIssueRecorder(job);
        recorder.setEnabledForAggregation(true);
        job.save();

        Build referenceBuild = job.startBuild().waitUntilFinished();
        reconfigureJobWithResource(job, "build_status_test/build_02");

        Build build = job.startBuild().waitUntilFinished();

        build.open();

        SummaryPage summaryPage = new SummaryPage(build, true);

        assertThat(summaryPage.getSummaryBoxByName("analysis")).hasSummary();
        // FIXME: @uhafner Field should also contain findbugs, even if there is no issue...
        //String resultsFrom = summaryPage.getSummaryBoxByName("analysis")
        //        .findResultEntryTextByNamePart("Static analysis results from");
        //assertThat(resultsFrom.toLowerCase()).contains(plugins);

        summaryPage.getSummaryBoxByName("analysis").getTitleResultLink().click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "analysisResult/");

        build.open();

        summaryPage.getSummaryBoxByName("analysis").getTitleResultInfoLink().click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "analysisResult/info/");

        build.open();

        summaryPage.getSummaryBoxByName("analysis").findClickableResultEntryByNamePart("2 new warnings").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "analysisResult/new/");

        build.open();

        summaryPage.getSummaryBoxByName("analysis").findClickableResultEntryByNamePart("One fixed warning").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "analysisResult/fixed/");

        build.open();

        summaryPage.getSummaryBoxByName("analysis").findClickableResultEntryByNamePart("Reference build").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(referenceBuild.url + "analysisResult/");
    }

    private void reconfigureJobWithResource(final FreeStyleJob job, final String fileName) {
        job.configure(() -> job.copyResource(WARNINGS_PLUGIN_PREFIX + fileName));
    }

    /**
     * Tests the functionality of the result overview with quality gate enabled.
     */
    @Test
    public void should_contain_expected_quality_gate_results() {
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_01");
        IssuesRecorder recorder = applyIssueRecorder(job);
        recorder.addQualityGateConfiguration(2);
        job.save();

        Build build = job.startBuild().waitUntilFinished();

        build.open();

        SummaryPage summaryPage = new SummaryPage(build, false);

        //Checks if the whole build is marked as failed (in the title)
        assertThat(summaryPage.getBuildState()).isEqualTo("Failed");

        //Checks if the issue parser boxes contain the expected quality gate states
        assertThat(summaryPage.getSummaryBoxByName("checkstyle")).hasQualityGateState("Success");
        assertThat(summaryPage.getSummaryBoxByName("findbugs")).hasQualityGateState("Success");
        assertThat(summaryPage.getSummaryBoxByName("pmd")).hasQualityGateState("Failed");

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
     * Simple test to check that the console log shows that build was a failure when thresholds of quality gate have been
     * reached.
     */
    @Test
    public void should_log_failure__when_quality_gate_thresholds_are_reached() {
        FreeStyleJob job = createFreeStyleJob("/warnings_plugin/checkstyle-result.xml");
        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        recorder.setTool("CheckStyle");
        recorder.openAdvancedOptions();
        recorder.setEnabledForFailure(true);
        recorder.addQualityGateConfiguration(5);
        job.save();

        Build build = job.startBuild().waitUntilFinished();
        assertThat(build.getConsole()).contains("Finished: FAILURE");
    }

    /**
     * Starts two builds with different configurations and checks the values of the new, fixed and outstanding issues of
     * the trend chart.
     */
    @Test
    public void should_log_values_in_trend_chart() {
        FreeStyleJob job = createFreeStyleJob("aggregation/checkstyle1.xml", "aggregation/checkstyle2.xml",
                "aggregation/pmd.xml");
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle", "**/checkstyle1.xml");
            recorder.addTool("PMD", "**/pmd.xml");
            recorder.setEnabledForAggregation(true);
        });
        job.save();

        job.startBuild().waitUntilFinished();

        job.editPublisher(IssuesRecorder.class, recorder -> recorder.setTool("CheckStyle", "**/checkstyle2.xml"));

        Build build = job.startBuild().waitUntilFinished();
        build.open();

        WarningsResultDetailsPage page = getWarningsResultDetailsPage(build, "analysis");

        WarningsTrendChart trend = page.getTrendChart();
        assertThat(trend).hasNewIssues(3);
        assertThat(trend).hasFixedIssues(2);
        assertThat(trend).hasOutstandingIssues(5);

        WarningsPriorityChart priorities = page.getPriorityChart();
        assertThat(priorities).hasLowPriority(1);
        assertThat(priorities).hasNormalPriority(2);
        assertThat(priorities).hasHighPriority(5);

        assertThat(page.getIssuesTable()).hasSize(8);
    }

    /**
     * Builds a FreeStyle build and that builds with a xml file
     * and checks if the results shown in the MessageBox are as expected.
     */
    @Test
    public void shouldBeOkIfContentsOfMsgBoxesAreCorrectForFreeStyleJob() {
        FreeStyleJob job = createFreeStyleJob(CHECKSTYLE_XML);
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool("CheckStyle", "**/checkstyle-result.xml"));
        job.save();
        Build build = job.startBuild().waitUntilFinished();

        MessageBox messageBox = new MessageBox(build, CHECKSTYLE_ID);
        messageBox.open();

        // Check Error Panel
        messageBox.getErrorMsgContent();
        String errno1 = "Can't read file '/mnt/hudson_workspace/workspace/HTS-CheckstyleTest/ssh-slaves"
                + "/src/main/java/hudson/plugins/sshslaves/RemoteLauncher.java': java.nio.file.NoSuchFileException:"
                + " \\mnt\\hudson_workspace\\workspace\\HTS-CheckstyleTest\\ssh-slaves\\src\\main\\java\\hudson\\plugins"
                + "\\sshslaves\\RemoteLauncher.java";
        assertThat(messageBox).hasErrorMessagesSize(3);
        assertThat(messageBox).containsErrorMessage(errno1);

        // Check Info Panel
        messageBox.getInfoMsgContent();
        assertThat(messageBox).hasInfoMessagesSize(7);
        assertThat(messageBox).containsInfoMessage("found 1 file");
        assertThat(messageBox).containsInfoMessage("for 2 issues");
        assertThat(messageBox).containsInfoMessage("No quality gates have been set - skipping");
    }
}

