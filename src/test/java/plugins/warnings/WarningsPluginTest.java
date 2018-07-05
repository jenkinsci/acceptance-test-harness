package plugins.warnings;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.AbstractNonDetailsIssuesTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.AnalysisResult;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.AnalysisSummary;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.AnalysisSummary.SummaryBoxPageArea;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.ConsoleLogView;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.DefaultWarningsTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.DetailsTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.DryIssuesTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.IssuesRecorder;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.IssuesRecorder.StaticAnalysisTool;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.IssuesTable;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.LogMessagesView;
import org.jenkinsci.test.acceptance.plugins.warnings.white_mountains.SourceView;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Test;

import static plugins.warnings.assertions.Assertions.*;

/**
 * Acceptance tests for the White Mountains release of the warnings plug-in.
 *
 * @author Frank Christian Geyer
 * @author Ullrich Hafner
 * @author Manuel Hampp
 * @author Anna-Maria Hardi
 * @author Elvira Hauer
 * @author Deniz Mardin
 * @author Stephan Plöderl
 * @author Alexander Praegla
 * @author Michaela Reitschuster
 * @author Arne Schöntag
 * @author Alexandra Wenzel
 * @author Nikolai Wohlgemuth
 */
@WithPlugins("warnings")
public class WarningsPluginTest extends AbstractJUnitTest {
    private static final String WARNINGS_PLUGIN_PREFIX = "/warnings_plugin/white-mountains/";

    private static final String CHECKSTYLE_ID = "checkstyle";
    private static final String ANALYSIS_ID = "analysis";
    private static final String CPD_ID = "cpd";

    private static final String HIGH_PRIORITY = "High";
    private static final String LOW_PRIORITY = "Low";

    private static final String CHECKSTYLE_XML = "checkstyle-result.xml";
    private static final String SOURCE_VIEW_FOLDER = WARNINGS_PLUGIN_PREFIX + "source-view/";

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String CPD_REPORT = "duplicate_code/cpd.xml";
    private static final String CPD_SOURCE_NAME = "Main.java";
    private static final String CPD_SOURCE_PATH = "duplicate_code/Main.java";
    private static final String PMD_ID = "pmd";
    private static final String FINDBUGS_ID = "findbugs";
    private static final String MAVEN_ID = "maven";
    private static final String NO_PACKAGE = "-";

