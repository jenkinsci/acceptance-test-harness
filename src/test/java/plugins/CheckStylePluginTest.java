package plugins;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jenkinsci.test.acceptance.junit.Bug;
import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisConfigurator;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckstyleAction;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckstyleFreestyleBuildSettings;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckstyleListViewColumn;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckstyleMavenBuildSettings;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckstyleWarningsPerProjectDashboardViewPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Build.Result;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.Node;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xml.sax.SAXException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Acceptance tests for the CheckStyle plugin.
 */
@WithPlugins("checkstyle")
public class CheckStylePluginTest extends AbstractAnalysisTest {
    private static final String PATTERN_WITH_776_WARNINGS = "checkstyle-result.xml";
    private static final String CHECKSTYLE_PLUGIN_ROOT = "/checkstyle_plugin/";
    private static final String FILE_WITH_776_WARNINGS = CHECKSTYLE_PLUGIN_ROOT + PATTERN_WITH_776_WARNINGS;
    private static final String FILE_FOR_2ND_RUN = "/checkstyle_plugin/forSecondRun/checkstyle-result.xml";

    /**
     * Checks that the plug-in sends a mail after a build has been failed. The content of the mail contains several
     * tokens that should be expanded in the mail with the correct vaules.
     */
    @Test
    @WithPlugins("email-ext")
    @Bug("25501")
    public void should_send_mail_with_expanded_tokens() {
        setUpMailer();

        FreeStyleJob job = setUpFreestyleJob(new AnalysisConfigurator<CheckstyleFreestyleBuildSettings>() {
            @Override
            public void configure(CheckstyleFreestyleBuildSettings settings) {
                settings.setBuildFailedTotalAll("0");
                settings.pattern.set(PATTERN_WITH_776_WARNINGS);
            }
        });

        configureEmailNotification(job, "Checkstyle: ${CHECKSTYLE_RESULT}",
                "Checkstyle: ${CHECKSTYLE_COUNT}-${CHECKSTYLE_FIXED}-${CHECKSTYLE_NEW}");

        job.startBuild().shouldFail();

        verifyReceivedMail("Checkstyle: FAILURE", "Checkstyle: 776-0-776");
    }

    private FreeStyleJob setUpFreestyleJob(final AnalysisConfigurator<CheckstyleFreestyleBuildSettings> buildConfigurator) {
        return setupJob(FILE_WITH_776_WARNINGS, FreeStyleJob.class,
                CheckstyleFreestyleBuildSettings.class, buildConfigurator);
    }

    /**
     * Builds a job with checkstyle enabled and verifies that checkstyle details are displayed in the build overview.
     */
    @Test
    public void record_checkstyle_report() {
        FreeStyleJob job = setUpFreestyleJob();
        buildJobWithSuccess(job);

        assertThatBuildHasCheckstyleResults(job.getLastBuild());
        assertThatBuildHasCheckstyleResults(job);
    }

    /**
     * Builds a job with checkstyle and verifies that the information checkstyle provides in the tabs about the build
     * are the information we expect.
     */
    @Test
    public void view_checkstyle_report() {
        FreeStyleJob job = setUpFreestyleJob();
        buildJobWithSuccess(job).open();

        CheckstyleAction ca = new CheckstyleAction(job);
        assertThat(ca.getResultLinkByXPathText("776 warnings"), is("checkstyleResult"));
        assertThat(ca.getResultLinkByXPathText("776 new warnings"), is("checkstyleResult/new"));
        assertThat(ca.getWarningNumber(), is(776));
        assertThat(ca.getNewWarningNumber(), is(776));
        assertThat(ca.getFixedWarningNumber(), is(0));
        assertThat(ca.getHighWarningNumber(), is(776));
        assertThat(ca.getNormalWarningNumber(), is(0));
        assertThat(ca.getLowWarningNumber(), is(0));
        assertFileTab(ca);
        assertCategoryTab(ca);
        assertTypeTab(ca);
    }

