package plugins;

import java.util.SortedMap;
import java.util.TreeMap;

import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisConfigurator;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdAction;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdColumn;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdMavenSettings;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdWarningsPortlet;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Build.Result;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.Node;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.jvnet.hudson.test.Issue;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import hudson.util.VersionNumber;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.text.IsEmptyString.isEmptyString;
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

    @Override
    protected PmdAction createProjectAction(final FreeStyleJob job) {
        return new PmdAction(job);
    }

    @Override
    protected PmdAction createResultAction(final Build build) {
        return new PmdAction(build);
    }

    @Override
    protected FreeStyleJob createFreeStyleJob() {
        return createFreeStyleJob(FILE_WITH_9_WARNINGS, new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                settings.pattern.set(PATTERN_WITH_9_WARNINGS);
            }
        });
    }

    @Override
    protected int getNumberOfWarnings() {
        return TOTAL_NUMBER_OF_WARNINGS;
    }

    /**
     * Verifies the validation of the ant pattern input field. The workspace is populated with several pmd files. Then,
     * different patterns are provided that all should match.
     */
    @Test @Issue({"JENKINS-34759", "JENKINS-34760"}) @Ignore("Until JENKINS-34759 JENKINS-34760 has been fixed in core.")
    public void should_show_no_warnings_for_correct_ant_patterns() {
        FreeStyleJob job = createFreeStyleJob();

        AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator = new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                settings.pattern.set(PATTERN_WITHOUT_WARNINGS);
            }
        };

        editJob(PLUGIN_ROOT, false, job,
                PmdFreestyleSettings.class, buildConfigurator);
        buildSuccessfulJob(job);

        validatePattern(job, "pmd.xml,not-here.xml");
        validatePattern(job, "not-here.xml,pmd.xml");
        validatePattern(job, "pmd.xml not-here.xml");
    }

    private void validatePattern(final FreeStyleJob job, final String pattern) {
        AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator = new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                String validationMessage = settings.validatePattern(pattern);
                assertThat(validationMessage, isEmptyString());
            }
        };
        editJob(PLUGIN_ROOT, false, job,
                PmdFreestyleSettings.class, buildConfigurator);
    }

    /**
     * Checks that the plug-in sends a mail after a build has been failed. The content of the mail
     * contains several tokens that should be expanded in the mail with the correct values.
     */
    @Test  @Issue("JENKINS-25501") @WithPlugins("email-ext")
    public void should_send_mail_with_expanded_tokens() {
        setUpMailer();

        FreeStyleJob job = createFreeStyleJob(FILE_WITH_9_WARNINGS, new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                settings.setBuildFailedTotalAll("0");
                settings.pattern.set(PATTERN_WITH_9_WARNINGS);
            }
        });

        configureEmailNotification(job, "PMD: ${PMD_RESULT}",
                "PMD: ${PMD_COUNT}-${PMD_FIXED}-${PMD_NEW}");

        job.startBuild().shouldFail();

        verifyReceivedMail("PMD: FAILURE", "PMD: 9-0-9");
    }

    /**
     * Configures a job with PMD and checks that the parsed PMD file does not contain warnings.
     */
    @Test
    public void should_find_no_warnings() {
        FreeStyleJob job = createFreeStyleJob(new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                settings.pattern.set(PATTERN_WITHOUT_WARNINGS);
            }
        });

        Build lastBuild = buildSuccessfulJob(job);

        assertThatBuildHasNoWarnings(lastBuild);
    }

    private FreeStyleJob createFreeStyleJob(final AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator) {
        return createFreeStyleJob(FILE_WITHOUT_WARNINGS, buildConfigurator);
    }

    private FreeStyleJob createFreeStyleJob(final String fileName, final AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator) {
        return setupJob(fileName, FreeStyleJob.class, PmdFreestyleSettings.class, buildConfigurator);
    }

    /**
     * Checks that PMD runs even if the build failed if the property 'canRunOnFailed' is set.
     */
    @Test
    public void should_collect_warnings_even_if_build_failed() {
        FreeStyleJob job = createFreeStyleJob(new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                settings.pattern.set(PATTERN_WITHOUT_WARNINGS);
                settings.setCanRunOnFailed(true);
            }
        });

        job.configure();
        job.addShellStep("false");
        job.save();

        Build lastBuild = buildFailingJob(job);

        assertThatBuildHasNoWarnings(lastBuild);
    }

    /**
     * Configures a job with PMD and checks that the parsed PMD file contains 9 warnings.
     */
    @Test
    public void should_report_details_in_different_tabs() {
        FreeStyleJob job = createFreeStyleJob(FILE_WITH_9_WARNINGS, new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                settings.pattern.set(PATTERN_WITH_9_WARNINGS);
            }
        });

        Build build = buildSuccessfulJob(job);

        assertThatPmdResultExists(job, build);

        build.open();

        PmdAction action = new PmdAction(build);

        assertThatWarningsCountInSummaryIs(action, TOTAL_NUMBER_OF_WARNINGS);
        assertThatNewWarningsCountInSummaryIs(action, TOTAL_NUMBER_OF_WARNINGS);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(TOTAL_NUMBER_OF_WARNINGS));
        assertThat(action.getNumberOfNewWarnings(), is(TOTAL_NUMBER_OF_WARNINGS));
        assertThat(action.getNumberOfFixedWarnings(), is(0));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(0));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(3));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(6));

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

    /**
     * Builds a job and tests if the PMD api (with depth=0 parameter set) responds with the expected output.
     * Difference in whitespaces are ok.
     */
    @Test @Category(SmokeTest.class)
    public void should_return_results_via_remote_api() {
        FreeStyleJob job = createFreeStyleJob();

        Build build = buildSuccessfulJob(job);

        boolean is2xLine = !jenkins.getVersion().isOlderThan(new VersionNumber("2.0"));
        assertXmlApiMatchesExpected(build, "pmdResult/api/xml?depth=0", PLUGIN_ROOT + (is2xLine ? "api_depth_0-2_x.xml" : "api_depth_0.xml"), false);
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
        editJob(PLUGIN_ROOT + "forSecondRun/pmd-warnings.xml", false, job);

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
     * Runs a job with warning threshold configured once and validates that build is marked as unstable.
     */
    @Test @Issue("JENKINS-19614")
    public void should_set_build_to_unstable_if_total_warnings_threshold_set() {
        FreeStyleJob job = createFreeStyleJob(FILE_WITH_9_WARNINGS, new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                settings.pattern.set(PATTERN_WITH_9_WARNINGS);
                settings.setBuildUnstableTotalAll("0");
                settings.setNewWarningsThresholdFailed("0");
                settings.setUseDeltaValues(true);
            }
        });

        buildUnstableJob(job);
    }

    private MavenModuleSet createMavenJob() {
        return createMavenJob(null);
    }

    private MavenModuleSet createMavenJob(AnalysisConfigurator<PmdMavenSettings> configurator) {
        String projectPath = PLUGIN_ROOT + "sample_pmd_project";
        String goal = "clean package pmd:pmd";
        return setupMavenJob(projectPath, goal, PmdMavenSettings.class, configurator);
    }

    /**
     * Builds a freestyle project and checks if new warning are displayed.
     */
    @Test
    public void should_link_to_source_code_in_real_project() {
        AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator = new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                settings.pattern.set("target/pmd.xml");
            }
        };
        FreeStyleJob job = setupJob(PLUGIN_ROOT + "sample_pmd_project", FreeStyleJob.class,
                PmdFreestyleSettings.class, buildConfigurator, "clean package pmd:pmd");

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
        MavenModuleSet job = createMavenJob(new AnalysisConfigurator<PmdMavenSettings>() {
            @Override
            public void configure(PmdMavenSettings settings) {
                settings.setBuildUnstableTotalAll("0");
            }
        });

        buildJobAndWait(job).shouldBeUnstable();
    }

    /**
     * Builds a maven project and checks if it failed.
     */
    @Test
    public void should_set_result_to_failed_if_warning_found() {
        MavenModuleSet job = createMavenJob(new AnalysisConfigurator<PmdMavenSettings>() {
            @Override
            public void configure(PmdMavenSettings settings) {
                settings.setBuildFailedTotalAll("0");
            }
        });

        buildJobAndWait(job).shouldFail();
    }

    /**
     * Builds a job on a slave with pmd and verifies that the information pmd provides in the tabs about the build
     * are the information we expect.
     */
    @Test
    public void should_retrieve_results_from_slave() throws Exception {
        FreeStyleJob job = createFreeStyleJob();
        Node slave = createSlaveForJob(job);

        Build build = buildSuccessfulJobOnSlave(job, slave);

        assertThat(build.getNode(), is(slave));
        assertThatPmdResultExists(job, build);
    }

    /**
     * Sets up a list view with a warnings column. Builds a job and checks if the column shows the correct number of
     * warnings and provides a direct link to the actual warning results.
     */
    @Test @Issue("JENKINS-24436")
    public void should_set_warnings_count_in_list_view_column() {
        MavenModuleSet job = createMavenJob();
        buildJobAndWait(job).shouldSucceed();

        ListView view = addListViewColumn(PmdColumn.class);

        assertValidLink(job.name);
        view.delete();
    }

    /**
     * Sets up a dashboard view with a warnings-per-project portlet. Builds a job and checks if the portlet shows the
     * correct number of warnings and provides a direct link to the actual warning results.
     */
    @Test @WithPlugins("dashboard-view")
    public void should_set_warnings_count_in_dashboard_portlet() {
        jenkins.restart();

        final MavenModuleSet job = createMavenJob();
        buildJobAndWait(job).shouldSucceed();

        final DashboardView view = addDashboardViewAndBottomPortlet(PmdWarningsPortlet.class);

        assertValidLink(job.name);
        view.delete();
    }

    private void assertValidLink(final String jobName) {
        By warningsLinkMatcher = by.css("a[href$='job/" + jobName + "/pmd']");

        assertThat(jenkins.all(warningsLinkMatcher).size(), is(1));
        WebElement link = jenkins.getElement(warningsLinkMatcher);
        assertThat(link.getText().trim(), is("2"));

        link.click();
        assertThat(driver, hasContent("PMD Result"));
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

    private void runBuild(final FreeStyleJob job, final int number, final Result expectedResult, final int expectedNewWarnings, final boolean usePreviousAsReference) {
        final String fileName = "pmd-warnings-build" + number + ".xml";
        AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator = new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                settings.setNewWarningsThresholdUnstable("0", usePreviousAsReference);
                settings.pattern.set(fileName);
            }
        };

        editJob(PLUGIN_ROOT + fileName, false, job,
                PmdFreestyleSettings.class, buildConfigurator);
        Build build = buildJobAndWait(job).shouldBe(expectedResult);

        if (expectedNewWarnings > 0) {
            assertThatPmdResultExists(job, build);

            build.open();

            PmdAction action = new PmdAction(build);
            action.open();

            assertThat(action.getNumberOfNewWarnings(), is(expectedNewWarnings));
        }
    }
}
