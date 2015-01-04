package plugins;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.jenkinsci.test.acceptance.junit.Bug;
import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisConfigurator;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdAction;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdColumn;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdMavenBuildSettings;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdWarningsPerProjectDashboardViewPortlet;
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
 * Acceptance tests for the PMD plugin.
 */
@WithPlugins("pmd")
public class PmdPluginTest extends AbstractAnalysisTest {
    private static final String PMD_PLUGIN_ROOT = "/pmd_plugin/";
    private static final String PATTERN_WITHOUT_WARNINGS = "pmd.xml";
    private static final String PMD_FILE_WITHOUT_WARNINGS = PMD_PLUGIN_ROOT + PATTERN_WITHOUT_WARNINGS;
    private static final String PATTERN_WITH_9_WARNINGS = "pmd-warnings.xml";
    private static final String FILE_WITH_9_WARNINGS = PMD_PLUGIN_ROOT + PATTERN_WITH_9_WARNINGS;

    /**
     * Checks that the plug-in sends a mail after a build has been failed. The content of the mail
     * contains several tokens that should be expanded in the mail with the correct vaules.
     */
    @Test @WithPlugins("email-ext") @Bug("25501")
    public void should_send_mail_with_expanded_tokens() {
        setUpMailer();

        AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator =
                new AnalysisConfigurator<PmdFreestyleSettings>() {
                    @Override
                    public void configure(PmdFreestyleSettings settings) {
                        settings.setBuildFailedTotalAll("0");
                        settings.pattern.set(PATTERN_WITH_9_WARNINGS);
                    }
                };
        FreeStyleJob job = setupJob(FILE_WITH_9_WARNINGS, FreeStyleJob.class,
                PmdFreestyleSettings.class, buildConfigurator);

        configureEmailNotification(job, "PMD: ${PMD_RESULT}",
                "PMD: ${PMD_COUNT}-${PMD_FIXED}-${PMD_NEW}");

        job.startBuild().shouldFail();

        verifyReceivedMail("PMD: FAILURE", "PMD: 9-0-9");
    }

