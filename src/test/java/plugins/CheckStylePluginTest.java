package plugins;

import java.util.SortedMap;
import java.util.TreeMap;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisConfigurator;
import org.jenkinsci.test.acceptance.plugins.analysis_core.NullConfigurator;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckStyleAction;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckStyleFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckStyleMavenSettings;
import org.jenkinsci.test.acceptance.plugins.envinject.EnvInjectConfig;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.plugins.parameterized_trigger.BuildTriggerConfig;
import org.jenkinsci.test.acceptance.plugins.parameterized_trigger.TriggerCallBuildStep;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Build.Result;
import org.jenkinsci.test.acceptance.po.Container;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.Node;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.Matchers.*;
import static org.junit.Assume.*;

/**
 * Acceptance tests for the CheckStyle plugin.
 *
 * @author Martin Kurz
 * @author Fabian Trampusch
 * @author Ullrich Hafner
 */
@WithPlugins("checkstyle")
public class CheckStylePluginTest extends AbstractAnalysisTest<CheckStyleAction> {
    private static final String PATTERN_WITH_776_WARNINGS = "checkstyle-result.xml";
    private static final String CHECKSTYLE_PLUGIN_ROOT = "/checkstyle_plugin/";
    private static final String FILE_WITH_776_WARNINGS = CHECKSTYLE_PLUGIN_ROOT + PATTERN_WITH_776_WARNINGS;
    private static final String FILE_FOR_2ND_RUN = CHECKSTYLE_PLUGIN_ROOT + "forSecondRun/checkstyle-result.xml";
    private static final int TOTAL_NUMBER_OF_WARNINGS = 776;

    /**
     * Verifies that environment variables are expanded in the file name pattern.
     */
    @Test @Issue("JENKINS-30735") @WithPlugins({"envinject", "analysis-core@1.77", "checkstyle@3.46"})
    public void should_resolve_environment_variables() {
        FreeStyleJob job = createFreeStyleJob(settings -> settings.pattern.set("checkstyle${ENV_DASH}result.xml"));

        job.configure(() -> new EnvInjectConfig.Environment(job).properties.sendKeys("ENV_DASH=-"));

        Build build = buildSuccessfulJob(job);
        assertThatCheckStyleResultExists(job, build);

        CheckStyleAction action = new CheckStyleAction(job);
        assertThatWarningsCountInSummaryIs(action, TOTAL_NUMBER_OF_WARNINGS);
        assertThatNewWarningsCountInSummaryIs(action, TOTAL_NUMBER_OF_WARNINGS);

        assertThat(build.getConsole(),
                containsRegexp("\\[CHECKSTYLE\\] Searching for all files in .* that match the pattern checkstyle-result.xml\n"));
    }

    @Test @WithPlugins("parameterized-trigger") @Issue("JENKINS-33162")
    public void should_return_from_triggered_subjob() {
        FreeStyleJob checkstyleJob = createFreeStyleJob(settings -> settings.pattern.set(PATTERN_WITH_776_WARNINGS));

        FreeStyleJob trigger = jenkins.jobs.create();
        trigger.configure();
        TriggerCallBuildStep step = trigger.addBuildStep(TriggerCallBuildStep.class);
        BuildTriggerConfig config = step.getBuildTriggerConfig(0);
        config.projects.set(checkstyleJob.name);
        config.block.click();
        trigger.save();

        trigger.startBuild().shouldSucceed();
        Build downstream = checkstyleJob.build(1);
        downstream.shouldSucceed();
    }

    /**
     * Runs job two times to check if new and fixed warnings are displayed. Afterwards, the first build
     * is deleted and Jenkins is restarted. Then the results of the second build are validated again: the detail
     * pages should then show the same results (see JENKINS-24940).
     */
    @Test @Issue("24940")
    public void should_report_new_and_fixed_warnings_in_consecutive_builds() {
        assumeTrue("This test requires a restartable Jenkins", jenkins.canRestart());

        FreeStyleJob job = createFreeStyleJob();
        Build firstBuild = buildJobAndWait(job);

        replaceResource(FILE_FOR_2ND_RUN, job);

        Build lastBuild = buildSuccessfulJob(job);

        assertThatCheckStyleResultExists(job, lastBuild);

        lastBuild.open();

        verifyWarningCounts(lastBuild);

        firstBuild.delete();
        jenkins.restart();
        lastBuild.open();

        verifyWarningCounts(lastBuild);
    }

