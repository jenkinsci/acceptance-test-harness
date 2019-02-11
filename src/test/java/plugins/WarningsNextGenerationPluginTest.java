package plugins;

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

import org.junit.Test;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.AbstractNonDetailsIssuesTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.AnalysisResult;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.AnalysisSummary;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.ConsoleLogView;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.DefaultWarningsTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.DetailsTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.DryIssuesTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.IssuesRecorder;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.IssuesRecorder.QualityGateType;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.IssuesRecorder.StaticAnalysisTool;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.IssuesTable;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.LogMessagesView;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.SourceView;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.WorkflowJob;

import static org.jenkinsci.test.acceptance.plugins.warnings_ng.Assertions.*;

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
@WithPlugins("warnings-ng")
public class WarningsNextGenerationPluginTest extends AbstractJUnitTest {
    private static final String WARNINGS_PLUGIN_PREFIX = "/warnings_ng_plugin/";

    private static final String CHECKSTYLE_ID = "checkstyle";
    private static final String ANALYSIS_ID = "analysis";
    private static final String CPD_ID = "cpd";

    private static final String HIGH_PRIORITY = "High";
    private static final String LOW_PRIORITY = "Low";

    private static final String CHECKSTYLE_XML = "checkstyle-result.xml";
    private static final String SOURCE_VIEW_FOLDER = WARNINGS_PLUGIN_PREFIX + "source-view/";

    private static final String CPD_REPORT = "duplicate_code/cpd.xml";
    private static final String CPD_SOURCE_NAME = "Main.java";
    private static final String CPD_SOURCE_PATH = "duplicate_code/Main.java";
    private static final String PMD_ID = "pmd";
    private static final String FINDBUGS_ID = "findbugs";
    private static final String MAVEN_ID = "maven";
    private static final String NO_PACKAGE = "-";

    /**
     * Runs a pipeline job with checkstyle and pmd. Verifies the expansion of tokens with the token-macro plugin.
     */
    @Test @WithPlugins("token-macro")
    public void should_expand_token() {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        String checkstyle = job.copyResourceStep(WARNINGS_PLUGIN_PREFIX + "aggregation/checkstyle1.xml");
        String pmd = job.copyResourceStep(WARNINGS_PLUGIN_PREFIX + "aggregation/pmd.xml");
        job.script.set("node {\n"
                + checkstyle.replace("\\", "\\\\")
                + pmd.replace("\\", "\\\\")
                + "recordIssues tool: checkStyle(pattern: '**/checkstyle*')\n"
                + "recordIssues tool: pmdParser(pattern: '**/pmd*')\n"
                + "def total = tm('${ANALYSIS_ISSUES_COUNT}')\n"
                + "echo '[total=' + total + ']' \n"
                + "def checkstyle = tm('${ANALYSIS_ISSUES_COUNT, tool=\"checkstyle\"}')\n"
                + "echo '[checkstyle=' + checkstyle + ']' \n"
                + "def pmd = tm('${ANALYSIS_ISSUES_COUNT, tool=\"pmd\"}')\n"
                + "echo '[pmd=' + pmd + ']' \n"
                + "}");
        job.sandbox.check();
        job.save();

        Build build = buildJob(job);

        assertThat(build.getConsole()).contains("[total=7]");
        assertThat(build.getConsole()).contains("[checkstyle=3]");
        assertThat(build.getConsole()).contains("[pmd=4]");
    }