    /**
     * Configures a job with PMD and checks that the parsed PMD file does not contain warnings.
     */
    @Test
    public void configure_a_job_with_PMD_post_build_steps() {
        AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator = new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                settings.pattern.set(PATTERN_WITHOUT_WARNINGS);
            }
        };

        FreeStyleJob job = createFreestyleJob(buildConfigurator);

        Build lastBuild = buildJobWithSuccess(job);
        assertThatBuildHasNoWarnings(lastBuild);
    }

    private void assertThatBuildHasNoWarnings(final Build lastBuild) {
        assertThat(lastBuild.open(), hasContent("0 warnings"));
    }

    private FreeStyleJob createFreestyleJob(final AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator) {
        return createFreeStyleJob(PMD_FILE_WITHOUT_WARNINGS, buildConfigurator);
    }

    private FreeStyleJob createFreeStyleJob(final String fileName, final AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator) {
        return setupJob(fileName, FreeStyleJob.class, PmdFreestyleSettings.class, buildConfigurator);
    }

    /**
     * Checks that PMD runs even if the build failed if the property 'canRunOnFailed' is set.
     */
    @Test
    public void configure_a_job_with_PMD_post_build_steps_run_always() {
        AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator = new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                settings.pattern.set(PATTERN_WITHOUT_WARNINGS);
                settings.setCanRunOnFailed(true);
            }
        };

        FreeStyleJob job = createFreestyleJob(buildConfigurator);

        job.configure();
        job.addShellStep("false");
        job.save();

        Build lastBuild = job.startBuild().waitUntilFinished().shouldFail();
        assertThatBuildHasNoWarnings(lastBuild);
    }

    /**
     * Configures a job with PMD and checks that the parsed PMD file contains 9 warnings.
     */
    @Test
    public void configure_a_job_with_PMD_post_build_steps_which_display_some_warnings() {
        AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator = new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                settings.pattern.set(PATTERN_WITH_9_WARNINGS);
            }
        };

        FreeStyleJob job = createFreeStyleJob(FILE_WITH_9_WARNINGS, buildConfigurator);

        Build lastBuild = buildJobWithSuccess(job);
        assertThatPageHasPmdResults(lastBuild);

        lastBuild.open();

        PmdAction action = new PmdAction(job);
        assertThat(action.getResultLinkByXPathText("9 warnings"), is("pmdResult"));
        assertThat(action.getResultLinkByXPathText("9 new warnings"), is("pmdResult/new"));
        assertThat(action.getWarningNumber(), is(9));
        assertThat(action.getNewWarningNumber(), is(9));
        assertThat(action.getFixedWarningNumber(), is(0));
        assertThat(action.getHighWarningNumber(), is(0));
        assertThat(action.getNormalWarningNumber(), is(3));
        assertThat(action.getLowWarningNumber(), is(6));
        assertFileTab(action);
        assertTypeTab(action);
        assertWarningsTab(action);
    }

    private void assertFileTab(PmdAction pa) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("ChannelContentAPIClient.m", 6);
        expectedContent.put("ProductDetailAPIClient.m", 2);
        expectedContent.put("ViewAllHoldingsAPIClient.m", 1);
        assertThat(pa.getFileTabContents(), is(expectedContent));
    }

    private void assertTypeTab(PmdAction pa) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("long line", 6);
        expectedContent.put("unused method parameter", 3);
        assertThat(pa.getTypesTabContents(), is(expectedContent));
    }

    private void assertWarningsTab(PmdAction pa) {
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
        assertThat(pa.getWarningsTabContents(), is(expectedContent));
    }

    /**
     * Builds a job and tests if the PMD api (with depth=0 parameter set) responds with the expected output.
     * Difference in whitespaces are ok.
     */
    @Test
    @Category(SmokeTest.class)
    public void xml_api_report_depth_0() throws IOException, SAXException, ParserConfigurationException {
        AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator = new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                settings.pattern.set(PATTERN_WITH_9_WARNINGS);
            }
        };

        FreeStyleJob job = createFreeStyleJob(FILE_WITH_9_WARNINGS, buildConfigurator);

        Build build = buildJobWithSuccess(job);
        String apiUrl = "pmdResult/api/xml?depth=0";
        String expectedXmlPath = PMD_PLUGIN_ROOT + "api_depth_0.xml";
        assertXmlApiMatchesExpected(build, apiUrl, expectedXmlPath);
    }

    /**
     * Runs job two times to check if new and fixed warnings are displayed.
     */
    @Test
    public void configure_a_job_with_PMD_post_build_steps_which_display_some_warnings_two_runs() {
        FreeStyleJob job = setUpPmdFreestyleJob();
        buildJobAndWait(job);

        editJob(PMD_PLUGIN_ROOT + "forSecondRun/pmd-warnings.xml", false, job);
        Build lastBuild = buildJobWithSuccess(job);
        assertThatPageHasPmdResults(lastBuild);
        lastBuild.open();

        PmdAction action = new PmdAction(job);
        assertThat(action.getResultLinkByXPathText("8 warnings"), is("pmdResult"));
        assertThat(action.getResultLinkByXPathText("1 new warning"), is("pmdResult/new"));
        assertThat(action.getResultLinkByXPathText("2 fixed warnings"), is("pmdResult/fixed"));
        assertThat(action.getWarningNumber(), is(8));
        assertThat(action.getNewWarningNumber(), is(1));
        assertThat(action.getFixedWarningNumber(), is(2));
        assertThat(action.getHighWarningNumber(), is(0));
        assertThat(action.getNormalWarningNumber(), is(2));
        assertThat(action.getLowWarningNumber(), is(6));
    }

    /**
     * Runs job two times to check if the links of the graph are relative.
     */
    @Test
    @Bug("21723")
    @Ignore("Until JENKINS-21723 is fixed")
    public void view_pmd_report_job_graph_links() {
        FreeStyleJob job = setUpPmdFreestyleJob();
        buildJobAndWait(job);
        editJob(PMD_PLUGIN_ROOT + "forSecondRun/pmd-warnings.xml", false, job);
        buildJobWithSuccess(job);

        assertAreaLinksOfJobAreLike(job, "^\\d+/pmdResult");
    }

    /*
     * Runs a job with warning threshold configured once and validates that build is marked as unstable.
     */
    @Test
    @Bug("19614")
    public void build_with_warning_threshold_set_should_be_unstable() {
        AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator = new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                settings.pattern.set(PATTERN_WITH_9_WARNINGS);
                settings.setBuildUnstableTotalAll("0");
                settings.setNewWarningsThresholdFailed("0");
                settings.setUseDeltaValues(true);
            }
        };
        FreeStyleJob job = createFreeStyleJob(FILE_WITH_9_WARNINGS, buildConfigurator);

        Build build = buildJobAndWait(job);
        assertThat(build.isUnstable(), is(true));
    }

    private MavenModuleSet setupSimpleMavenJob() {
        return setupSimpleMavenJob(null);
    }

    private MavenModuleSet setupSimpleMavenJob(AnalysisConfigurator<PmdMavenBuildSettings> configurator) {
        String projectPath = PMD_PLUGIN_ROOT + "sample_pmd_project";
        String goal = "clean package pmd:pmd";
        return setupMavenJob(projectPath, goal, PmdMavenBuildSettings.class, configurator);
    }

    /**
     * Builds a freestyle project and checks if new warning are displayed.
     */
    @Test
    public void build_simple_freestyle_mavengoals_project() {
        AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator = new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                settings.pattern.set("target/pmd.xml");
            }
        };
        FreeStyleJob job = setupJob(PMD_PLUGIN_ROOT + "sample_pmd_project", FreeStyleJob.class, PmdFreestyleSettings.class, buildConfigurator, "clean package pmd:pmd"
        );
        Build lastBuild = buildJobWithSuccess(job);
        assertThatPageHasPmdResults(lastBuild);
        lastBuild.open();
        PmdAction pmd = new PmdAction(job);
        assertThat(pmd.getNewWarningNumber(), is(2));
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("Main.java:9", 9);
        expectedContent.put("Main.java:13", 13);
