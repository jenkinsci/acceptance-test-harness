package plugins;

import java.util.SortedMap;
import java.util.TreeMap;

import org.jvnet.hudson.test.Issue;
import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisConfigurator;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckStyleFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckStyleMavenSettings;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckStyleAction;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckStyleColumn;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckStylePortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Build.Result;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.Node;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Acceptance tests for the CheckStyle plugin.
 *
 * @author Martin Kurz
 * @author Fabian Trampusch
 * @author Ullrich Hafner
 */
@WithPlugins("checkstyle")
public class CheckStylePluginTest extends AbstractAnalysisTest {
    private static final String PATTERN_WITH_776_WARNINGS = "checkstyle-result.xml";
    private static final String CHECKSTYLE_PLUGIN_ROOT = "/checkstyle_plugin/";
    private static final String FILE_WITH_776_WARNINGS = CHECKSTYLE_PLUGIN_ROOT + PATTERN_WITH_776_WARNINGS;
    private static final String FILE_FOR_2ND_RUN = CHECKSTYLE_PLUGIN_ROOT + "forSecondRun/checkstyle-result.xml";

    /**\
     * Checks that the plug-in sends a mail after a build has been failed. The content of the mail contains several
     * tokens that should be expanded in the mail with the correct values.
     */
    @Test @Issue("JENKINS-25501") @WithPlugins("email-ext")
    public void should_send_mail_with_expanded_tokens() {
        setUpMailer();

        FreeStyleJob job = createFreeStyleJob(new AnalysisConfigurator<CheckStyleFreestyleSettings>() {
            @Override
            public void configure(CheckStyleFreestyleSettings settings) {
                settings.setBuildFailedTotalAll("0");
                settings.pattern.set(PATTERN_WITH_776_WARNINGS);
            }
        });

        configureEmailNotification(job, "Checkstyle: ${CHECKSTYLE_RESULT}",
                "Checkstyle: ${CHECKSTYLE_COUNT}-${CHECKSTYLE_FIXED}-${CHECKSTYLE_NEW}");

        job.startBuild().shouldFail();

        verifyReceivedMail("Checkstyle: FAILURE", "Checkstyle: 776-0-776");
    }

    private FreeStyleJob createFreeStyleJob() {
        AnalysisConfigurator<CheckStyleFreestyleSettings> buildConfigurator = new AnalysisConfigurator<CheckStyleFreestyleSettings>() {
            @Override
            public void configure(CheckStyleFreestyleSettings settings) {
                settings.pattern.set(PATTERN_WITH_776_WARNINGS);
            }
        };
        return createFreeStyleJob(buildConfigurator);
    }

    private FreeStyleJob createFreeStyleJob(final AnalysisConfigurator<CheckStyleFreestyleSettings> buildConfigurator) {
        return createFreeStyleJob(FILE_WITH_776_WARNINGS, buildConfigurator);
    }

    private FreeStyleJob createFreeStyleJob(final String fileName, final AnalysisConfigurator<CheckStyleFreestyleSettings> buildConfigurator) {
        return setupJob(fileName, FreeStyleJob.class, CheckStyleFreestyleSettings.class, buildConfigurator);
    }

    /**
     * Builds a job with checkstyle enabled and verifies that checkstyle details are displayed in the build overview.
     */
    @Test
    public void should_collect_warnings_in_build() {
        FreeStyleJob job = createFreeStyleJob();
        buildJobWithSuccess(job);

        assertThatPageContainsCheckstyleResults(job.getLastBuild());
        assertThatPageContainsCheckstyleResults(job);
    }

    /**
     * Builds a job with checkstyle and verifies that the information checkstyle provides in the tabs about the build
     * are the information we expect.
     */
    @Test
    public void should_report_details_in_different_tabs() {
        FreeStyleJob job = createFreeStyleJob();
        buildJobWithSuccess(job).open();

        CheckStyleAction action = new CheckStyleAction(job);
        assertThat(action.getResultLinkByXPathText("776 warnings"), is("checkstyleResult"));
        assertThat(action.getResultLinkByXPathText("776 new warnings"), is("checkstyleResult/new"));
        assertThat(action.getWarningNumber(), is(776));
        assertThat(action.getNewWarningNumber(), is(776));
        assertThat(action.getFixedWarningNumber(), is(0));
        assertThat(action.getHighWarningNumber(), is(776));
        assertThat(action.getNormalWarningNumber(), is(0));
        assertThat(action.getLowWarningNumber(), is(0));
        assertThatFilesTabIsCorrectlyFilled(action);
        assertThatCategoriesTabIsCorrectlyFilled(action);
        assertThatTypesTabIsCorrectlyFilled(action);
    }