    /**
     * Verifies that clicking on the (+) icon within the details column of the issues table will show and hide
     * the details child row.
     */
    @Test
    public void should_be_able_to_open_and_hide_details_row() {
        Build build = createAndBuildFreeStyleJob("CPD", cpd -> cpd.setHighThreshold(2).setNormalThreshold(1),
                CPD_REPORT, CPD_SOURCE_PATH);

        AnalysisResult result = openAnalysisResult(build, CPD_ID);
        IssuesTable issuesTable = result.openIssuesTable();
        assertThat(issuesTable).hasSize(10);

        DryIssuesTableRow firstRow = issuesTable.getRowAs(0, DryIssuesTableRow.class);
        DryIssuesTableRow secondRow = issuesTable.getRowAs(1, DryIssuesTableRow.class);

        firstRow.toggleDetailsRow();
        assertThat(issuesTable).hasSize(11);

        DetailsTableRow detailsRow = issuesTable.getRowAs(1, DetailsTableRow.class);
        assertThat(detailsRow).hasDetails("Found duplicated code.\npublic static void functionOne()\n"
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
     * Verifies that the priority filter link is working and opens a new details page with the correct priorities only.
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

        AnalysisResult result = openAnalysisResult(build, "checkstyle");

        IssuesTable issuesTable = result.openIssuesTable();
        assertThat(issuesTable).hasSize(1);
    }

    /**
     * Tests the build overview page by running two builds with three issue parsers enabled. Checks the contents
     * of the result summary boxes for each parser.
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

        AnalysisSummary checkstyle = new AnalysisSummary(build, CHECKSTYLE_ID);
        assertThat(checkstyle).isDisplayed();
        assertThat(checkstyle).hasTitleText("CheckStyle: 3 warnings");

        AnalysisSummary pmd = new AnalysisSummary(build, PMD_ID);
        assertThat(pmd).isDisplayed();
        assertThat(pmd).hasTitleText("PMD: 2 warnings");

        AnalysisSummary findBugs = new AnalysisSummary(build, FINDBUGS_ID);
        assertThat(findBugs).isDisplayed();
        assertThat(findBugs).hasTitleText("FindBugs: No warnings");

        AnalysisResult checkstyleDetails = new AnalysisSummary(build, CHECKSTYLE_ID).clickTitleLink();
//        assertThat(checkstyleDetails.getTrendChart())
//                .hasNewIssues(3)
//                .hasFixedIssues(1)
//                .hasOutstandingIssues(0);

        build.open();

        LogMessagesView logMessagesView = new AnalysisSummary(build, CHECKSTYLE_ID).clickInfoLink();
        assertThat(logMessagesView).hasInfoMessages(
                "-> found 1 file",
                "-> found 3 issues (skipped 0 duplicates)");

        build.open();

        AnalysisResult newResult = new AnalysisSummary(build, CHECKSTYLE_ID).clickNewLink();
//        assertThat(newResult.getTrendChart())
//                .hasNewIssues(3)
//                .hasFixedIssues(0)
//                .hasOutstandingIssues(0);

        build.open();

        AnalysisResult referenceResult = new AnalysisSummary(build, CHECKSTYLE_ID).clickReferenceBuildLink();
//        assertThat(referenceResult.getTrendChart())
//                .hasNewIssues(0)
//                .hasFixedIssues(0)
//                .hasOutstandingIssues(1);

        build.open();

        String noWarningsResult = new AnalysisSummary(build, FINDBUGS_ID)
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

        AnalysisSummary referenceSummary = new AnalysisSummary(referenceBuild, ANALYSIS_ID);
        referenceSummary.getTitleResultLink().click();
        AnalysisResult referenceDetails = openAnalysisResult(referenceBuild, ANALYSIS_ID);

        // assertThat(referenceDetails.getTrendChart()).hasNewIssues(0).hasFixedIssues(0).hasOutstandingIssues(4);

        reconfigureJobWithResource(job, "build_status_test/build_02");

        Build build = buildJob(job);

        build.open();

        AnalysisSummary analysisSummary = new AnalysisSummary(build, ANALYSIS_ID);

        String resultsFrom = analysisSummary.findResultEntryTextByNamePart("Static analysis results from");
        assertThat(resultsFrom).containsIgnoringCase(FINDBUGS_ID);
        assertThat(resultsFrom).containsIgnoringCase(PMD_ID);
        assertThat(resultsFrom).containsIgnoringCase("checkstyle");

        analysisSummary.getTitleResultLink().click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "analysis/");

        AnalysisResult details = new AnalysisResult(build, ANALYSIS_ID);
        // assertThat(details.getTrendChart()).hasNewIssues(3).hasFixedIssues(2).hasOutstandingIssues(2);

        build.open();

        analysisSummary = new AnalysisSummary(build, ANALYSIS_ID);
        analysisSummary.getTitleResultInfoLink().click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "analysis/info/");

        build.open();

        analysisSummary.findClickableResultEntryByNamePart("3 new warnings").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "analysis/new/");

        build.open();

        analysisSummary.findClickableResultEntryByNamePart("2 fixed warnings").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build.url + "analysis/fixed/");

        build.open();

        analysisSummary.findClickableResultEntryByNamePart("Reference build").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(referenceBuild.url + "analysis/");
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
        recorder.addQualityGateConfiguration(2, QualityGateType.TOTAL, true);
        job.save();

        Build build = buildJob(job);

        build.open();

        assertThat(new AnalysisSummary(build, CHECKSTYLE_ID)).hasQualityGateResult("Success");

        // build: assertThat(analysisSummary.getBuildState()).isEqualTo("Failed");
        assertThat(new AnalysisSummary(build, CHECKSTYLE_ID)).hasQualityGateResult("Success");
        assertThat(new AnalysisSummary(build, FINDBUGS_ID)).hasQualityGateResult("Success");
        assertThat(new AnalysisSummary(build, PMD_ID)).hasQualityGateResult("Failed");

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
     * the trend chart as well as the low, normal and high priorities of the priority chart. Check the entries of the
     * issues table.
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

//        assertThat(page.getTrendChart())
//                .hasNewIssues(3)
//                .hasFixedIssues(2)
//                .hasOutstandingIssues(5);

//        assertThat(page.getPriorityChart())
//                .hasLowPriority(1)
//                .hasNormalPriority(2)
//                .hasHighPriority(5);

        IssuesTable issuesTable = page.openIssuesTable();
        assertThat(issuesTable).hasSize(8);

        DefaultWarningsTableRow tableRow = issuesTable.getRowAs(0, DefaultWarningsTableRow.class);
        assertThat(tableRow.getFileName()).isEqualTo("ChangeSelectionAction.java");
        assertThat(tableRow.getLineNumber()).isEqualTo(14);
        assertThat(tableRow.getPackageName()).isEqualTo("com.avaloq.adt.env.internal.ui.actions.change");
        assertThat(tableRow.getCategoryName()).isEqualTo("Import Statement Rules");
        assertThat(tableRow.getTypeName()).isEqualTo("UnusedImports");
        assertThat(tableRow.getPriority()).isEqualTo("Normal");
        assertThat(tableRow.getAge()).isEqualTo(2);
    }

    /**
     * Runs a freestyle job and pipeline that publishes checkstyle warnings. Verifies the content of the info and error
     * log view.
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

        assertThat(logMessagesView.getErrorMessages()).hasSize(1 + 1 + 1 + 11);
        assertThat(logMessagesView).hasErrorMessages("Can't resolve absolute paths for some files",
                "Can't create fingerprints for some files");

        assertThat(logMessagesView).hasInfoMessages(
                "-> found 1 file",
                "-> found 11 issues (skipped 0 duplicates)",
                "Post processing issues on 'Master' with encoding 'UTF-8'",
                "-> 0 resolved, 1 unresolved, 0 already resolved",
                "-> 0 copied, 0 not in workspace, 1 not-found, 0 with I/O error",
                "-> resolved module names for 11 issues",
                "-> resolved package names of 1 affected files",
                "-> created fingerprints for 0 issues",
                "No valid reference build found that meets the criteria (NO_JOB_FAILURE - SUCCESSFUL_QUALITY_GATE)",
                "All reported issues will be considered outstanding",
                "No quality gates have been set - skipping",
                "Health report is disabled - skipping");
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

        AnalysisSummary analysisSummary = new AnalysisSummary(build, MAVEN_ID);

        AnalysisResult mavenDetails = analysisSummary.clickTitleLink();
//        assertThat(mavenDetails.getTrendChart())
//                .hasNewIssues(0)
//                .hasFixedIssues(0)
//                .hasOutstandingIssues(5);
//        assertThat(mavenDetails.getPriorityChart())
//                .hasHighPriority(2)
//                .hasNormalPriority(3);

        IssuesTable issuesTable = mavenDetails.openIssuesTable();

        DefaultWarningsTableRow firstRow = issuesTable.getRowAs(0, DefaultWarningsTableRow.class);
        ConsoleLogView sourceView = firstRow.openConsoleLog();
        assertThat(sourceView).hasTitle("Console Details");
        assertThat(sourceView).hasHighlightedText("[WARNING]\n"
                + "[WARNING] Some problems were encountered while building the effective model for edu.hm.hafner.irrelevant.groupId:random-artifactId:jar:1.0\n"
                + "[WARNING] 'build.plugins.plugin.version' for org.apache.maven.plugins:maven-compiler-plugin is missing. @ line 13, column 15\n"
                + "[WARNING]\n"
                + "[WARNING] It is highly recommended to fix these problems because they threaten the stability of your build.\n"
                + "[WARNING]\n"
                + "[WARNING] For this reason, future Maven versions might no longer support building such malformed projects.\n"
                + "[WARNING]");
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

        AnalysisSummary analysisSummary = new AnalysisSummary(build, "eclipse");

        AnalysisResult result = analysisSummary.clickTitleLink();
//        assertThat(result.getTrendChart()).hasOutstandingIssues(9);
//        assertThat(result.getPriorityChart()).hasNormalPriority(9);

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
            DefaultWarningsTableRow tableRow = issuesTable.getRowAs(row,
                    DefaultWarningsTableRow.class); // TODO: create custom assertions
            String actualFileName = fileToPackage.getKey();
            assertThat(tableRow.getFileName()).as("File name in row %d", row).isEqualTo(actualFileName);
            assertThat(tableRow.getPackageName()).as("Package name in row %d", row).isEqualTo(fileToPackage.getValue());
            if (row == 0) {
                // TODO: validate
            }
            else {
                SourceView sourceView = tableRow.openFile();
                assertThat(sourceView).hasFileName(actualFileName);
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
        return createAndBuildFreeStyleJob(toolName, c -> {
        }, resourcesToCopy);
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