// TODO decision of uhafner
//        assertThat(pmd.getWarningsTabContents(), is(expectedContent));
//        assertThat(pmd.getLinkedSourceFileLineNumber("Warnings", "Main.java:9", "High"), is(9));
//        assertThat(pmd.getLinkedSourceFileLineAsString("Warnings", "Main.java:13", "High"), endsWith("if(false) {"));
    }

    /**
     * Builds a maven project and checks if new warnings are displayed.
     */
    @Test
    public void build_simple_maven_project() {
        MavenModuleSet job = setupSimpleMavenJob();
        Build lastBuild = buildJobWithSuccess(job);
        assertThatPageHasPmdResults(lastBuild);
        lastBuild.open();
        PmdAction pmd = new PmdAction(job);
        assertThat(pmd.getNewWarningNumber(), is(2));
    }

    private void assertThatPageHasPmdResults(final PageObject page) {
        assertThat(page, hasAction("PMD Warnings"));
    }

    /**
     * Builds a maven project and checks if it is unstable.
     */
    @Test
    public void build_simple_maven_project_and_check_if_it_is_unstable() {
        AnalysisConfigurator<PmdMavenBuildSettings> buildConfigurator =
                new AnalysisConfigurator<PmdMavenBuildSettings>() {
                    @Override
                    public void configure(PmdMavenBuildSettings settings) {
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
        AnalysisConfigurator<PmdMavenBuildSettings> buildConfigurator =
                new AnalysisConfigurator<PmdMavenBuildSettings>() {
                    @Override
                    public void configure(PmdMavenBuildSettings settings) {
                        settings.setBuildFailedTotalAll("0");
                    }
                };
        MavenModuleSet job = setupSimpleMavenJob(buildConfigurator);
        buildJobAndWait(job).shouldFail();
    }

    /**
     * Builds a job on a slave with pmd and verifies that the information pmd provides in the tabs about the build
     * are the information we expect.
     */
    @Test
    public void configure_a_job_with_PMD_post_build_steps_build_on_slave() throws Exception {
        FreeStyleJob job = setUpPmdFreestyleJob();
        Node slave = makeASlaveAndConfigureJob(job);
        Build build = buildJobOnSlaveWithSuccess(job, slave);
        assertThat(build.getNode(), is(slave));
        assertThatPageHasPmdResults(build);
        assertThatPageHasPmdResults(job);
    }

    /**
     * Build a job and check set up a dashboard list-view. Check, if the dashboard view shows correct warning count.
     */
    @Test
    public void build_a_job_and_check_if_dashboard_list_view_shows_correct_warnings() {
        MavenModuleSet job = setupSimpleMavenJob();
        buildJobAndWait(job).shouldSucceed();
        ListView view = addDashboardListViewColumn(PmdColumn.class);

        By expectedDashboardLinkMatcher = by.css("a[href$='job/" + job.name + "/pmd']");
        assertThat(jenkins.all(expectedDashboardLinkMatcher).size(), is(1));
        WebElement dashboardLink = jenkins.getElement(expectedDashboardLinkMatcher);
        assertThat(dashboardLink.getText().trim(), is("2"));

        view.delete();
    }

    /**
     * Build a job and check set up a "dashboard"-style view. Check, if the dashboard view shows correct warning count.
     */
    @Test
    @WithPlugins("dashboard-view")
    public void build_a_job_and_check_if_dashboard_view_shows_correct_warnings() {
        MavenModuleSet job = setupSimpleMavenJob();
        buildJobAndWait(job).shouldSucceed();

        DashboardView view = addDashboardViewAndBottomPortlet(PmdWarningsPerProjectDashboardViewPortlet.class);

        By expectedDashboardLinkMatcher = by.css("a[href='job/" + job.name + "/pmd']");
        assertThat(jenkins.all(expectedDashboardLinkMatcher).size(), is(1));
        WebElement dashboardLink = jenkins.getElement(expectedDashboardLinkMatcher);
        assertThat(dashboardLink.getText().trim(), is("2"));

        view.delete();
    }

    /**
     * Makes a Freestyle Job with PMD and a warnings-file.
     *
     * @return The new Job
     */
    private FreeStyleJob setUpPmdFreestyleJob() {
        AnalysisConfigurator<PmdFreestyleSettings> buildConfigurator = new AnalysisConfigurator<PmdFreestyleSettings>() {
            @Override
            public void configure(PmdFreestyleSettings settings) {
                settings.pattern.set(PATTERN_WITH_9_WARNINGS);
            }
        };
        return createFreeStyleJob(FILE_WITH_9_WARNINGS, buildConfigurator);
    }

    /**
     * Creates a sequence of Freestyle builds and checks if the build result is set correctly. New warning threshold is
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
    public void should_set_build_status_in_build_sequence_compare_to_reference_build() {
        FreeStyleJob job = setupJob(FILE_WITH_9_WARNINGS, FreeStyleJob.class, PmdFreestyleSettings.class, null);

        runBuild(job, 1, Result.SUCCESS, 1, false);
        runBuild(job, 2, Result.UNSTABLE, 2, false);
        runBuild(job, 3, Result.UNSTABLE, 1, false);
        runBuild(job, 4, Result.SUCCESS, 0, false);
    }

    /**
     * Creates a sequence of Freestyle builds and checks if the build result is set correctly. New warning threshold is
     * set to zero, e.g. a new warning should mark a build as unstable.
     * <p/>
     * <ol>
     *     <li>Build 1: 1 new warning (SUCCESS since no reference build is set)</li>
     *     <li>Build 2: 2 new warnings (UNSTABLE since threshold is reached)</li>
     *     <li>Build 3: 1 new warning (SUCCESS since all warnings of previous build are fixed)</li>
     * </ol>
     */
    @Test
    @Bug("13458")
    public void should_set_build_status_in_build_sequence_compare_to_previous_build() {
        FreeStyleJob job = setupJob(FILE_WITH_9_WARNINGS, FreeStyleJob.class, PmdFreestyleSettings.class, null);

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

        editJob(PMD_PLUGIN_ROOT + fileName, false, job,
                PmdFreestyleSettings.class, buildConfigurator);
        Build lastBuild = buildJobAndWait(job).shouldBe(expectedResult);

        if (expectedNewWarnings > 0) {
            assertThatPageHasPmdResults(lastBuild);
            lastBuild.open();
            PmdAction pmd = new PmdAction(job);
            assertThat(pmd.getNewWarningNumber(), is(expectedNewWarnings));
        }
    }
}