    private void verifyWarningCounts(final Build build) {
        CheckStyleAction action = new CheckStyleAction(build);

        assertThatWarningsCountInSummaryIs(action, 679);
        assertThatNewWarningsCountInSummaryIs(action, 3);
        assertThatFixedWarningsCountInSummaryIs(action, 97);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(679));
        assertThat(action.getNumberOfNewWarnings(), is(3));
        assertThat(action.getNumberOfFixedWarnings(), is(97));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(679));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(0));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(0));

        action.openNew();

        assertThat(action.getNumberOfWarningsWithHighPriority(), is(3));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(0));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(0));

        action.openFixed();

        assertThat(action.getNumberOfRowsInFixedWarningsTable(), is(97));
    }

    private void assertThatCheckStyleResultExists(final Job job, final PageObject build) {
        String actionName = "Checkstyle Warnings";
        assertThat(job, hasAction(actionName));
        assertThat(job.getLastBuild(), hasAction(actionName));
        assertThat(build, hasAction(actionName));
    }

    private MavenModuleSet createMavenJob() {
        return createMavenJob(new NullConfigurator<>());
    }

    private MavenModuleSet createMavenJob(AnalysisConfigurator<CheckStyleMavenSettings> configurator) {
        String projectPath = CHECKSTYLE_PLUGIN_ROOT + "sample_checkstyle_project";
        String goal = "clean package checkstyle:checkstyle";
        return createMavenJob(projectPath, goal, CheckStyleMavenSettings.class, configurator);
    }

    /**
     * Builds an existing freestyle project using actual maven commands and checks if new warning are displayed. Also
     * verifies that the warnings have links to the actual source code and the source code view shows the affected
     * line.
     */
    @Test
    public void should_link_to_source_code_in_real_project() {
        FreeStyleJob job = createJob(jenkins, CHECKSTYLE_PLUGIN_ROOT + "sample_checkstyle_project", FreeStyleJob.class,
                CheckStyleFreestyleSettings.class,
                settings -> settings.pattern.set("target/checkstyle-result.xml"));
        setMavenGoal(job, "clean package checkstyle:checkstyle");

        Build build = buildSuccessfulJob(job);

        assertThatCheckStyleResultExists(job, build);

        build.open();

        CheckStyleAction checkstyle = new CheckStyleAction(build);
        checkstyle.open();

        assertThat(checkstyle.getNumberOfNewWarnings(), is(12));

        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("Main.java:0", 0);
        expectedContent.put("Main.java:2", 2);
        expectedContent.put("Main.java:4", 4);
        expectedContent.put("Main.java:6", 6);
        expectedContent.put("Main.java:9", 9);
        expectedContent.put("Main.java:13", 13);
        expectedContent.put("Main.java:18", 18);
        expectedContent.put("Main.java:23", 23);
        expectedContent.put("Main.java:24", 24);
        expectedContent.put("Main.java:27", 27);
        assertThat(checkstyle.getWarningsTabContents(), is(expectedContent));

        verifySourceLine(checkstyle, "Main.java", 27,
                "27     public static int return8() {",
                "Checks the Javadoc of a method or constructor.");
    }

    /**
     * Builds a multi-module maven project and checks that warnings are grouped by module.
     */
    // TODO: Check module details
    @Test
    public void should_group_warnings_by_module() {
        MavenModuleSet job = createMavenJob(CHECKSTYLE_PLUGIN_ROOT + "maven_multi_module",
                "clean package checkstyle:checkstyle", CheckStyleMavenSettings.class, new NullConfigurator<>());
        Node slave = createSlaveForJob(job);
        Build build = buildSuccessfulJobOnSlave(job, slave);

        assertThatCheckStyleResultExists(job, build);

        build.open();

        CheckStyleAction checkstyle = new CheckStyleAction(build);
        checkstyle.open();

        assertThat(checkstyle.getNumberOfNewWarnings(), is(24));
        assertThatModulesTabIsCorrectlyFilled(checkstyle);
    }

    private void assertThatModulesTabIsCorrectlyFilled(final CheckStyleAction checkstyle) {
        SortedMap<String, Integer> expectedConfigurationDetails = new TreeMap<>();
        expectedConfigurationDetails.put("module1", 12);
        expectedConfigurationDetails.put("module2", 12);
        assertThat(checkstyle.getModulesTabContents(), is(expectedConfigurationDetails));
    }

    /**
     * Builds a maven project and checks if new warning are displayed.
     */
    @Test
    public void should_retrieve_results_from_maven_job() {
        MavenModuleSet job = createMavenJob();

        Build build = buildSuccessfulJob(job);

        assertThatCheckStyleResultExists(job, build);

        build.open();

        CheckStyleAction checkstyle = new CheckStyleAction(build);
        checkstyle.open();

        assertThat(checkstyle.getNumberOfNewWarnings(), is(12));
    }

    /**
     * Builds a maven project and checks if it is unstable.
     */
    @Test
    public void should_set_result_to_unstable_if_warning_found() {
        MavenModuleSet job = createMavenJob(settings -> settings.setBuildUnstableTotalAll("0"));

        buildJobAndWait(job).shouldBeUnstable();
    }

    /**
     * Builds a maven project and checks if it failed.
     */
    @Test
    public void should_set_result_to_failed_if_warning_found() {
        MavenModuleSet job = createMavenJob(settings -> settings.setBuildFailedTotalAll("0"));

        buildJobAndWait(job).shouldFail();
    }

     /**
     * Creates a sequence of freestyle builds and checks if the build result is set correctly. New warning threshold is
     * set to zero, e.g. a new warning should mark a build as unstable.
     * <p/>
     * <ol>
     *     <li>Build 1: 1 new, 0 fixed (SUCCESS since no reference build is set)</li>
     *     <li>Build 2: 2 new, 0 fixed (UNSTABLE since threshold is reached)</li>
     *     <li>Build 3: 1 new, 2 fixed (UNSTABLE since still one warning is new based on delta with reference build)</li>
     *     <li>Build 4: 0 new, 1 fixed (SUCCESS since there are no warnings)</li>
     * </ol>
     */
    @Test
    public void should_set_result_in_build_sequence_when_comparing_to_reference_build() {
        FreeStyleJob job = createFreeStyleJob();

        runBuild(job, 1, Result.SUCCESS, 1, 0, false);
        runBuild(job, 2, Result.UNSTABLE, 2, 0, false);
        runBuild(job, 3, Result.UNSTABLE, 1, 1, false);
        runBuild(job, 4, Result.SUCCESS, 0, 0, false);
    }

    /**
     * Creates a sequence of freestyle builds and checks if the build result is set correctly. New warning threshold is
     * set to zero, e.g. a new warning should mark a build as unstable.
     * <p/>
     * <ol>
     *     <li>Build 1: 1 new warning (SUCCESS since no reference build is set)</li>
     *     <li>Build 2: 2 new warnings (UNSTABLE since threshold is reached)</li>
     *     <li>Build 3: 1 new warning (SUCCESS since all warnings of previous build are fixed)</li>
     * </ol>
     */
    @Test @Issue("JENKINS-13458")
    public void should_set_result_in_build_sequence_when_comparing_to_previous_build() {
        FreeStyleJob job = createFreeStyleJob();

        runBuild(job, 1, Result.SUCCESS, 1, 0, true);
        runBuild(job, 2, Result.UNSTABLE, 2, 0, true);
        runBuild(job, 3, Result.SUCCESS, 0, 2, true);
        runBuild(job, 4, Result.SUCCESS, 0, 1, true);
    }

    private void runBuild(final FreeStyleJob job, final int buildNumber, final Result expectedResult,
            final int expectedNewWarnings, final int expectedFixedWarnings, final boolean usePreviousAsReference) {
        String fileName = "checkstyle-result-build" + buildNumber + ".xml";

        editJob(CHECKSTYLE_PLUGIN_ROOT + fileName, false, job,
                CheckStyleFreestyleSettings.class,
                settings -> {
                    settings.setNewWarningsThresholdUnstable("0", usePreviousAsReference);
                    settings.pattern.set(fileName);
                });
        Build build = buildJobAndWait(job).shouldBe(expectedResult);

        if (expectedNewWarnings > 0) {
            assertThatCheckStyleResultExists(job, build);
            build.open();

            CheckStyleAction checkstyle = new CheckStyleAction(build);
            checkstyle.open();
            assertThat(checkstyle.getNumberOfNewWarnings(), is(expectedNewWarnings));
            assertThat(checkstyle.getNumberOfFixedWarnings(), is(expectedFixedWarnings));
        }
    }

    @Override
    protected CheckStyleAction createProjectAction(final Job job) {
        return new CheckStyleAction(job);
    }

    @Override
    protected CheckStyleAction createResultAction(final Build build) {
        return new CheckStyleAction(build);
    }

    @Override
    protected FreeStyleJob createFreeStyleJob(final Container owner) {
        return createFreeStyleJob(owner, settings -> settings.pattern.set(PATTERN_WITH_776_WARNINGS));
    }

    @Override
    protected WorkflowJob createPipeline() {
        return createPipelineWith(FILE_WITH_776_WARNINGS, "CheckStylePublisher");
    }

    @Override
    protected int getNumberOfWarnings() {
        return TOTAL_NUMBER_OF_WARNINGS;
    }

    @Override
    protected int getNumberOfHighPriorityWarnings() {
        return TOTAL_NUMBER_OF_WARNINGS;
    }

    @Override
    protected int getNumberOfNormalPriorityWarnings() {
        return 0;
    }

    @Override
    protected int getNumberOfLowPriorityWarnings() {
        return 0;
    }

    private FreeStyleJob createFreeStyleJob() {
        return createFreeStyleJob(jenkins);
    }

    private FreeStyleJob createFreeStyleJob(final AnalysisConfigurator<CheckStyleFreestyleSettings> buildConfigurator) {
        return createFreeStyleJob(jenkins, buildConfigurator);
    }

    private FreeStyleJob createFreeStyleJob(final Container owner,
            final AnalysisConfigurator<CheckStyleFreestyleSettings> buildConfigurator) {
        return createFreeStyleJob(owner, FILE_WITH_776_WARNINGS, buildConfigurator);
    }

    private FreeStyleJob createFreeStyleJob(final Container owner, final String fileName,
            final AnalysisConfigurator<CheckStyleFreestyleSettings> buildConfigurator) {
        return createJob(owner, fileName, FreeStyleJob.class, CheckStyleFreestyleSettings.class, buildConfigurator);
    }

    @Override
    protected void assertThatDetailsAreFilled(final CheckStyleAction action) {
        assertXmlApiMatchesExpected(action.getBuild(), "checkstyleResult/api/xml?depth=0",
                CHECKSTYLE_PLUGIN_ROOT + "api_depth_0-2_x.xml",
                false);

        assertThatFilesTabIsCorrectlyFilled(action);
        assertThatCategoriesTabIsCorrectlyFilled(action);
        assertThatTypesTabIsCorrectlyFilled(action);
    }

    private void assertThatFilesTabIsCorrectlyFilled(CheckStyleAction action) {
        SortedMap<String, Integer> expectedFileDetails = new TreeMap<>();
        expectedFileDetails.put("JavaProvider.java", 18);
        expectedFileDetails.put("PluginImpl.java", 8);
        expectedFileDetails.put("RemoteLauncher.java", 63);
        expectedFileDetails.put("SFTPClient.java", 76);
        expectedFileDetails.put("SFTPFileSystem.java", 34);
        expectedFileDetails.put("SSHConnector.java", 96);
        expectedFileDetails.put("SSHLauncher.java", 481);
        assertThat(action.getFileTabContents(), is(expectedFileDetails));
    }

    private void assertThatCategoriesTabIsCorrectlyFilled(CheckStyleAction action) {
        SortedMap<String, Integer> expectedCategories = new TreeMap<>();
        expectedCategories.put("Blocks", 28);
        expectedCategories.put("Checks", 123);
        expectedCategories.put("Coding", 61);
        expectedCategories.put("Design", 47);
        expectedCategories.put("Imports", 3);
        expectedCategories.put("Javadoc", 104);
        expectedCategories.put("Naming", 4);
        expectedCategories.put("Regexp", 23);
        expectedCategories.put("Sizes", 164);
        expectedCategories.put("Whitespace", 219);
        assertThat(action.getCategoriesTabContents(), is(expectedCategories));
    }

    private void assertThatTypesTabIsCorrectlyFilled(CheckStyleAction action) {
        SortedMap<String, Integer> expectedTypes = new TreeMap<>();
        expectedTypes.put("AvoidInlineConditionalsCheck", 9);
        expectedTypes.put("AvoidStarImportCheck", 1);
        expectedTypes.put("ConstantNameCheck", 1);
        expectedTypes.put("DesignForExtensionCheck", 35);
        expectedTypes.put("EmptyBlockCheck", 1);
        expectedTypes.put("FileTabCharacterCheck", 47);
        expectedTypes.put("FinalParametersCheck", 120);
        expectedTypes.put("HiddenFieldCheck", 44);
        expectedTypes.put("JavadocMethodCheck", 88);
        expectedTypes.put("JavadocPackageCheck", 1);
        expectedTypes.put("JavadocStyleCheck", 9);
        expectedTypes.put("JavadocTypeCheck", 3);
        expectedTypes.put("JavadocVariableCheck", 3);
        expectedTypes.put("LineLengthCheck", 160);
        expectedTypes.put("MagicNumberCheck", 8);
        expectedTypes.put("MethodNameCheck", 1);
        expectedTypes.put("NeedBracesCheck", 26);
        expectedTypes.put("ParameterNameCheck", 2);
        expectedTypes.put("ParameterNumberCheck", 4);
        expectedTypes.put("RegexpSinglelineCheck", 23);
        expectedTypes.put("RightCurlyCheck", 1);
        expectedTypes.put("TodoCommentCheck", 3);
        expectedTypes.put("UnusedImportsCheck", 2);
        expectedTypes.put("VisibilityModifierCheck", 12);
        expectedTypes.put("WhitespaceAfterCheck", 66);
        expectedTypes.put("WhitespaceAroundCheck", 106);
        assertThat(action.getTypesTabContents(), is(expectedTypes));
    }
}