    private void assertFileTab(CheckstyleAction ca) {
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

    private void assertCategoryTab(CheckstyleAction ca) {
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

    private void assertTypeTab(CheckstyleAction ca) {
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
    public void xml_api_report_depth_0() throws IOException, SAXException, ParserConfigurationException {
        FreeStyleJob job = setUpFreestyleJob();
        Build build = buildJobWithSuccess(job);
        assertXmlApiMatchesExpected(build, "checkstyleResult/api/xml?depth=0", "/checkstyle_plugin/api_depth_0.xml");
    }

    /**
     * Runs job two times to check if new and fixed warnings are displayed.
     */
    @Test
    public void view_checkstyle_report_two_runs_and_changed_results() {
        FreeStyleJob job = setUpFreestyleJob();
        buildJobAndWait(job);
        editJob(FILE_FOR_2ND_RUN, false, job);
        Build lastBuild = buildJobWithSuccess(job);
        assertThatBuildHasCheckstyleResults(lastBuild);
        lastBuild.open();
        CheckstyleAction ca = new CheckstyleAction(job);
        assertThat(ca.getResultLinkByXPathText("679 warnings"), is("checkstyleResult"));
        assertThat(ca.getResultLinkByXPathText("3 new warnings"), is("checkstyleResult/new"));
        assertThat(ca.getResultLinkByXPathText("100 fixed warnings"), is("checkstyleResult/fixed"));
        assertThat(ca.getWarningNumber(), is(679));
        assertThat(ca.getNewWarningNumber(), is(3));
        assertThat(ca.getFixedWarningNumber(), is(100));
        assertThat(ca.getHighWarningNumber(), is(679));
        assertThat(ca.getNormalWarningNumber(), is(0));
        assertThat(ca.getLowWarningNumber(), is(0));
    }

    private void assertThatBuildHasCheckstyleResults(final PageObject build) {
        assertThat(build, hasAction("Checkstyle Warnings"));
    }

    /**
     * Runs job two times to check if the links of the graph are relative.
     */
    @Test
    @Bug("21723")
    @Ignore("Until JENKINS-21723 is fixed")
    public void view_checkstyle_report_job_graph_links() throws Exception {
        FreeStyleJob job = setUpFreestyleJob();
        buildJobAndWait(job);
        editJob(FILE_FOR_2ND_RUN, false, job);
        buildJobWithSuccess(job);

        assertAreaLinksOfJobAreLike(job, "^\\d+/checkstyleResult");
    }

    private MavenModuleSet setupSimpleMavenJob() {
        return setupSimpleMavenJob(null);
    }

    private MavenModuleSet setupSimpleMavenJob(AnalysisConfigurator<CheckstyleMavenBuildSettings> configurator) {
        String projectPath = "/checkstyle_plugin/sample_checkstyle_project";
        String goal = "clean package checkstyle:checkstyle";
        return setupMavenJob(projectPath, goal, CheckstyleMavenBuildSettings.class, configurator);
    }

    /**
     * Builds an existing freestyle project using actual maven commands and checks if new warning are displayed. Also
     * verifies that the warnings have links to the actual source code and the source code view shows the affected
     * line.
     */
    @Test
    public void build_simple_freestyle_mavengoals_project() {
        AnalysisConfigurator<CheckstyleFreestyleBuildSettings> buildConfigurator = new AnalysisConfigurator<CheckstyleFreestyleBuildSettings>() {
            @Override
            public void configure(CheckstyleFreestyleBuildSettings settings) {
                settings.pattern.set("target/checkstyle-result.xml");
            }
        };
        FreeStyleJob job = setupJob("/checkstyle_plugin/sample_checkstyle_project", FreeStyleJob.class,
                CheckstyleFreestyleBuildSettings.class, buildConfigurator, "clean package checkstyle:checkstyle"
        );
        Build lastBuild = buildJobWithSuccess(job);
        assertThatBuildHasCheckstyleResults(lastBuild);
        lastBuild.open();
        CheckstyleAction checkstyle = new CheckstyleAction(job);
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
        // FIXME: Add source file checks
// TODO decision of uhafner
//        assertThat(checkstyle.getLinkedSourceFileLineNumber("Warnings", "Main.java:27", "High"), is(27));
//        assertThat(checkstyle.getLinkedSourceFileLineAsString("Warnings", "Main.java:0", "High"), containsString("Missing package-info.java file."));
//        assertThat(checkstyle.getLinkedSourceFileLineAsString("Warnings", "Main.java:6", "High"), endsWith("public static void main(String[] args) {"));
    }

    /**
     * Builds a maven project and checks if new warning are displayed.
     */
    @Test
    public void build_simple_maven_project() {
        MavenModuleSet job = setupSimpleMavenJob();
        Build lastBuild = buildJobWithSuccess(job);
        assertThatBuildHasCheckstyleResults(lastBuild);
        lastBuild.open();
        CheckstyleAction checkstyle = new CheckstyleAction(job);
        assertThat(checkstyle.getNewWarningNumber(), is(12));
    }

    /**
     * Builds a maven project and checks if it is unstable.
     */
    @Test
    public void build_simple_maven_project_and_check_if_it_is_unstable() {
        AnalysisConfigurator<CheckstyleMavenBuildSettings> buildConfigurator =
                new AnalysisConfigurator<CheckstyleMavenBuildSettings>() {
                    @Override
                    public void configure(CheckstyleMavenBuildSettings settings) {
                        settings.setBuildUnstableTotalAll("0");
                    }
                };
        MavenModuleSet job = setupSimpleMavenJob(buildConfigurator);
        buildJobAndWait(job).shouldBeUnstable();
    }

    /**
     * Builds a maven project and checks if it failed.
     */
    @Test
    public void build_simple_maven_project_and_check_if_failed() {
        AnalysisConfigurator<CheckstyleMavenBuildSettings> buildConfigurator =
                new AnalysisConfigurator<CheckstyleMavenBuildSettings>() {
                    @Override
                    public void configure(CheckstyleMavenBuildSettings settings) {
                        settings.setBuildFailedTotalAll("0");
                    }
                };
        MavenModuleSet job = setupSimpleMavenJob(buildConfigurator);
        buildJobAndWait(job).shouldFail();
    }

    /**
     * Builds a job on a slave with checkstyle and verifies that the information checkstyle provides in the tabs about
     * the build are the information we expect.
     */
    @Test
    public void view_checkstyle_report_build_on_slave() throws Exception {
        FreeStyleJob job = setUpFreestyleJob();
        Node slave = makeASlaveAndConfigureJob(job);
        Build build = buildJobOnSlaveWithSuccess(job, slave);

        assertThat(build.getNode(), is(slave));
        assertThatBuildHasCheckstyleResults(job.getLastBuild());
        assertThatBuildHasCheckstyleResults(job);
    }

    /**
     * Sets up a list view with a warnings column. Builds a job and checks if the column shows the correct number of
     * warnings and provides a direct link to the actual warning results.
     */
    @Test
    @Category(SmokeTest.class)
    @Bug("24436")
    public void build_a_job_and_check_if_list_view_column_shows_correct_warnings() {
        MavenModuleSet job = setupSimpleMavenJob();
        buildJobAndWait(job).shouldSucceed();

        ListView view = addDashboardListViewColumn(CheckstyleListViewColumn.class);

        String relativeUrl = "job/" + job.name + "/checkstyle";
        By expectedDashboardLinkMatcher = by.css("a[href$='" + relativeUrl + "']");

        assertValidLink(expectedDashboardLinkMatcher);

        view.delete();
    }

    /**
     * Sets up a dashboard view with a warnings-per-project portlet. Builds a job and checks if the portlett shows the
     * correct number of warnings and provides a direct link to the actual warning results.
     */
    @Test
    @WithPlugins("dashboard-view")
    public void build_a_job_and_check_if_dashboard_portlet_shows_correct_warnings() {
        MavenModuleSet job = setupSimpleMavenJob();
        buildJobAndWait(job).shouldSucceed();

        DashboardView view = addDashboardViewAndBottomPortlet(CheckstyleWarningsPerProjectDashboardViewPortlet.class);

        By expectedDashboardLinkMatcher = by.css("a[href='job/" + job.name + "/checkstyle']");

        assertValidLink(expectedDashboardLinkMatcher);

        view.delete();
    }

    private void assertValidLink(final By warningsLinkMatcher) {
        assertThat(jenkins.all(warningsLinkMatcher).size(), is(1));
        WebElement link = jenkins.getElement(warningsLinkMatcher);
        assertThat(link.getText().trim(), is("12"));

        link.click();
        assertThat(driver, hasContent("CheckStyle Result"));
    }

    /**
     * Makes a Freestyle Job with Checkstyle and a warnings-file.
     *
     * @return The new Job
     */
    private FreeStyleJob setUpFreestyleJob() {
        AnalysisConfigurator<CheckstyleFreestyleBuildSettings> buildConfigurator = new AnalysisConfigurator<CheckstyleFreestyleBuildSettings>() {
            @Override
            public void configure(CheckstyleFreestyleBuildSettings settings) {
                settings.pattern.set(PATTERN_WITH_776_WARNINGS);
            }
        };
        return setUpFreestyleJob(buildConfigurator);
    }

    /**
     * Creates a sequence of Freestyle builds and checks if the build result is set correctly. New warning threshold is
     * set to zero, e.g. a new warning should mark a build as unstable.
     * <p/>
     * <ol>
     *     <li>Build 1: 1 new warning (SUCCESS since no reference build is set)</li>
     *     <li>Build 2: 2 new warnings (UNSTABLE since threshold is reached)</li>
     *     <li>Build 3: 1 new warning (UNSTABLE since still one warning is new based on delta with reference build)</li>
     *     <li>Build 4: 1 new warning (SUCCESS since no reference build is set)</li>
     * </ol>
     */
    @Test
    public void should_set_build_status_in_build_sequence() {
        FreeStyleJob job = setupJob(FILE_WITH_776_WARNINGS, FreeStyleJob.class, CheckstyleFreestyleBuildSettings.class, null);

        runBuild(job, 1, Result.SUCCESS, 1);
        runBuild(job, 2, Result.UNSTABLE, 2);
        runBuild(job, 3, Result.UNSTABLE, 1);
        runBuild(job, 4, Result.SUCCESS, 0);
    }

    private void runBuild(final FreeStyleJob job, final int number, final Result expectedResult, final int expectedNewWarnings) {
        final String fileName = "checkstyle-result-build" + number + ".xml";
        AnalysisConfigurator<CheckstyleFreestyleBuildSettings> buildConfigurator = new AnalysisConfigurator<CheckstyleFreestyleBuildSettings>() {
            @Override
            public void configure(CheckstyleFreestyleBuildSettings settings) {
                settings.setNewWarningsThresholdUnstable("0");
                settings.pattern.set(fileName);
            }
        };

        editJob(CHECKSTYLE_PLUGIN_ROOT + fileName, false, job,
                CheckstyleFreestyleBuildSettings.class, buildConfigurator);
        Build lastBuild = buildJobAndWait(job).shouldBe(expectedResult);

        if (expectedNewWarnings > 0) {
            assertThatBuildHasCheckstyleResults(lastBuild);
            lastBuild.open();
            CheckstyleAction checkstyle = new CheckstyleAction(job);
            assertThat(checkstyle.getNewWarningNumber(), is(expectedNewWarnings));
        }
    }
}
