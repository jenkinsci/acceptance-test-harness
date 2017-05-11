package plugins;

import java.util.SortedMap;
import java.util.TreeMap;

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisConfigurator;
import org.jenkinsci.test.acceptance.plugins.analysis_core.NullConfigurator;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdAction;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdMavenSettings;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Build.Result;
import org.jenkinsci.test.acceptance.po.Container;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.text.IsEmptyString.*;
import static org.jenkinsci.test.acceptance.Matchers.*;
import static org.junit.Assume.*;

/**
 * Acceptance tests for the PMD plugin.
 *
 * @author Martin Kurz
 * @author Fabian Trampusch
 * @author Ullrich Hafner
 */
@WithPlugins("pmd")
public class PmdPluginTest extends AbstractAnalysisTest<PmdAction> {
    private static final String PLUGIN_ROOT = "/pmd_plugin/";
    private static final String PATTERN_WITHOUT_WARNINGS = "pmd.xml";
    private static final String FILE_WITHOUT_WARNINGS = PLUGIN_ROOT + PATTERN_WITHOUT_WARNINGS;
    private static final String PATTERN_WITH_9_WARNINGS = "pmd-warnings.xml";
    private static final String FILE_WITH_9_WARNINGS = PLUGIN_ROOT + PATTERN_WITH_9_WARNINGS;
    private static final int TOTAL_NUMBER_OF_WARNINGS = 9;

    /**
     * Verifies the validation of the ant pattern input field. The workspace is populated with several pmd files. Then,
     * different patterns are provided that all should match.
     */
    @Test @Issue({"JENKINS-34759", "JENKINS-34760"}) @Ignore("Until JENKINS-34759 JENKINS-34760 has been fixed in core.")
    public void should_show_no_warnings_for_correct_ant_patterns() {
        FreeStyleJob job = createFreeStyleJob();

        editJob(PLUGIN_ROOT, false, job, PmdFreestyleSettings.class,
                settings -> settings.pattern.set(PATTERN_WITHOUT_WARNINGS));
        buildSuccessfulJob(job);

        validatePattern(job, "pmd.xml,not-here.xml");
        validatePattern(job, "not-here.xml,pmd.xml");
        validatePattern(job, "pmd.xml not-here.xml");
    }

    private void validatePattern(final FreeStyleJob job, final String pattern) {
        editJob(PLUGIN_ROOT, false, job, PmdFreestyleSettings.class,
                settings -> {
                    String validationMessage = settings.validatePattern(pattern);
                    assertThat(validationMessage, isEmptyString());
                });
    }

   /**
     * Configures a job with PMD and checks that the parsed PMD file does not contain warnings.
     */
    @Test
    public void should_find_no_warnings() {
        FreeStyleJob job = createFreeStyleJob(settings -> settings.pattern.set(PATTERN_WITHOUT_WARNINGS));

        Build lastBuild = buildSuccessfulJob(job);

        assertThatBuildHasNoWarnings(lastBuild);
    }

     /**
     * Checks that PMD runs even if the build failed if the property 'canRunOnFailed' is set.
     */
    @Test
    public void should_collect_warnings_even_if_build_failed() {
        FreeStyleJob job = createFreeStyleJob(settings -> {
            settings.pattern.set(PATTERN_WITHOUT_WARNINGS);
            settings.setCanRunOnFailed(true);
        });

        job.configure(() -> job.addShellStep("false"));

        Build lastBuild = buildFailingJob(job);

        assertThatBuildHasNoWarnings(lastBuild);
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
        editJob(PLUGIN_ROOT + "forSecondRun/pmd-warnings.xml", false, job,
                PmdFreestyleSettings.class);

        Build lastBuild = buildSuccessfulJob(job);

        assertThatPmdResultExists(job, lastBuild);

        lastBuild.open();

        verifyWarningCounts(lastBuild);

        firstBuild.delete();
        jenkins.restart();
        lastBuild.open();

        verifyWarningCounts(lastBuild);
    }