    private void assertThatFilesTabIsCorrectlyFilled(CheckStyleAction ca) {
        SortedMap<String, Integer> expectedFileDetails = new TreeMap<>();
        expectedFileDetails.put("JavaProvider.java", 18);
        expectedFileDetails.put("PluginImpl.java", 8);
        expectedFileDetails.put("RemoteLauncher.java", 63);
        expectedFileDetails.put("SFTPClient.java", 76);
        expectedFileDetails.put("SFTPFileSystem.java", 34);
        expectedFileDetails.put("SSHConnector.java", 96);
        expectedFileDetails.put("SSHLauncher.java", 481);
        assertThat(ca.getFileTabContents(), is(expectedFileDetails));
    }

    private void assertThatCategoriesTabIsCorrectlyFilled(CheckStyleAction ca) {
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
        assertThat(ca.getCategoriesTabContents(), is(expectedCategories));
    }

    private void assertThatTypesTabIsCorrectlyFilled(CheckStyleAction ca) {
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
        assertThat(ca.getTypesTabContents(), is(expectedTypes));
    }

    /**
     * Builds a job and tests if the checkstyle api (with depth=0 parameter set) responds with the expected output.
     * Difference in whitespaces are ok.
     */
    @Test
    public void should_return_results_via_remote_api() {
        FreeStyleJob job = createFreeStyleJob();
        Build build = buildJobWithSuccess(job);
        assertXmlApiMatchesExpected(build, "checkstyleResult/api/xml?depth=0", CHECKSTYLE_PLUGIN_ROOT + "api_depth_0.xml");
    }

    /**
     * Runs job two times to check if new and fixed warnings are displayed.
     */
    @Test
    public void should_report_new_and_fixed_warnings_in_consecutive_builds() {
        FreeStyleJob job = createFreeStyleJob();
        buildJobAndWait(job);
        editJob(FILE_FOR_2ND_RUN, false, job);
        Build lastBuild = buildJobWithSuccess(job);
        assertThatPageContainsCheckstyleResults(lastBuild);
        lastBuild.open();
        CheckStyleAction ca = new CheckStyleAction(job);
        assertThat(ca.getResultLinkByXPathText("679 warnings"), is("checkstyleResult"));
        assertThat(ca.getResultLinkByXPathText("3 new warnings"), is("checkstyleResult/new"));
        assertThat(ca.getResultLinkByXPathText("97 fixed warnings"), is("checkstyleResult/fixed"));
        assertThat(ca.getWarningNumber(), is(679));
        assertThat(ca.getNewWarningNumber(), is(3));
        assertThat(ca.getFixedWarningNumber(), is(97));
        assertThat(ca.getHighWarningNumber(), is(679));
        assertThat(ca.getNormalWarningNumber(), is(0));
        assertThat(ca.getLowWarningNumber(), is(0));
    }

    private void assertThatPageContainsCheckstyleResults(final PageObject page) {
        assertThat(page, hasAction("Checkstyle Warnings"));
    }

    /**
     * Runs job two times to check if the links of the graph are relative.
     */
    @Test @Issue("JENKINS-21723")
    public void should_have_relative_graph_links() throws Exception {
        FreeStyleJob job = createFreeStyleJob();
        buildJobAndWait(job);
        editJob(FILE_FOR_2ND_RUN, false, job);
        buildJobWithSuccess(job);

        assertAreaLinksOfJobAreLike(job, "checkstyle");
    }

    /**
     * Runs a job with warning threshold configured once and validates that build is marked as unstable.
     */
    @Test @Issue("JENKINS-19614")
    public void should_set_build_to_unstable_if_total_warnings_threshold_set() {
        FreeStyleJob job = createFreeStyleJob(FILE_WITH_776_WARNINGS, new AnalysisConfigurator<CheckStyleFreestyleSettings>() {
            @Override
            public void configure(CheckStyleFreestyleSettings settings) {
                settings.pattern.set(PATTERN_WITH_776_WARNINGS);
                settings.setBuildUnstableTotalAll("0");
                settings.setNewWarningsThresholdFailed("0");
                settings.setUseDeltaValues(true);
            }
        });

        buildJobAndWait(job).shouldBeUnstable();
    }

    private MavenModuleSet createMavenJob() {
        return createMavenJob(null);
    }

    private MavenModuleSet createMavenJob(AnalysisConfigurator<CheckStyleMavenSettings> configurator) {
        String projectPath = CHECKSTYLE_PLUGIN_ROOT + "sample_checkstyle_project";
        String goal = "clean package checkstyle:checkstyle";
        return setupMavenJob(projectPath, goal, CheckStyleMavenSettings.class, configurator);
    }