    /**
     * Verifies that clicking on the icon within the details column of the issues table, the row which shows the issues
     * details will be displayed or hidden.
     */
    @Test
    public void should_be_able_to_open_details_row() {
        Build build = createAndBuildFreeStyleJob("CPD", cpd -> cpd.setHighThreshold(2).setNormalThreshold(1),
                CPD_REPORT, CPD_SOURCE_PATH);

        AnalysisResult result = openAnalysisResult(build, CPD_ID);
        IssuesTable issuesTable = result.openIssuesTable();
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
    @Test
    public void should_be_able_to_open_the_source_code_page_by_clicking_the_links() {
        String expectedSourceCode = toString(WARNINGS_PLUGIN_PREFIX + CPD_SOURCE_PATH);
        Build build = createAndBuildFreeStyleJob("CPD", cpd -> cpd.setHighThreshold(2).setNormalThreshold(1),
                CPD_REPORT, CPD_SOURCE_PATH);
        AnalysisResult result = openAnalysisResult(build, CPD_ID);
        IssuesTable issuesTable = result.openIssuesTable();

        SourceView sourceView = issuesTable.getRowAs(0, DryIssuesTableRow.class).clickOnFileLink();

        assertThat(sourceView).hasFileName(CPD_SOURCE_NAME);
        assertThat(sourceView).hasSourceCode(expectedSourceCode);

        issuesTable = result.openIssuesTable();
        DryIssuesTableRow firstRow = issuesTable.getRowAs(0, DryIssuesTableRow.class);

        int expectedAmountOfDuplications = 5;
        assertThat(firstRow.getDuplicatedIn()).hasSize(expectedAmountOfDuplications);

        for (int i = 0; i < expectedAmountOfDuplications; i++) {
            issuesTable = result.openIssuesTable();
            firstRow = issuesTable.getRowAs(0, DryIssuesTableRow.class);
            sourceView = firstRow.clickOnDuplicatedInLink(i);
            assertThat(sourceView.getFileName()).isEqualTo(CPD_SOURCE_NAME);
            assertThat(sourceView).hasSourceCode(expectedSourceCode);
        }
    }

    /**
     * Verifies that the priority filter link is working.
     */
    @Test
    public void should_be_able_to_use_the_filter_links() {
        Build build = createAndBuildFreeStyleJob("CPD", cpd -> cpd.setHighThreshold(3).setNormalThreshold(2),
                CPD_REPORT, CPD_SOURCE_PATH);

        AnalysisResult page = openAnalysisResult(build, CPD_ID);
        IssuesTable issuesTable = page.openIssuesTable();

        DryIssuesTableRow firstRow = issuesTable.getRowAs(0, DryIssuesTableRow.class);
        assertThat(firstRow).hasPriority(HIGH_PRIORITY);
        AnalysisResult highPriorityPage = firstRow.clickOnPriorityLink();
        highPriorityPage.openIssuesTable()
                .getTableRows()
                .stream()
                .map(row -> row.getAs(AbstractNonDetailsIssuesTableRow.class))
                .forEach(row -> assertThat(row).hasPriority(HIGH_PRIORITY));

        issuesTable = page.openIssuesTable();
        DryIssuesTableRow sixthRow = issuesTable.getRowAs(5, DryIssuesTableRow.class);
        assertThat(sixthRow).hasPriority(LOW_PRIORITY);

        AnalysisResult lowPriorityPage = sixthRow.clickOnPriorityLink();
        lowPriorityPage.openIssuesTable()
                .getTableRows()
                .stream()
                .map(row -> row.getAs(AbstractNonDetailsIssuesTableRow.class))
                .forEach(row -> assertThat(row).hasPriority(LOW_PRIORITY));
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

        Build build = buildJob(job);

        assertThat(build.getConsole()).contains(
                "Applying 2 filters on the set of 4 issues (3 issues have been removed, 1 issues will be published)");
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

        buildJob(job);

        reconfigureJobWithResource(job, "build_status_test/build_02");

        Build build = buildJob(job);
        build.open();

        AnalysisSummary analysisSummary = new AnalysisSummary(build);
        assertThat(analysisSummary.getSummaryBoxByName(CHECKSTYLE_ID)).hasSummary();
        assertThat(analysisSummary.getSummaryBoxByName(PMD_ID)).hasSummary();
        assertThat(analysisSummary.getSummaryBoxByName(FINDBUGS_ID)).hasSummary();

        AnalysisResult checkstyleDetails = analysisSummary.getSummaryBoxByName(CHECKSTYLE_ID).clickTitleLink();
        assertThat(checkstyleDetails.getTrendChart())
                .hasNewIssues(3)
                .hasFixedIssues(1)
                .hasOutstandingIssues(0);

        build.open();

        LogMessagesView logMessagesView = analysisSummary.getSummaryBoxByName(CHECKSTYLE_ID).clickInfoLink();
        assertThat(logMessagesView).containsInfoMessage("checkstyle-result.xml: found 3 issues");

        build.open();

        AnalysisResult newResult = analysisSummary.getSummaryBoxByName(CHECKSTYLE_ID).clickNewLink();
        assertThat(newResult.getTrendChart())
                .hasNewIssues(3)
                .hasFixedIssues(0)
                .hasOutstandingIssues(0);

        build.open();

        AnalysisResult referenceResult = analysisSummary.getSummaryBoxByName(CHECKSTYLE_ID).clickReferenceBuildLink();
        assertThat(referenceResult.getTrendChart())
                .hasNewIssues(0)
                .hasFixedIssues(0)
                .hasOutstandingIssues(1);

        build.open();

        String noWarningsResult = analysisSummary.getSummaryBoxByName(FINDBUGS_ID)
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

        Build referenceBuild = buildJob(job);
        referenceBuild.open();

        AnalysisSummary referenceSummary = new AnalysisSummary(referenceBuild);
        referenceSummary.getSummaryBoxByName(ANALYSIS_ID).getTitleResultLink().click();
        AnalysisResult referenceDetails = openAnalysisResult(referenceBuild, ANALYSIS_ID);
        assertThat(referenceDetails.getTrendChart()).hasNewIssues(0).hasFixedIssues(0).hasOutstandingIssues(4);

        reconfigureJobWithResource(job, "build_status_test/build_02");

        Build build = buildJob(job);

        build.open();

        AnalysisSummary analysisSummary = new AnalysisSummary(build);

        SummaryBoxPageArea aggregatedSummary = analysisSummary.getSummaryBoxByName(ANALYSIS_ID);
        assertThat(aggregatedSummary).hasSummary();

        String resultsFrom = analysisSummary.getSummaryBoxByName(ANALYSIS_ID)
                .findResultEntryTextByNamePart("Static analysis results from");
        assertThat(resultsFrom).containsIgnoringCase(FINDBUGS_ID);
        assertThat(resultsFrom).containsIgnoringCase(PMD_ID);
        assertThat(resultsFrom).containsIgnoringCase("checkstyle");

        analysisSummary.getSummaryBoxByName(ANALYSIS_ID).getTitleResultLink().click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "analysisResult/");

        AnalysisResult details = new AnalysisResult(build, ANALYSIS_ID);
        assertThat(details.getTrendChart()).hasNewIssues(3).hasFixedIssues(2).hasOutstandingIssues(2);

        build.open();

        analysisSummary.getSummaryBoxByName(ANALYSIS_ID).getTitleResultInfoLink().click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "analysisResult/info/");