    private void verifyWarningCounts(final Build build) {
        PmdAction action = new PmdAction(build);

        assertThatWarningsCountInSummaryIs(action, 8);
        assertThatNewWarningsCountInSummaryIs(action, 1);
        assertThatFixedWarningsCountInSummaryIs(action, 1);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(8));
        assertThat(action.getNumberOfNewWarnings(), is(1));
        assertThat(action.getNumberOfFixedWarnings(), is(1));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(0));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(2));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(6));

        action.openNew();

        assertThat(action.getNumberOfWarningsWithHighPriority(), is(0));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(0));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(1));

        action.openFixed();

        assertThat(action.getNumberOfRowsInFixedWarningsTable(), is(1));
    }

    /**
     * Builds a freestyle project and checks if new warning are displayed.
     */
    @Test
    public void should_link_to_source_code_in_real_project() {
        FreeStyleJob job = createJob(jenkins, PLUGIN_ROOT + "sample_pmd_project", FreeStyleJob.class,
                PmdFreestyleSettings.class,
                settings -> settings.pattern.set("target/pmd.xml"));
        setMavenGoal(job, "clean package pmd:pmd");

        Build build = buildSuccessfulJob(job);

        assertThatPmdResultExists(job, build);

        build.open();

        PmdAction action = new PmdAction(build);
        action.open();

        assertThat(action.getNumberOfNewWarnings(), is(2));

        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("Main.java:9", TOTAL_NUMBER_OF_WARNINGS);
        expectedContent.put("Main.java:13", 13);

        verifySourceLine(action, "Main.java", 13,
                "13         if(false) {",
                "Do not use if statements that are always true or always false.");
    }

    /**
     * Builds a maven project and checks if new warnings are displayed.
     */
    @Test
    public void should_retrieve_results_from_maven_job() {
        MavenModuleSet job = createMavenJob();

        Build build = buildSuccessfulJob(job);

        assertThatPmdResultExists(job, build);

        build.open();

        PmdAction action = new PmdAction(build);
        action.open();

        assertThat(action.getNumberOfNewWarnings(), is(2));
    }

    private void assertThatPmdResultExists(final Job job, final PageObject build) {
        String actionName = "PMD Warnings";
        assertThat(job, hasAction(actionName));
        assertThat(job.getLastBuild(), hasAction(actionName));
        assertThat(build, hasAction(actionName));
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
     *     <li>Build 1: 1 new warning (SUCCESS since no reference build is set)</li>
     *     <li>Build 2: 2 new warnings (UNSTABLE since threshold is reached)</li>
     *     <li>Build 3: 1 new warning (UNSTABLE since still one warning is new based on delta with reference build)</li>
     *     <li>Build 4: 1 new warning (SUCCESS since there are no warnings)</li>
     * </ol>
     */
    @Test
    public void should_set_result_in_build_sequence_when_comparing_to_reference_build() {
        FreeStyleJob job = createFreeStyleJob();

        runBuild(job, 1, Result.SUCCESS, 1, false);
        runBuild(job, 2, Result.UNSTABLE, 2, false);
        runBuild(job, 3, Result.UNSTABLE, 1, false);
        runBuild(job, 4, Result.SUCCESS, 0, false);
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

        runBuild(job, 1, Result.SUCCESS, 1, true);
        runBuild(job, 2, Result.UNSTABLE, 2, true);
        runBuild(job, 3, Result.SUCCESS, 0, true);
    }

    private void runBuild(final FreeStyleJob job, final int number, final Result expectedResult,
            final int expectedNewWarnings, final boolean usePreviousAsReference) {
        final String fileName = "pmd-warnings-build" + number + ".xml";

        editJob(PLUGIN_ROOT + fileName, false, job, PmdFreestyleSettings.class,
                settings -> {
                    settings.setNewWarningsThresholdUnstable("0", usePreviousAsReference);
                    settings.pattern.set(fileName);
                });
        Build build = buildJobAndWait(job).shouldBe(expectedResult);

        if (expectedNewWarnings > 0) {
            assertThatPmdResultExists(job, build);

            build.open();

            PmdAction action = new PmdAction(build);
            action.open();

            assertThat(action.getNumberOfNewWarnings(), is(expectedNewWarnings));
        }
    }

    @Override
    protected PmdAction createProjectAction(final Job job) {
        return new PmdAction(job);
    }

    @Override
    protected PmdAction createResultAction(final Build build) {
        return new PmdAction(build);
    }

    @Override
    protected FreeStyleJob createFreeStyleJob(final Container owner) {
        return createFreeStyleJob(FILE_WITH_9_WARNINGS, owner, settings -> settings.pattern.set(PATTERN_WITH_9_WARNINGS));
    }

    @Override
    protected WorkflowJob createPipeline() {
        return createPipelineWith(FILE_WITH_9_WARNINGS, "PmdPublisher");
    }

    @Override
    protected int getNumberOfWarnings() {
        return TOTAL_NUMBER_OF_WARNINGS;
    }

    @Override
    protected int getNumberOfHighPriorityWarnings() {
        return 0;
    }

    @Override
    protected int getNumberOfNormalPriorityWarnings() {
        return 3;
    }

    @Override
    protected int getNumberOfLowPriorityWarnings() {
        return 6;
    }

    @Override
    protected void assertThatDetailsAreFilled(final PmdAction action) {
        assertXmlApiMatchesExpected(action.getBuild(), "pmdResult/api/xml?depth=0",
                PLUGIN_ROOT + "api_depth_0-2_x.xml", false);

        assertThatFilesTabIsCorrectlyFilled(action);
        assertThatTypesTabIsCorrectlyFilled(action);
        assertThatWarningsTabIsCorrectlyFilled(action);
    }

    private void assertThatFilesTabIsCorrectlyFilled(PmdAction action) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("ChannelContentAPIClient.m", 6);
        expectedContent.put("ProductDetailAPIClient.m", 2);
        expectedContent.put("ViewAllHoldingsAPIClient.m", 1);
        assertThat(action.getFileTabContents(), is(expectedContent));
    }

    private void assertThatTypesTabIsCorrectlyFilled(PmdAction action) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("long line", 6);
        expectedContent.put("unused method parameter", 3);
        assertThat(action.getTypesTabContents(), is(expectedContent));
    }

    private void assertThatWarningsTabIsCorrectlyFilled(PmdAction action) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("ChannelContentAPIClient.m:28", 28);
        expectedContent.put("ChannelContentAPIClient.m:28", 28);
        expectedContent.put("ChannelContentAPIClient.m:28", 28);
        expectedContent.put("ChannelContentAPIClient.m:32", 32);
        expectedContent.put("ChannelContentAPIClient.m:36", 36);
        expectedContent.put("ChannelContentAPIClient.m:40", 40);
        expectedContent.put("ProductDetailAPIClient.m:37", 37);
        expectedContent.put("ProductDetailAPIClient.m:38", 38);
        expectedContent.put("ViewAllHoldingsAPIClient.m:23", 23);
        assertThat(action.getWarningsTabContents(), is(expectedContent));
    }

    private FreeStyleJob createFreeStyleJob() {
        return createFreeStyleJob(jenkins);
    }

    private FreeStyleJob createFreeStyleJob(final AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator) {
        return createFreeStyleJob(FILE_WITHOUT_WARNINGS, buildConfigurator);
    }

    private FreeStyleJob createFreeStyleJob(final String fileName, final AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator) {
        return createFreeStyleJob(fileName, jenkins, buildConfigurator);
    }

    private FreeStyleJob createFreeStyleJob(final String fileName, final Container owner,
            final AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator) {
        return createJob(owner, fileName, FreeStyleJob.class, PmdFreestyleSettings.class, buildConfigurator);
    }
    private MavenModuleSet createMavenJob() {
        return createMavenJob(new NullConfigurator<>());
    }

    private MavenModuleSet createMavenJob(AnalysisConfigurator<PmdMavenSettings> configurator) {
        String projectPath = PLUGIN_ROOT + "sample_pmd_project";
        String goal = "clean package pmd:pmd";
        return createMavenJob(projectPath, goal, PmdMavenSettings.class, configurator);
    }
}