    /**
     * Builds an existing freestyle project using actual maven commands and checks if new warning are displayed. Also
     * verifies that the warnings have links to the actual source code and the source code view shows the affected
     * line.
     */
    @Test
    public void should_link_to_source_code_in_real_project() {
        AnalysisConfigurator<CheckStyleFreestyleSettings> buildConfigurator = new AnalysisConfigurator<CheckStyleFreestyleSettings>() {
            @Override
            public void configure(CheckStyleFreestyleSettings settings) {
                settings.pattern.set("target/checkstyle-result.xml");
            }
        };
        FreeStyleJob job = setupJob(CHECKSTYLE_PLUGIN_ROOT + "sample_checkstyle_project", FreeStyleJob.class,
                CheckStyleFreestyleSettings.class, buildConfigurator, "clean package checkstyle:checkstyle"
        );
        Build lastBuild = buildJobWithSuccess(job);
        assertThatPageContainsCheckstyleResults(lastBuild);
        lastBuild.open();
        CheckStyleAction checkstyle = new CheckStyleAction(job);
        assertThat(checkstyle.getNewWarningNumber(), is(12));

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
     * Builds a maven project and checks if new warning are displayed.
     */
    @Test
    public void should_retrieve_results_from_maven_job() {
        MavenModuleSet job = createMavenJob();
        Build lastBuild = buildJobWithSuccess(job);
        assertThatPageContainsCheckstyleResults(lastBuild);
        lastBuild.open();
        CheckStyleAction checkstyle = new CheckStyleAction(job);
        assertThat(checkstyle.getNewWarningNumber(), is(12));
    }

    /**
     * Builds a maven project and checks if it is unstable.
     */
    @Test
    public void should_set_result_to_unstable_if_warning_found() {
        MavenModuleSet job = createMavenJob(new AnalysisConfigurator<CheckStyleMavenSettings>() {
            @Override
            public void configure(CheckStyleMavenSettings settings) {
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
        MavenModuleSet job = createMavenJob(new AnalysisConfigurator<CheckStyleMavenSettings>() {
            @Override
            public void configure(CheckStyleMavenSettings settings) {
                settings.setBuildFailedTotalAll("0");
            }
        });
        buildJobAndWait(job).shouldFail();
    }

    /**
     * Builds a job on a slave with checkstyle and verifies that the information checkstyle provides in the tabs about
     * the build are the information we expect.
     */
    @Test
    public void should_retrieve_results_from_slave() throws Exception {
        FreeStyleJob job = createFreeStyleJob();
        Node slave = makeASlaveAndConfigureJob(job);
        Build build = buildJobOnSlaveWithSuccess(job, slave);

        assertThat(build.getNode(), is(slave));
        assertThatPageContainsCheckstyleResults(job.getLastBuild());
        assertThatPageContainsCheckstyleResults(job);
    }

    /**
     * Sets up a list view with a warnings column. Builds a job and checks if the column shows the correct number of
     * warnings and provides a direct link to the actual warning results.
     */
    @Test @Category(SmokeTest.class) @Issue("JENKINS-24436")
    public void should_set_warnings_count_in_list_view_column() {
        MavenModuleSet job = createMavenJob();
        buildJobAndWait(job).shouldSucceed();

        ListView view = addDashboardListViewColumn(CheckStyleColumn.class);
        assertValidLink(job.name);
        view.delete();
    }

    /**
     * Sets up a dashboard view with a warnings-per-project portlet. Builds a job and checks if the portlet shows the
     * correct number of warnings and provides a direct link to the actual warning results.
     */
    @Test @WithPlugins("dashboard-view")
    public void should_set_warnings_count_in_dashboard_portlet() {
        MavenModuleSet job = createMavenJob();
        buildJobAndWait(job).shouldSucceed();

        DashboardView view = addDashboardViewAndBottomPortlet(CheckStylePortlet.class);
        assertValidLink(job.name);
        view.delete();
    }

    private void assertValidLink(final String jobName) {
        By warningsLinkMatcher = by.css("a[href$='job/" + jobName + "/checkstyle']");

        assertThat(jenkins.all(warningsLinkMatcher).size(), is(1));
        WebElement link = jenkins.getElement(warningsLinkMatcher);
        assertThat(link.getText().trim(), is("12"));

        link.click();
        assertThat(driver, hasContent("CheckStyle Result"));
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
        final String fileName = "checkstyle-result-build" + number + ".xml";
        AnalysisConfigurator<CheckStyleFreestyleSettings> buildConfigurator = new AnalysisConfigurator<CheckStyleFreestyleSettings>() {
            @Override
            public void configure(CheckStyleFreestyleSettings settings) {
                settings.setNewWarningsThresholdUnstable("0", usePreviousAsReference);
                settings.pattern.set(fileName);
            }
        };

        editJob(CHECKSTYLE_PLUGIN_ROOT + fileName, false, job,
                CheckStyleFreestyleSettings.class, buildConfigurator);
        Build lastBuild = buildJobAndWait(job).shouldBe(expectedResult);

        if (expectedNewWarnings > 0) {
            assertThatPageContainsCheckstyleResults(lastBuild);
            lastBuild.open();
            CheckStyleAction checkstyle = new CheckStyleAction(job);
            assertThat(checkstyle.getNewWarningNumber(), is(expectedNewWarnings));
        }
    }
}
