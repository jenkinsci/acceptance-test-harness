package plugins.warnings;

import java.util.function.Consumer;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.AbstractNonDetailsIssuesTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.DetailsTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.DryIssuesTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.IssuesRecorder;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.IssuesRecorder.StaticAnalysisTool;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.IssuesTable;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.SourceCodeView;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.SummaryPage;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.SummaryPage.SummaryBoxPageArea;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.WarningsPriorityChart;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.WarningsResultDetailsPage;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.WarningsTrendChart;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.MessageBox;
import org.junit.Test;

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
    private static final String HIGH_PRIORITY = "High";
    private static final String LOW_PRIORITY = "Low";
    private static final String ANALYSIS_ID = "analysis";

    /**
     * Simple test to check that there are some duplicate code warnings.
     */
    @Test
    public void should_have_duplicate_code_warnings() {
        String id = "CPD";
        Build build = createAndBuildFreeStyleJob(id, cpd -> cpd.setHighThreshold(2).setNormalThreshold(1),
                "duplicate_code/cpd.xml", "duplicate_code/Main.java");

        WarningsResultDetailsPage page = getWarningsResultDetailsPage(id, build);
        IssuesTable issuesTable = page.getIssuesTable();
        assertThat(issuesTable).hasSize(10);
    }

    /**
     * Verifies that clicking on the icon within the details column of the issues table, the row which shows the issues
     * details will be displayed or hidden.
     */
    @Test
    public void should_be_able_to_open_details_row() {
        String id = "CPD";
        Build build = createAndBuildFreeStyleJob(id, cpd -> cpd.setHighThreshold(2).setNormalThreshold(1),
                "duplicate_code/cpd.xml", "duplicate_code/Main.java");

        WarningsResultDetailsPage page = getWarningsResultDetailsPage(id, build);
        IssuesTable issuesTable = page.getIssuesTable();
        assertThat(issuesTable).hasSize(10);

        DryIssuesTableRow firstRow = issuesTable.getRowAs(0, DryIssuesTableRow.class);
        DryIssuesTableRow secondRow = issuesTable.getRowAs(1, DryIssuesTableRow.class);

        firstRow.toggleDetailsRow();
        assertThat(issuesTable).hasSize(11);

        DetailsTableRow newSecondRow = issuesTable.getRowAs(1, DetailsTableRow.class);
        assertThat(newSecondRow).hasDetails("public static void functionOne()\n"
                + "  {\n"
                + "    System.out.println(\"testfile for redundancy\");");
        assertThat(issuesTable.getRowAs(2, DryIssuesTableRow.class)).isEqualTo(secondRow);

        firstRow.toggleDetailsRow();
        assertThat(issuesTable).hasSize(10);
        assertThat(issuesTable.getRowAs(1, DryIssuesTableRow.class)).isEqualTo(secondRow);
    }

    /**
     * Verifies that the links to the source code view are working.
     */
    // TODO: replace with new {@link SourceCodeView}
    @Test
    public void should_be_able_to_open_the_source_code_page_by_clicking_the_links() {
        String id = "CPD";
        Build build = createAndBuildFreeStyleJob(id, cpd -> cpd.setHighThreshold(2).setNormalThreshold(1),
                "duplicate_code/cpd.xml", "duplicate_code/Main.java");
        WarningsResultDetailsPage page = getWarningsResultDetailsPage(id, build);
        IssuesTable issuesTable = page.getIssuesTable();

        SourceCodeView sourceCodeView = issuesTable.getRowAs(0, DryIssuesTableRow.class).clickOnFileLink();

        String fileName = "Main.java";
        assertThat(sourceCodeView.getFileName()).isEqualTo(fileName);

        issuesTable = page.getIssuesTable();
        DryIssuesTableRow firstRow = issuesTable.getRowAs(0, DryIssuesTableRow.class);

        int expectedAmountOfDuplications = 5;
        assertThat(firstRow.getDuplicatedIn()).hasSize(expectedAmountOfDuplications);

        for (int i = 0; i < expectedAmountOfDuplications; i++) {
            issuesTable = page.getIssuesTable();
            firstRow = issuesTable.getRowAs(0, DryIssuesTableRow.class);
            sourceCodeView = firstRow.clickOnDuplicatedInLink(i);
            assertThat(sourceCodeView.getFileName()).isEqualTo(fileName);
        }
    }

    /**
     * Verifies that the priority filter link is working.
     */
    @Test
    public void should_be_able_to_use_the_filter_links() {
        String id = "CPD";
        Build build = createAndBuildFreeStyleJob(id, cpd -> cpd.setHighThreshold(3).setNormalThreshold(2),
                "duplicate_code/cpd.xml", "duplicate_code/Main.java");

        WarningsResultDetailsPage page = getWarningsResultDetailsPage(id, build);
        IssuesTable issuesTable = page.getIssuesTable();

        DryIssuesTableRow firstRow = issuesTable.getRowAs(0, DryIssuesTableRow.class);
        assertThat(firstRow).hasPriority(HIGH_PRIORITY);
        WarningsResultDetailsPage highPriorityPage = firstRow.clickOnPriorityLink();
        highPriorityPage.getIssuesTable()
                .getTableRows()
                .stream()
                .map(row -> row.getAs(AbstractNonDetailsIssuesTableRow.class))
                .forEach(row -> assertThat(row).hasPriority(HIGH_PRIORITY));

        issuesTable = page.getIssuesTable();
        DryIssuesTableRow sixthRow = issuesTable.getRowAs(5, DryIssuesTableRow.class);
        assertThat(sixthRow).hasPriority(LOW_PRIORITY);

        WarningsResultDetailsPage lowPriorityPage = sixthRow.clickOnPriorityLink();
        lowPriorityPage.getIssuesTable()
                .getTableRows()
                .stream()
                .map(row -> row.getAs(AbstractNonDetailsIssuesTableRow.class))
                .forEach(row -> assertThat(row).hasPriority(LOW_PRIORITY));
    }

    /**
     * Creates and builds a FreestyleJob for a specific static analysis tool.
     *
     * @param toolName
     *         the name of the tool
     * @param configuration
     *         the configuration steps for the static analysis tool
     * @param resourcesToCopy
     *         the resources which shall be copied to the workspace
     *
     * @return the finished build
     */
    private Build createAndBuildFreeStyleJob(final String toolName, final Consumer<StaticAnalysisTool> configuration,
            final String... resourcesToCopy) {
        FreeStyleJob job = createFreeStyleJob(resourcesToCopy);
        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        recorder.setTool(toolName, configuration);
        job.save();

        return job.startBuild().waitUntilFinished();
    }

    /**
     * Opens the WarningsResultDetailsPage and returns the corresponding PageObject representing it.
     *
     * @param id
     *         the id of the static analysis tool
     * @param build
     *         the build
     *
     * @return the PageObject representing the WarningsResultDetailsPage
     */
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
            job.copyResource(WARNINGS_PLUGIN_PREFIX + resource);
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
        WarningsResultDetailsPage checkstyleDetails = getWarningsResultDetailsPage(CHECKSTYLE_ID, build);
        assertThat(checkstyleDetails.getTrendChart())
                .hasNewIssues(3)
                .hasFixedIssues(1)
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
        referenceBuild.open();

        SummaryPage referenceSummary = new SummaryPage(referenceBuild, false);
        referenceSummary.getSummaryBoxByName(ANALYSIS_ID).getTitleResultLink().click();
        WarningsResultDetailsPage referenceDetails = getWarningsResultDetailsPage(ANALYSIS_ID, referenceBuild);
        assertThat(referenceDetails.getTrendChart()).hasNewIssues(0).hasFixedIssues(0).hasOutstandingIssues(4);

        reconfigureJobWithResource(job, "build_status_test/build_02");

        Build build = job.startBuild().waitUntilFinished();

        build.open();

        SummaryPage summaryPage = new SummaryPage(build, true);

        SummaryBoxPageArea aggregatedSummary = summaryPage.getSummaryBoxByName(ANALYSIS_ID);
        assertThat(aggregatedSummary).hasSummary();

        String resultsFrom = summaryPage.getSummaryBoxByName(ANALYSIS_ID)
                .findResultEntryTextByNamePart("Static analysis results from");
        assertThat(resultsFrom).containsIgnoringCase("findbugs");
        assertThat(resultsFrom).containsIgnoringCase("pmd");
        assertThat(resultsFrom).containsIgnoringCase("checkstyle");

        summaryPage.getSummaryBoxByName(ANALYSIS_ID).getTitleResultLink().click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "analysisResult/");

        WarningsResultDetailsPage details = new WarningsResultDetailsPage(build, ANALYSIS_ID);
        assertThat(details.getTrendChart()).hasNewIssues(3).hasFixedIssues(2).hasOutstandingIssues(2);

        build.open();

        summaryPage.getSummaryBoxByName(ANALYSIS_ID).getTitleResultInfoLink().click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "analysisResult/info/");

        build.open();

        summaryPage.getSummaryBoxByName(ANALYSIS_ID).findClickableResultEntryByNamePart("3 new warnings").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "analysisResult/new/");

        build.open();

        summaryPage.getSummaryBoxByName(ANALYSIS_ID).findClickableResultEntryByNamePart("2 fixed warnings").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "analysisResult/fixed/");

        build.open();

        summaryPage.getSummaryBoxByName(ANALYSIS_ID).findClickableResultEntryByNamePart("Reference build").click();
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

        // Checks if the whole build is marked as failed (in the title)
        assertThat(summaryPage.getBuildState()).isEqualTo("Failed");

        // Checks if the issue parser boxes contain the expected quality gate states
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
     * Simple test to check that the console log shows that build was a failure when thresholds of quality gate have
     * been reached.
     */
    @Test
    public void should_log_failure__when_quality_gate_thresholds_are_reached() {
        FreeStyleJob job = createFreeStyleJob("/checkstyle-result.xml");
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

        WarningsResultDetailsPage page = getWarningsResultDetailsPage(ANALYSIS_ID, build);

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
     * Builds a FreeStyle build and that builds with a xml file and checks if the results shown in the MessageBox are as
     * expected.
     */
    @Test
    public void shouldBeOkIfContentsOfMsgBoxesAreCorrectForFreeStyleJob() {
        FreeStyleJob job = createFreeStyleJob(CHECKSTYLE_XML);
        job.addPublisher(IssuesRecorder.class, recorder -> recorder.setTool("CheckStyle", "**/checkstyle-result.xml"));
        job.save();
        Build build = job.startBuild().waitUntilFinished();

        MessageBox messageBox = new MessageBox(build, CHECKSTYLE_ID);
        messageBox.open();

        messageBox.getErrorMsgContent();
        assertThat(messageBox).hasErrorMessagesSize(11 + 1);
        assertThat(messageBox).containsErrorMessage("Can't resolve absolute paths for 11 files");

        messageBox.getInfoMsgContent();
        assertThat(messageBox).containsInfoMessage("-> found 1 file");
        assertThat(messageBox).containsInfoMessage("checkstyle-result.xml: found 11 issues");
        assertThat(messageBox).containsInfoMessage("Post processing issues on 'Master' with encoding 'UTF-8'");
        assertThat(messageBox).containsInfoMessage("Resolved absolute paths for 1 files");
        assertThat(messageBox).containsInfoMessage("11 unresolved");
        assertThat(messageBox).containsInfoMessage("Resolved package names of 1 affected files");
        assertThat(messageBox).containsInfoMessage(
                "Creating fingerprints for all affected code blocks to track issues over different builds");
        assertThat(messageBox).containsInfoMessage("No quality gates have been set - skipping");
    }
}

