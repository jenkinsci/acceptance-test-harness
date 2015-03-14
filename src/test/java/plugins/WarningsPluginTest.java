package plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.jvnet.hudson.test.Issue;
import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisConfigurator;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsAction;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsBuildSettings;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsColumn;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.MatrixConfiguration;
import org.jenkinsci.test.acceptance.po.MatrixProject;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Tests various aspects of the warnings plug-in. Most tests copy an existing file with several warnings into the
 * workspace. This file is then analyzed by console and workspace parsers.
 */
@WithPlugins("warnings")
public class WarningsPluginTest extends AbstractAnalysisTest {
    /** Contains warnings for Javac parser. Warnings have file names preset for include/exclude filter tests. */
    private static final String WARNINGS_FILE_FOR_INCLUDE_EXCLUDE_TESTS = "/warnings_plugin/warningsForRegEx.txt";
    private static final String SEVERAL_PARSERS_FILE_NAME = "warningsAll.txt";
    /** Contains warnings for several parsers. */
    private static final String SEVERAL_PARSERS_FILE_FULL_PATH = "/warnings_plugin/" + SEVERAL_PARSERS_FILE_NAME;

    private static final int JAVA_COUNT = 131;
    private static final int JAVADOC_COUNT = 8;
    private static final int MSBUILD_COUNT = 15;
    private static final int TOTAL = JAVA_COUNT + JAVADOC_COUNT + MSBUILD_COUNT;

    /**
     * Build a matrix job with three configurations. For each configuration a different set of warnings will be parsed
     * with the same parser (GCC). After the successful build the total number of warnings at the root level should be
     * set to 12 (sum of all three configurations). Moreover, for each configuration the total number of warnings is
     * also verified (4, 6, and 2 warnings).
     */
    // TODO: run the job twice and check for the graphs
    @Test @Issue({"JENKINS-11225", "JENKINS-26913"})
    public void should_report_warnings_per_axis() {
        String file = "matrix-warnings.txt";
        MatrixProject job = setupJob("/warnings_plugin/" + file, MatrixProject.class,
                WarningsBuildSettings.class, new AnalysisConfigurator<WarningsBuildSettings>() {
                    @Override
                    public void configure(WarningsBuildSettings settings) {
                        settings.addConsoleScanner("GNU C Compiler 4 (gcc)");
                    }
                });

        job.configure();
        job.addUserAxis("user_axis", "one two three");
        job.addShellStep("cat " + file + "| grep $user_axis");
        job.save();

        Build build = buildJobAndWait(job).shouldSucceed();

        String title = "GNU C Compiler Warnings";
        assertThatActionExists(job, build, title);
        build.open();
        assertThat(driver, hasContent(title + ": 12"));

        Map<String, Integer> warningsPerAxis = new HashMap<>();
        warningsPerAxis.put("user_axis=one", 4);
        warningsPerAxis.put("user_axis=two", 6);
        warningsPerAxis.put("user_axis=three", 2);
        for (MatrixConfiguration axis : job.getConfigurations()) {
            Build axisBuild = axis.getLastBuild();
            assertThat(axisBuild, hasAction(title));
            assertThat(driver, hasContent(title + ": " + warningsPerAxis.get(axis.name)));
        }

        assertThatConfigurationTabIsCorrectlyFilled(job);
        assertThatFoldersTabIsCorrectlyFilled(job);
        assertThatFilesTabIsCorrectlyFilled(job);
    }

    private void assertThatConfigurationTabIsCorrectlyFilled(final MatrixProject job) {
        SortedMap<String, Integer> expectedConfigurationDetails = new TreeMap<>();
        expectedConfigurationDetails.put("user_axis=one", 4);
        expectedConfigurationDetails.put("user_axis=two", 6);
        expectedConfigurationDetails.put("user_axis=three", 2);
        WarningsAction action = new WarningsAction(job);
        assertThat(action.getModulesTabContents(), is(expectedConfigurationDetails));
    }