        build.open();

        analysisSummary.getSummaryBoxByName(ANALYSIS_ID).findClickableResultEntryByNamePart("3 new warnings").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "analysisResult/new/");

        build.open();

        analysisSummary.getSummaryBoxByName(ANALYSIS_ID).findClickableResultEntryByNamePart("2 fixed warnings").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "analysisResult/fixed/");

        build.open();

        analysisSummary.getSummaryBoxByName(ANALYSIS_ID).findClickableResultEntryByNamePart("Reference build").click();
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

        Build build = buildJob(job);

        build.open();

        AnalysisSummary analysisSummary = new AnalysisSummary(build);

        assertThat(analysisSummary.getBuildState()).isEqualTo("Failed");
        assertThat(analysisSummary.getSummaryBoxByName("checkstyle")).hasQualityGateState("Success");
        assertThat(analysisSummary.getSummaryBoxByName(FINDBUGS_ID)).hasQualityGateState("Success");
        assertThat(analysisSummary.getSummaryBoxByName(PMD_ID)).hasQualityGateState("Failed");

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
     * Starts two builds with different configurations and checks the values of the new, fixed and outstanding issues of
     * the trend chart as well as the low, normal and high priorities of the priority chart.
     * Check the entries of the issues table.
     */
    @Test
    public void should_log_values_in_trend_and_priority_chart() {
        FreeStyleJob job = createFreeStyleJob("aggregation/checkstyle1.xml", "aggregation/checkstyle2.xml",
                "aggregation/pmd.xml");
        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle", "**/checkstyle1.xml");
            recorder.addTool("PMD", "**/pmd.xml");
            recorder.setEnabledForAggregation(true);
        });
        job.save();

        buildJob(job);

        job.editPublisher(IssuesRecorder.class, recorder -> recorder.setTool("CheckStyle", "**/checkstyle2.xml"));

        Build build = buildJob(job);
        build.open();

        AnalysisResult page = openAnalysisResult(build, ANALYSIS_ID);

        assertThat(page.getTrendChart())
                .hasNewIssues(3)
                .hasFixedIssues(2)
                .hasOutstandingIssues(5);

        assertThat(page.getPriorityChart())
                .hasLowPriority(1)
                .hasNormalPriority(2)
                .hasHighPriority(5);

        IssuesTable issuesTable = page.openIssuesTable();
        assertThat(issuesTable).hasSize(8);
        
        DefaultWarningsTableRow tableRow = issuesTable.getRowAs(0, DefaultWarningsTableRow.class);
        assertThat(tableRow.getFileName()).isEqualTo("ChangeSelectionAction.java");
        assertThat(tableRow.getLineNumber()).isEqualTo(14);
        assertThat(tableRow.getPackageName()).isEqualTo("com.avaloq.adt.env.internal.ui.actions.change");
        assertThat(tableRow.getCategoryName()).isEqualTo("Import Statement Rules");
        assertThat(tableRow.getTypeName()).isEqualTo("UnusedImports");
        assertThat(tableRow.getPriority()).isEqualTo("Normal");
        assertThat(tableRow.getAge()).isEqualTo(3);
    }

    /**
     * Runs a freestyle job and pipeline that publishes checkstyle warnings. Verifies the content of the info and error log view.
     */
    @Test
    public void should_show_info_and_error_messages() {
        Build build = createAndBuildFreeStyleJob("CheckStyle", CHECKSTYLE_XML);

        verifyInfoAndErrorMessages(build);

        buildJob(createPipeline(CHECKSTYLE_XML));

        verifyInfoAndErrorMessages(build);
    }

    private void verifyInfoAndErrorMessages(final Build build) {
        LogMessagesView logMessagesView = new LogMessagesView(build, CHECKSTYLE_ID);
        logMessagesView.open();

        assertThat(logMessagesView).hasErrorMessagesSize(11 + 1);
        assertThat(logMessagesView).containsErrorMessage("Can't resolve absolute paths for 11 files");

        assertThat(logMessagesView).containsInfoMessage("-> found 1 file");
        assertThat(logMessagesView).containsInfoMessage("checkstyle-result.xml: found 11 issues");
        assertThat(logMessagesView).containsInfoMessage("Post processing issues on 'Master' with encoding 'UTF-8'");
        assertThat(logMessagesView).containsInfoMessage("Resolved absolute paths for 1 files");
        assertThat(logMessagesView).containsInfoMessage("11 unresolved");
        assertThat(logMessagesView).containsInfoMessage("Resolved package names of 1 affected files");
        assertThat(logMessagesView).containsInfoMessage(
                "Creating fingerprints for all affected code blocks to track issues over different builds");
        assertThat(logMessagesView).containsInfoMessage("No quality gates have been set - skipping");
    }

    /**
     * Creates and builds a maven job and verifies that all warnings and errors are shown in the console log view.
     */
    @Test
    public void should_show_maven_warnings_in_console_log_view() {
        MavenModuleSet job = createMavenProject();
        copyResourceFilesToWorkspace(job, SOURCE_VIEW_FOLDER + "pom.xml");
        configureJob(job, "Maven", "");
        job.save();

        Build build = buildFailingJob(job);
        build.open();

        AnalysisSummary analysisSummary = new AnalysisSummary(build);
        assertThat(analysisSummary.getSummaryBoxByName(MAVEN_ID)).hasSummary();

        AnalysisResult mavenDetails = analysisSummary.getSummaryBoxByName(MAVEN_ID).clickTitleLink();
        assertThat(mavenDetails.getTrendChart())
                .hasNewIssues(0)
                .hasFixedIssues(0)
                .hasOutstandingIssues(5);
        assertThat(mavenDetails.getPriorityChart())
                .hasHighPriority(2)
                .hasNormalPriority(3);

        IssuesTable issuesTable = mavenDetails.openIssuesTable();

        DefaultWarningsTableRow firstRow = issuesTable.getRowAs(0, DefaultWarningsTableRow.class);
        ConsoleLogView sourceView = firstRow.openConsoleLog();
        assertThat(sourceView).hasTitle("Console Details");
        assertThat(sourceView).hasHighlightedText(
                "[WARNING] For this reason, future Maven versions might no longer support building such malformed projects."
                        + LINE_SEPARATOR + "[WARNING]");
    }

    /**
     * Verifies that package and namespace names are resolved.
     */
    @Test
    public void should_resolve_packages_and_namespaces() {
        MavenModuleSet job = createMavenProject();
        job.copyDir(job.resource(SOURCE_VIEW_FOLDER));
        configureJob(job, "Eclipse ECJ", "**/*Classes.txt");
        job.save();

        Build build = buildFailingJob(job);
        build.open();

        AnalysisSummary analysisSummary = new AnalysisSummary(build);
        assertThat(analysisSummary.getSummaryBoxByName("eclipse")).hasSummary();

        AnalysisResult result = analysisSummary.getSummaryBoxByName("eclipse").clickTitleLink();
        assertThat(result.getTrendChart()).hasOutstandingIssues(9);
        assertThat(result.getPriorityChart()).hasNormalPriority(9);


        LinkedHashMap<String, String> filesToPackages = new LinkedHashMap<>();
        filesToPackages.put("NOT_EXISTING_FILE", NO_PACKAGE);
        filesToPackages.put("SampleClassWithBrokenPackageNaming.java", NO_PACKAGE);
        filesToPackages.put("SampleClassWithNamespace.cs", "SampleClassWithNamespace");
        filesToPackages.put("SampleClassWithNamespaceBetweenCode.cs", "NestedNamespace");
        filesToPackages.put("SampleClassWithNestedAndNormalNamespace.cs", "SampleClassWithNestedAndNormalNamespace");
        filesToPackages.put("SampleClassWithoutNamespace.cs", NO_PACKAGE);
        filesToPackages.put("SampleClassWithoutPackage.java", NO_PACKAGE);
        filesToPackages.put("SampleClassWithPackage.java", "edu.hm.hafner.analysis._123.int.naming.structure");
        filesToPackages.put("SampleClassWithUnconventionalPackageNaming.java", NO_PACKAGE);

        int row = 0;
        for (Entry<String, String> fileToPackage : filesToPackages.entrySet()) {
            IssuesTable issuesTable = result.openIssuesTable();
            DefaultWarningsTableRow tableRow = issuesTable.getRowAs(row, DefaultWarningsTableRow.class); // TODO: create custom assertions
            String actualFileName = fileToPackage.getKey();
            assertThat(tableRow.getFileName()).as("File name in row %d", row).isEqualTo(actualFileName);
            assertThat(tableRow.getPackageName()).as("Package name in row %d", row).isEqualTo(fileToPackage.getValue());
            SourceView sourceView = tableRow.openFile();
            assertThat(sourceView).hasFileName(actualFileName);
            if (row == 0) {
                assertThat(sourceView).hasSourceCode("Content of file NOT_EXISTING_FILE" + LINE_SEPARATOR
                        + "Can't read file: java.io.FileNotFoundException: "
                        + "/NOT/EXISTING/PATH/TO/NOT_EXISTING_FILE (No such file or directory)");
            }
            else {
                String expectedSourceCode = toString(SOURCE_VIEW_FOLDER + actualFileName);
                assertThat(sourceView).hasSourceCode(expectedSourceCode);
            }
            row++;
        }
    }

    private WorkflowJob createPipeline(final String resourceToCopy) {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        String resource = job.copyResourceStep(WARNINGS_PLUGIN_PREFIX + resourceToCopy);
        job.script.set("node {\n" + resource.replace("\\", "\\\\")
                + "recordIssues enabledForFailure: true, tools: [[pattern: '', tool: [$class: 'CheckStyle']]]"
                + "}");
        job.sandbox.check();
        job.save();
        return job;
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
        FreeStyleJob job = createFreeStyleJob(toolName, configuration, resourcesToCopy);

        return buildJob(job);
    }

    /**
     * Creates a FreestyleJob for a specific static analysis tool.
     *
     * @param toolName
     *         the name of the tool
     * @param configuration
     *         the configuration steps for the static analysis tool
     * @param resourcesToCopy
     *         the resources which shall be copied to the workspace
     *
     * @return the created job
     */
    private FreeStyleJob createFreeStyleJob(final String toolName, final Consumer<StaticAnalysisTool> configuration,
            final String... resourcesToCopy) {
        FreeStyleJob job = createFreeStyleJob(resourcesToCopy);
        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        recorder.setTool(toolName, configuration);
        job.save();
        return job;
    }

    /**
     * Creates and builds a FreestyleJob for a specific static analysis tool.
     *
     * @param toolName
     *         the name of the tool
     * @param resourcesToCopy
     *         the resources which shall be copied to the workspace
     *
     * @return the finished build
     */
    private Build createAndBuildFreeStyleJob(final String toolName, final String... resourcesToCopy) {
        return createAndBuildFreeStyleJob(toolName, c -> {}, resourcesToCopy);
    }

    /**
     * Opens the AnalysisResult and returns the corresponding PageObject representing it.
     *
     * @param build
     *         the build
     * @param id
     *         the id of the static analysis tool
     *
     * @return the PageObject representing the AnalysisResult
     */
    private AnalysisResult openAnalysisResult(final Build build, final String id) {
        AnalysisResult resultPage = new AnalysisResult(build, id);
        resultPage.open();
        return resultPage;
    }

    private FreeStyleJob createFreeStyleJob(final String... resourcesToCopy) {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        for (String resource : resourcesToCopy) {
            job.copyResource(WARNINGS_PLUGIN_PREFIX + resource);
        }
        return job;
    }

    private MavenModuleSet createMavenProject() {
        MavenInstallation.installSomeMaven(jenkins);
        return jenkins.getJobs().create(MavenModuleSet.class);
    }

    private void configureJob(final MavenModuleSet job, final String toolName, final String pattern) {
        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        recorder.setToolWithPattern(toolName, pattern);
        recorder.openAdvancedOptions();
        recorder.deleteFilter();
        recorder.setEnabledForFailure(true);
    }

    private Build buildFailingJob(final Job job) {
        return buildJob(job).shouldFail();
    }

    private Build buildJob(final Job job) {
        return job.startBuild().waitUntilFinished();
    }

    private void copyResourceFilesToWorkspace(final Job job, final String... resources) {
        for (String file : resources) {
            job.copyResource(file);
        }
    }

    /**
     * Finds a resource with the given name and returns the content (decoded with UTF-8) as String.
     *
     * @param fileName
     *         name of the desired resource
     *
     * @return the content represented as {@link String}
     */
    private String toString(final String fileName) {
        return new String(readAllBytes(fileName), StandardCharsets.UTF_8);
    }

    /**
     * Reads the contents of the desired resource. The rules for searching resources associated with this test class are
     * implemented by the defining {@linkplain ClassLoader class loader} of this test class.  This method delegates to
     * this object's class loader.  If this object was loaded by the bootstrap class loader, the method delegates to
     * {@link ClassLoader#getSystemResource}.
     * <p>
     * Before delegation, an absolute resource name is constructed from the given resource name using this algorithm:
     * <p>
     * <ul>
     * <li> If the {@code name} begins with a {@code '/'} (<tt>'&#92;u002f'</tt>), then the absolute name of the
     * resource is the portion of the {@code name} following the {@code '/'}.</li>
     * <li> Otherwise, the absolute name is of the following form:
     * <blockquote> {@code modified_package_name/name} </blockquote>
     * <p> Where the {@code modified_package_name} is the package name of this object with {@code '/'}
     * substituted for {@code '.'} (<tt>'&#92;u002e'</tt>).</li>
     * </ul>
     *
     * @param fileName
     *         name of the desired resource
     *
     * @return the content represented by a byte array
     */
    private byte[] readAllBytes(final String fileName) {
        try {
            return Files.readAllBytes(getPath(fileName));
        }
        catch (IOException | URISyntaxException e) {
            throw new AssertionError("Can't read resource " + fileName, e);
        }
    }

    private Path getPath(final String name) throws URISyntaxException {
        URL resource = getClass().getResource(name);
        if (resource == null) {
            throw new AssertionError("Can't find resource " + name);
        }
        return Paths.get(resource.toURI());
    }
}

