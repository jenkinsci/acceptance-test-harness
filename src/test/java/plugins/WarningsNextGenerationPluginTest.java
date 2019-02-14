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
import org.jenkinsci.test.acceptance.plugins.warnings_ng.AnalysisResult.Tab;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.AnalysisSummary;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.AnalysisSummary.InfoType;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.AnalysisSummary.QualityGateResult;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.ConsoleLogView;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.DefaultWarningsTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.DetailsTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.DryIssuesTableRow;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.InfoView;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.IssuesRecorder;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.IssuesRecorder.QualityGateType;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.IssuesRecorder.StaticAnalysisTool;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.IssuesTable;
import org.jenkinsci.test.acceptance.plugins.warnings_ng.SourceView;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Build.Result;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.WorkflowJob;

import static org.jenkinsci.test.acceptance.plugins.warnings_ng.Assertions.*;

/**
 * Acceptance tests for the Warnings Next Generation Plugin.
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
     * Runs a pipeline with checkstyle and pmd. Verifies the expansion of tokens with the token-macro plugin.
     */
    @Test @WithPlugins({"token-macro", "workflow-cps", "pipeline-stage-step", "workflow-durable-task-step", "workflow-basic-steps"})
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
     * Tests the build overview page by running two builds with three different tools enabled. Checks the contents
     * of the result summaries for each tool.
     */
    @Test
    public void should_show_build_summary_and_link_to_details() {
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_01");
        addRecorderWith3Tools(job);
        job.save();

        buildJob(job);

        reconfigureJobWithResource(job, "build_status_test/build_02");

        Build build = buildJob(job);

        build.open();
        AnalysisSummary checkstyle = new AnalysisSummary(build, CHECKSTYLE_ID);
        assertThat(checkstyle).isDisplayed();
        assertThat(checkstyle).hasTitleText("CheckStyle: 3 warnings");
        assertThat(checkstyle).hasNewSize(3);
        assertThat(checkstyle).hasFixedSize(1);
        assertThat(checkstyle).hasReferenceBuild(1);
        assertThat(checkstyle).hasInfoType(InfoType.ERROR);

        AnalysisResult checkstyleDetails = checkstyle.openOverallResult();
        assertThat(checkstyleDetails).hasActiveTab(Tab.CATEGORIES);
        assertThat(checkstyleDetails).hasTotal(3);
        assertThat(checkstyleDetails).hasOnlyAvailableTabs(Tab.CATEGORIES, Tab.TYPES, Tab.ISSUES);

        build.open();
        AnalysisSummary pmd = new AnalysisSummary(build, PMD_ID);
        assertThat(pmd).isDisplayed();
        assertThat(pmd).hasTitleText("PMD: 2 warnings");
        assertThat(pmd).hasNewSize(0);
        assertThat(pmd).hasFixedSize(1);
        assertThat(pmd).hasReferenceBuild(1);
        assertThat(pmd).hasInfoType(InfoType.ERROR);

        AnalysisResult pmdDetails = pmd.openOverallResult();
        assertThat(pmdDetails).hasActiveTab(Tab.CATEGORIES);
        assertThat(pmdDetails).hasTotal(2);
        assertThat(pmdDetails).hasOnlyAvailableTabs(Tab.CATEGORIES, Tab.TYPES, Tab.ISSUES);

        build.open();
        AnalysisSummary findBugs = new AnalysisSummary(build, FINDBUGS_ID);
        assertThat(findBugs).isDisplayed();
        assertThat(findBugs).hasTitleText("FindBugs: No warnings");
        assertThat(findBugs).hasNewSize(0);
        assertThat(findBugs).hasFixedSize(0);
        assertThat(findBugs).hasReferenceBuild(1);
        assertThat(findBugs).hasInfoType(InfoType.INFO);
        assertThat(findBugs).hasDetails("No warnings for 2 builds, i.e. since build 1");

        build.open();
        assertThat(new AnalysisSummary(build, CHECKSTYLE_ID).openInfoView()).hasInfoMessages(
                "-> found 1 file",
                "-> found 3 issues (skipped 0 duplicates)",
                "Issues delta (vs. reference build): outstanding: 0, new: 3, fixed: 1");

        build.open();
        assertThat(new AnalysisSummary(build, PMD_ID).openInfoView()).hasInfoMessages(
                "-> found 1 file",
                "-> found 2 issues (skipped 0 duplicates)",
                "Issues delta (vs. reference build): outstanding: 2, new: 0, fixed: 1");

        build.open();
        assertThat(new AnalysisSummary(build, FINDBUGS_ID).openInfoView()).hasInfoMessages(
                "-> found 1 file",
                "-> found 0 issues (skipped 0 duplicates)",
                "Issues delta (vs. reference build): outstanding: 0, new: 0, fixed: 0");
    }

    /**
     * Tests the build overview page by running two builds that aggregate the three different tools into a single result. Checks the contents
     * of the result summary.
     */
    @Test
    public void should_aggregate_tools_into_single_result() {
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_01");
        IssuesRecorder recorder = addRecorderWith3Tools(job);
        recorder.setEnabledForAggregation(true);
        job.save();

        Build referenceBuild = buildJob(job);
        referenceBuild.open();

        assertThat(new AnalysisSummary(referenceBuild, CHECKSTYLE_ID)).isNotDisplayed();
        assertThat(new AnalysisSummary(referenceBuild, PMD_ID)).isNotDisplayed();
        assertThat(new AnalysisSummary(referenceBuild, FINDBUGS_ID)).isNotDisplayed();

        AnalysisSummary referenceSummary = new AnalysisSummary(referenceBuild, ANALYSIS_ID);
        assertThat(referenceSummary).isDisplayed();
        assertThat(referenceSummary).hasTitleText("Static Analysis: 4 warnings");
        assertThat(referenceSummary).hasAggregation("FindBugs, CheckStyle, PMD");
        assertThat(referenceSummary).hasNewSize(0);
        assertThat(referenceSummary).hasFixedSize(0);
        assertThat(referenceSummary).hasReferenceBuild(0);

        AnalysisResult referenceDetails = referenceSummary.openOverallResult();
        // assertThat(referenceDetails.getTrendChart()).hasNewIssues(0).hasFixedIssues(0).hasOutstandingIssues(4);

        reconfigureJobWithResource(job, "build_status_test/build_02");

        Build build = buildJob(job);

        build.open();

        AnalysisSummary analysisSummary = new AnalysisSummary(build, ANALYSIS_ID);
        assertThat(analysisSummary).isDisplayed();
        assertThat(analysisSummary).hasTitleText("Static Analysis: 5 warnings");
        assertThat(analysisSummary).hasAggregation("FindBugs, CheckStyle, PMD");
        assertThat(analysisSummary).hasNewSize(3);
        assertThat(analysisSummary).hasFixedSize(2);
        assertThat(analysisSummary).hasReferenceBuild(1);

        AnalysisResult result = analysisSummary.openOverallResult();
        assertThat(result).hasActiveTab(Tab.TOOLS);
        assertThat(result).hasTotal(5);
        assertThat(result).hasOnlyAvailableTabs(Tab.TOOLS, Tab.PACKAGES, Tab.FILES, Tab.CATEGORIES, Tab.TYPES, Tab.ISSUES);
    }

    /**
     * Verifies that the quality gate is evaluated and changes the result of the build to UNSTABLE or FAILED.
     */
    @Test
    public void should_change_build_result_if_quality_gate_is_not_passed() {
        runJobWithQualityGate(false);
        runJobWithQualityGate(true);
    }

    private void runJobWithQualityGate(final boolean isUnstable) {
        FreeStyleJob job = createFreeStyleJob("build_status_test/build_01");
        IssuesRecorder recorder = addRecorderWith3Tools(job);
        recorder.addQualityGateConfiguration(2, QualityGateType.TOTAL, isUnstable);
        job.save();

        Build build = buildJob(job).shouldBe(isUnstable ? Result.UNSTABLE : Result.FAILURE);
        build.open();

        assertThat(new AnalysisSummary(build, CHECKSTYLE_ID)).hasQualityGateResult(QualityGateResult.SUCCESS);
        assertThat(new AnalysisSummary(build, FINDBUGS_ID)).hasQualityGateResult(QualityGateResult.SUCCESS);
        assertThat(new AnalysisSummary(build, PMD_ID))
                .hasQualityGateResult(isUnstable ? QualityGateResult.UNSTABLE : QualityGateResult.FAILED);
    }

    /**
     * Verifies that clicking on the (+) icon within the details column of the issues table will show and hide
     * the details child row.
     */
    @Test
    public void should_open_and_hide_details_row() {
        Build build = createAndBuildFreeStyleJob("CPD",
                cpd -> cpd.setHighThreshold(2).setNormalThreshold(1),
                CPD_REPORT, CPD_SOURCE_PATH);

        AnalysisResult result = openAnalysisResult(build, CPD_ID);
        IssuesTable issuesTable = result.openIssuesTable();
        assertThat(issuesTable).hasSize(10);

        DryIssuesTableRow firstRow = issuesTable.getRowAs(0, DryIssuesTableRow.class);
        DryIssuesTableRow secondRow = issuesTable.getRowAs(1, DryIssuesTableRow.class);

        firstRow.toggleDetailsRow();
        assertThat(issuesTable).hasSize(11);

        DetailsTableRow detailsRow = issuesTable.getRowAs(1, DetailsTableRow.class);
        assertThat(detailsRow).hasDetails(
                "Found duplicated code.\npublic static void functionOne()\n"
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
    public void should_open_source_code_view_from_issues_table() {
        Build build = createAndBuildFreeStyleJob("CPD", cpd -> cpd.setHighThreshold(2).setNormalThreshold(1),
                CPD_REPORT, CPD_SOURCE_PATH);
        AnalysisResult result = openAnalysisResult(build, CPD_ID);
        IssuesTable issuesTable = result.openIssuesTable();

        SourceView sourceView = issuesTable.getRowAs(0, DryIssuesTableRow.class).clickOnFileLink();
        assertThat(sourceView).hasFileName(CPD_SOURCE_NAME);

        String expectedSourceCode = toString(WARNINGS_PLUGIN_PREFIX + CPD_SOURCE_PATH);
        assertThat(sourceView.getSourceCode()).isEqualToIgnoringWhitespace(expectedSourceCode);

        issuesTable = result.openIssuesTable();
        DryIssuesTableRow firstRow = issuesTable.getRowAs(0, DryIssuesTableRow.class);

        int expectedAmountOfDuplications = 5;
        assertThat(firstRow.getDuplicatedIn()).hasSize(expectedAmountOfDuplications);

        for (int i = 0; i < expectedAmountOfDuplications; i++) {
            issuesTable = result.openIssuesTable();
            firstRow = issuesTable.getRowAs(0, DryIssuesTableRow.class);
            sourceView = firstRow.clickOnDuplicatedInLink(i);
            assertThat(sourceView).hasFileName(CPD_SOURCE_NAME);
            assertThat(sourceView.getSourceCode()).isEqualToIgnoringWhitespace(expectedSourceCode);
        }
    }

    /**
     * Verifies that the severity links in the issues table filter the results by the selected severity.
     */
    @Test
    public void should_filter_results_by_severity() {
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
    public void should_filter_issues_by_include_and_exclude_filters() {
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

    private void reconfigureJobWithResource(final FreeStyleJob job, final String fileName) {
        job.configure(() -> job.copyResource(WARNINGS_PLUGIN_PREFIX + fileName));
    }

    private IssuesRecorder addRecorderWith3Tools(final FreeStyleJob job) {
        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);

        recorder.setTool("CheckStyle");
        recorder.addTool("FindBugs");
        recorder.addTool("PMD");
        recorder.openAdvancedOptions();
        recorder.setEnabledForFailure(true);

        return recorder;
    }

    /**
     * Starts two builds with different configurations and checks the entries of the issues table.
     */
    @Test
    public void should_show_details_in_issues_table() {
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

        IssuesTable issuesTable = page.openIssuesTable();
        assertThat(issuesTable).hasSize(8);

        DefaultWarningsTableRow tableRow = issuesTable.getRowAs(0, DefaultWarningsTableRow.class);
        assertThat(tableRow).hasFileName("ChangeSelectionAction.java");
        assertThat(tableRow).hasLineNumber(14);
        assertThat(tableRow).hasPackageName("com.avaloq.adt.env.internal.ui.actions.change");
        assertThat(tableRow).hasCategoryName("Import Statement Rules");
        assertThat(tableRow).hasTypeName("UnusedImports");
        assertThat(tableRow).hasPriority("Normal");
        assertThat(tableRow).hasAge(2);
    }

    /**
     * Runs a freestyle job and pipeline that publishes checkstyle warnings. Verifies the content of the info and error
     * log view.
     */
    @Test
    public void should_show_info_and_error_messages() {
        Build build = createAndBuildFreeStyleJob("CheckStyle", CHECKSTYLE_XML);
        verifyInfoAndErrorMessages(build);

        Build pipeline = buildJob(createPipelineWithCheckStyle(CHECKSTYLE_XML));
        verifyInfoAndErrorMessages(pipeline);
    }

    private void verifyInfoAndErrorMessages(final Build build) {
        InfoView infoView = new InfoView(build, CHECKSTYLE_ID);
        infoView.open();

        assertThat(infoView.getErrorMessages()).hasSize(1 + 1 + 1 + 11);
        assertThat(infoView).hasErrorMessages("Can't resolve absolute paths for some files:",
                "Can't create fingerprints for some files:");

        assertThat(infoView).hasInfoMessages(
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
     * Creates and builds a maven job and verifies that all warnings are shown in the summary and details views.
     */
    @Test @WithPlugins("maven-plugin")
    public void should_show_maven_warnings_in_maven_project() {
        MavenModuleSet job = createMavenProject();
        copyResourceFilesToWorkspace(job, SOURCE_VIEW_FOLDER + "pom.xml");
        configureJob(job, "Maven", "");
        job.save();

        Build build = buildFailingJob(job);
        build.open();

        AnalysisSummary summary = new AnalysisSummary(build, MAVEN_ID);
        assertThat(summary).isDisplayed();
        assertThat(summary).hasTitleText("Maven: 2 warnings");
        assertThat(summary).hasNewSize(0);
        assertThat(summary).hasFixedSize(0);
        assertThat(summary).hasReferenceBuild(0);

        AnalysisResult mavenDetails = summary.openOverallResult();
        assertThat(mavenDetails).hasActiveTab(Tab.TYPES);
        assertThat(mavenDetails).hasTotal(2);
        assertThat(mavenDetails).hasOnlyAvailableTabs(Tab.TYPES, Tab.ISSUES);

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
    @Test @WithPlugins("maven-plugin")
    public void should_resolve_packages_and_namespaces() {
        MavenModuleSet job = createMavenProject();
        job.copyDir(job.resource(SOURCE_VIEW_FOLDER));
        configureJob(job, "Eclipse ECJ", "**/*Classes.txt");
        job.save();

        Build build = buildFailingJob(job);
        build.open();

        AnalysisSummary analysisSummary = new AnalysisSummary(build, "eclipse");
        AnalysisResult result = analysisSummary.openOverallResult();
        assertThat(result).hasActiveTab(Tab.MODULES);
        assertThat(result).hasTotal(9);
        assertThat(result).hasOnlyAvailableTabs(Tab.MODULES, Tab.PACKAGES, Tab.FILES, Tab.ISSUES);

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
            if (row != 0) { // first row has no file attached
                SourceView sourceView = tableRow.openFile();
                assertThat(sourceView).hasFileName(actualFileName);
                String expectedSourceCode = toString(SOURCE_VIEW_FOLDER + actualFileName);
                assertThat(sourceView.getSourceCode()).isEqualToIgnoringWhitespace(expectedSourceCode);
            }
            row++;
        }
    }

    private WorkflowJob createPipelineWithCheckStyle(final String resourceToCopy) {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        String resource = job.copyResourceStep(WARNINGS_PLUGIN_PREFIX + resourceToCopy);
        job.script.set("node {\n"
                + resource.replace("\\", "\\\\")
                + "recordIssues enabledForFailure: true, tool: checkStyle()"
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
        return createAndBuildFreeStyleJob(toolName, c -> { }, resourcesToCopy);
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