    private void assertThatFoldersTabIsCorrectlyFilled(final MatrixProject job) {
        SortedMap<String, Integer> expectedConfigurationDetails = new TreeMap<>();
        expectedConfigurationDetails.put("axis/one", 4);
        expectedConfigurationDetails.put("axis/two", 6);
        expectedConfigurationDetails.put("axis/three", 2);
        WarningsAction action = new WarningsAction(job);
        assertThat(action.getPackagesTabContents(), is(expectedConfigurationDetails));
    }

    private void assertThatFilesTabIsCorrectlyFilled(final MatrixProject job) {
        SortedMap<String, Integer> expectedConfigurationDetails = new TreeMap<>();
        WarningsAction action = new WarningsAction(job);
        expectedConfigurationDetails.put("FileOne.c", 4);
        expectedConfigurationDetails.put("FileTwo.c", 6);
        expectedConfigurationDetails.put("FileThree.c", 2);
        assertThat(action.getFileTabContents(), is(expectedConfigurationDetails));
    }

    /**
     * Checks that the plug-in sends a mail after a build has been failed. The content of the mail contains several
     * tokens that should be expanded in the mail with the correct vaules.
     */
    @Test @Issue("25501") @Category(SmokeTest.class) @WithPlugins("email-ext")
    public void should_send_mail_with_expanded_tokens() {
        setUpMailer();

        AnalysisConfigurator<WarningsBuildSettings> buildConfigurator = new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
                settings.setBuildFailedTotalAll("0");
            }
        };
        FreeStyleJob job = setupJob(SEVERAL_PARSERS_FILE_FULL_PATH, FreeStyleJob.class,
                WarningsBuildSettings.class, buildConfigurator);

        configureEmailNotification(job, "Warnings: ${WARNINGS_RESULT}",
                "Warnings: ${WARNINGS_COUNT}-${WARNINGS_FIXED}-${WARNINGS_NEW}");

        job.startBuild().shouldFail();

        verifyReceivedMail("Warnings: FAILURE", "Warnings: 131-0-131");
    }

    /**
     * Checks that no warnings are reported if the build does nothing.
     */
    @Test
    public void should_detect_no_errors_in_console_log_and_workspace_when_there_are_none() {
        FreeStyleJob job = setupJob(null, FreeStyleJob.class, WarningsBuildSettings.class,
                new AnalysisConfigurator<WarningsBuildSettings>() {
                    @Override
                    public void configure(WarningsBuildSettings settings) {
                        settings.addConsoleScanner("Maven");
                        settings.addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
                    }
                });

        Build build = buildJobWithSuccess(job);

        assertThatActionIsMissing(job, build, "Java Warnings");
        assertThatActionIsMissing(job, build, "Maven Warnings");

        build.open();
        assertThat(driver, hasContent("Java Warnings: 0"));
        assertThat(driver, hasContent("Maven Warnings: 0"));
    }

    /**
     * Checks that no warnings are reported if these are located in a different file.
     */
    @Test
    public void should_not_detect_errors_in_ignored_parts_of_the_workspace() {
        FreeStyleJob job = setupJob(null, FreeStyleJob.class, WarningsBuildSettings.class,
                new AnalysisConfigurator<WarningsBuildSettings>() {
                    @Override
                    public void configure(WarningsBuildSettings settings) {
                        settings.addWorkspaceFileScanner("Maven", "no_errors_here.log");
                    }
                });

        job.configure();
        job.addShellStep("mvn clean install > errors.log || true");
        job.save();

        Build build = buildJobWithSuccess(job);

        assertThatActionIsMissing(job, build, "Maven Warnings");

        build.open();
        assertThat(driver, hasContent("Maven Warnings: 0"));
    }

    /**
     * Build a job and check set up a dashboard list-view. Check, if the dashboard view shows correct warning count.
     */
    @Test
    public void build_a_freestyle_job_and_check_if_dashboard_list_view_shows_correct_warnings() {
        FreeStyleJob job = setupJob(SEVERAL_PARSERS_FILE_FULL_PATH, FreeStyleJob.class,
                WarningsBuildSettings.class, create3ParserConfiguration());

        catWarningsToConsole(job);

        buildJobAndWait(job).shouldSucceed();

        verifyWarningsColumn(job);
    }

    /**
     * Build a job and check set up a dashboard list-view. Check, if the dashboard view shows correct warning count.
     */
    @Test
    @Issue("JENKINS-23446")
    public void build_a_matrix_project_and_check_if_dashboard_list_view_shows_correct_warnings() {
        MatrixProject job = setupJob(SEVERAL_PARSERS_FILE_FULL_PATH, MatrixProject.class,
                WarningsBuildSettings.class, create3ParserConfiguration());

        catWarningsToConsole(job);

        buildJobAndWait(job).shouldSucceed();

        verifyWarningsColumn(job);
    }

    private void verifyWarningsColumn(final Job job) {
        ListView view = addDashboardListViewColumn(WarningsColumn.class);

        By expectedDashboardLinkMatcher = by.css("a[href$='job/" + job.name + "/warnings']");
        assertThat(jenkins.all(expectedDashboardLinkMatcher).size(), is(1));
        WebElement dashboardLink = jenkins.getElement(expectedDashboardLinkMatcher);
        assertThat(dashboardLink.getText().trim(), is(String.valueOf(TOTAL)));

        view.delete();
    }

    /**
     * Checks that warning results are correctly created for a matrix job with the parsers "Java", "JavaDoc" and
     * "MSBuild" if the console log contains multiple warnings of these types.
     */
    @Test
    public void detect_warnings_of_multiple_compilers_in_console_matrix() {
        MatrixProject job = setupJob(SEVERAL_PARSERS_FILE_FULL_PATH, MatrixProject.class,
                WarningsBuildSettings.class, create3ParserConfiguration());

        catWarningsToConsole(job);

        job.configure();
        job.addUserAxis("user_axis", "axis1 axis2 axis3");
        job.save();

        verify3ParserResults(job, 3);
    }

    /**
     * Runs a job with warning threshold configured once and validates that build is marked as unstable.
     */
    @Test
    @Issue("JENKINS-19614")
    public void should_set_build_to_unstable_if_total_warnings_threshold_set() {
        AnalysisConfigurator<WarningsBuildSettings> buildConfiguration = new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addConsoleScanner("Java Compiler (javac)");
                settings.addConsoleScanner("JavaDoc Tool");
                settings.addConsoleScanner("MSBuild");
                settings.setBuildUnstableTotalAll("0");
                settings.setNewWarningsThresholdFailed("0");
                settings.setUseDeltaValues(true);
            }
        };
        FreeStyleJob job = setupJob(SEVERAL_PARSERS_FILE_FULL_PATH, FreeStyleJob.class,
                WarningsBuildSettings.class, buildConfiguration);

        catWarningsToConsole(job);
        buildJobAndWait(job).shouldBeUnstable();
    }

    /**
     * Checks that warning results are correctly created for a freestyle project with the parsers "Java", "JavaDoc" and
     * "MSBuild" if the console log contains multiple warnings of these types.
     */
    @Test
    public void detect_warnings_of_multiple_compilers_in_console_freestyle() {
        FreeStyleJob job = setupJob(SEVERAL_PARSERS_FILE_FULL_PATH, FreeStyleJob.class,
                WarningsBuildSettings.class, create3ParserConfiguration());

        catWarningsToConsole(job);

        verify3ParserResults(job, 1);
    }

    private void catWarningsToConsole(final Job job) {
        job.configure();
        job.addShellStep("cat " + SEVERAL_PARSERS_FILE_NAME);
        job.save();
    }

    private AnalysisConfigurator<WarningsBuildSettings> create3ParserConfiguration() {
        return new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addConsoleScanner("Java Compiler (javac)");
                settings.addConsoleScanner("JavaDoc Tool");
                settings.addConsoleScanner("MSBuild");
            }
        };
    }

    /**
     * Checks that warning results are correctly created for the workspace parsers "Java", "JavaDoc" and "MSBuild" if a
     * file with multiple warnings of these types is copied to the workspace.
     */
    @Test
    public void detect_warnings_of_multiple_compilers_in_workspace() {
        AnalysisConfigurator<WarningsBuildSettings> buildConfigurator = new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
                settings.addWorkspaceFileScanner("JavaDoc Tool", "**/*");
                settings.addWorkspaceFileScanner("MSBuild", "**/*");
            }
        };
        FreeStyleJob job = setupJob(SEVERAL_PARSERS_FILE_FULL_PATH, FreeStyleJob.class,
                WarningsBuildSettings.class, buildConfigurator);

        verify3ParserResults(job, 1);
    }

    private void verify3ParserResults(final Job job, final int numberOfRuns) {
        Build build = buildJobWithSuccess(job);
        assertThatActionExists(job, build, "Java Warnings");
        assertThatActionExists(job, build, "JavaDoc Warnings");
        assertThatActionExists(job, build, "MSBuild Warnings");

        build.open();
        assertThat(driver, hasContent("Java Warnings: " + JAVA_COUNT * numberOfRuns));
        assertThat(driver, hasContent("JavaDoc Warnings: " + JAVADOC_COUNT * numberOfRuns));
        assertThat(driver, hasContent("MSBuild Warnings: " + MSBUILD_COUNT * numberOfRuns));
    }

    /**
     * Checks that the warnings plugin will be skipped if "Run always" is not checked.
     */
    @Test
    public void skip_failed_builds() {
        FreeStyleJob job = runBuildWithRunAlwaysOption(false);
        Build build = buildJobAndWait(job).shouldFail();

        assertThatActionIsMissing(job, build, "Java Warnings");

        build.open();
        assertThat(driver, not(hasContent("Java Warnings:")));
    }

    private void assertThatActionIsMissing(final FreeStyleJob job, final Build build, final String parser) {
        assertThat(build, not(hasAction(parser)));
        assertThat(job.getLastBuild(), not(hasAction("Java Warnings")));
    }

    /**
     * Checks that the warnings plugin will not skip build results if "Run always" is checked.
     */
    @Test
    public void do_not_skip_failed_builds_with_option_run_always() {
        FreeStyleJob job = runBuildWithRunAlwaysOption(true);
        Build build = buildJobAndWait(job).shouldFail();

        assertThatActionExists(job, build, "Java Warnings");

        WarningsAction action = new WarningsAction(job);
        assertThatWarningsCountIs(action, 131);
        assertThatNewWarningsCountIs(action, 131);
    }

    private FreeStyleJob runBuildWithRunAlwaysOption(final boolean canRunOnFailed) {
        AnalysisConfigurator<WarningsBuildSettings> buildConfigurator = new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
                settings.setCanRunOnFailed(canRunOnFailed);
            }
        };
        FreeStyleJob job = setupJob(SEVERAL_PARSERS_FILE_FULL_PATH, FreeStyleJob.class,
                WarningsBuildSettings.class, buildConfigurator);

        job.configure();
        job.addShellStep("exit 1");
        job.save();

        return job;
    }

    /**
     * Checks whether the warnings plugin finds one Maven warning in the console log. The result should be a build with
     * 1 Maven Warning.
     */
    @Test
    public void detect_errors_in_console_log() {
        AnalysisConfigurator<WarningsBuildSettings> buildConfigurator = new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addConsoleScanner("Maven");
            }
        };
        FreeStyleJob job = setupJob(null, FreeStyleJob.class,
                WarningsBuildSettings.class, buildConfigurator);

        job.configure();
        job.addShellStep("mvn clean install || true");
        job.save();

        Build build = buildJobWithSuccess(job);
        assertThatActionExists(job, build, "Maven Warnings");

        WarningsAction action = new WarningsAction(job);
        assertThatWarningsCountIs(action, 1);
        assertThatNewWarningsCountIs(action, 1);

        assertThat(action.getNewWarningNumber(), is(1));
        assertThat(action.getWarningNumber(), is(1));
        assertThat(action.getFixedWarningNumber(), is(0));
        assertThat(action.getHighWarningNumber(), is(1));
        assertThat(action.getNormalWarningNumber(), is(0));
        assertThat(action.getLowWarningNumber(), is(0));
    }

    private void assertThatActionExists(final Job job, final Build build, final String parser) {
        assertThat(build, hasAction(parser));
        assertThat(job.getLastBuild(), hasAction(parser));
    }

    /**
     * Checks whether the warnings plugin picks only specific warnings. The warnings to exclude are given by three
     * exclude patterns {".*ignore1.*, .*ignore2.*, .*default.*"}. The result should be a build with 4 Java Warnings
     * (from a file that contains 9 warnings).
     */
    @Test
    public void skip_warnings_in_ignored_parts() {
        AnalysisConfigurator<WarningsBuildSettings> buildConfigurator = new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
                settings.addWarningsToInclude(".*/.*");
                settings.addWarningsToIgnore(".*/ignore1/.*, .*/ignore2/.*, .*/default/.*");
            }
        };
        FreeStyleJob job = setupJob(WARNINGS_FILE_FOR_INCLUDE_EXCLUDE_TESTS, FreeStyleJob.class,
                WarningsBuildSettings.class, buildConfigurator);

        Build build = buildJobWithSuccess(job);
        assertThatActionExists(job, build, "Java Warnings");

        WarningsAction action = new WarningsAction(job);
        assertThatWarningsCountIs(action, 4);
        assertThatNewWarningsCountIs(action, 4);
    }

    /**
     * Checks whether the warnings plugin picks only specific warnings. The warnings to include are given by two include
     * patterns {".*include.*", ".*default.*"}. The result should be a build with 5 Java Warnings (from a file that
     * contains 9 warnings).
     */
    @Test
    public void include_warnings_specified_in_included_parts() {
        AnalysisConfigurator<WarningsBuildSettings> buildConfigurator = new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
                settings.addWarningsToInclude(".*/include*/.*, .*/default/.*");
            }
        };
        FreeStyleJob job = setupJob(WARNINGS_FILE_FOR_INCLUDE_EXCLUDE_TESTS, FreeStyleJob.class,
                WarningsBuildSettings.class, buildConfigurator);
        Build build = buildJobWithSuccess(job);
        assertThatActionExists(job, build, "Java Warnings");

        WarningsAction action = new WarningsAction(job);
        assertThatWarningsCountIs(action, 5);
        assertThatNewWarningsCountIs(action, 5);

        assertThat(action.getWarningNumber(), is(5));
        assertThat(action.getNewWarningNumber(), is(5));
        assertThat(action.getFixedWarningNumber(), is(0));
        assertThat(action.getHighWarningNumber(), is(0));
        assertThat(action.getNormalWarningNumber(), is(5));
        assertThat(action.getLowWarningNumber(), is(0));
    }

    private void assertThatWarningsCountIs(final WarningsAction action, final int numberOfWarnings) {
        assertThat(action.getResultLinkByXPathText(numberOfWarnings + " warning" + plural(numberOfWarnings)),
                containsRegexp("warnings.*Result"));
    }

    private String plural(final int numberOfWarnings) {
        return numberOfWarnings == 1 ? StringUtils.EMPTY : "s";
    }

    private void assertThatNewWarningsCountIs(final WarningsAction action, final int numberOfNewWarnings) {
        assertThat(action.getResultLinkByXPathText(numberOfNewWarnings + " new warning" + plural(numberOfNewWarnings)),
                containsRegexp("warnings.*Result/new"));
    }
}