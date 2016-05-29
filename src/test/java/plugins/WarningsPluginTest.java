package plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisConfigurator;
import org.jenkinsci.test.acceptance.plugins.envinject.EnvInjectConfig;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsAction;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsBuildSettings;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsColumn;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.FreeStyleMultiBranchJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.MatrixConfiguration;
import org.jenkinsci.test.acceptance.po.MatrixProject;
import org.jenkinsci.test.acceptance.po.Node;
import org.jenkinsci.test.acceptance.po.StringParameter;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.jvnet.hudson.test.Issue;
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
public class WarningsPluginTest extends AbstractAnalysisTest<WarningsAction> {
    private static final String RESOURCES = "/warnings_plugin/";
    /** Contains warnings for Javac parser. Warnings have file names preset for include/exclude filter tests. */
    private static final String WARNINGS_FILE_FOR_INCLUDE_EXCLUDE_TESTS = RESOURCES + "warningsForRegEx.txt";
    private static final String SEVERAL_PARSERS_FILE_NAME = "warningsAll.txt";
    /** Contains warnings for several parsers. */
    private static final String SEVERAL_PARSERS_FILE_FULL_PATH = RESOURCES + SEVERAL_PARSERS_FILE_NAME;

    private static final int JAVA_COUNT = 131;
    private static final int JAVADOC_COUNT = 8;
    private static final int MSBUILD_COUNT = 15;
    private static final int TOTAL = JAVA_COUNT + JAVADOC_COUNT + MSBUILD_COUNT;
    private static final String JAVA_COMPILER = "Java Compiler (javac)";
    private static final String MS_BUILD = "MSBuild";
    private static final String JAVA_DOC = "JavaDoc Tool";
    private static final String CLANG = "Clang (LLVM based)";

    @Override
    protected WarningsAction createProjectAction(final FreeStyleJob job) {
        return new WarningsAction(job);
    }

    @Override
    protected WarningsAction createResultAction(final Build build) {
        return new WarningsAction(build);
    }

    @Override
    protected FreeStyleJob createFreeStyleJob() {
        FreeStyleJob job = createFreeStyleJob(new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addConsoleParser(JAVA_COMPILER);
            }
        });
        catWarningsToConsole(job);
        return job;
    }

    @Override
    protected int getNumberOfWarnings() {
        return 131;
    }

    /**
     * Runs a pipeline script that compiles a file with some warnings. The warnings plug-in should find all
     * 6 warnings.
     */
    @Test
    @WithPlugins("workflow-aggregator") @Issue("32191")
    public void should_find_warnings_without_sleep() {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(
                "node {\n" +
                        "    writeFile(file: \"a.c\", text: '''\n" +
                        "#include <sys/types.h>\n" +
                        "#include <stdlib.h>\n" +
                        "\n" +
                        "void\n" +
                        "func1(void)\n" +
                        "{\n" +
                        "}\n" +
                        "\n" +
                        "int\n" +
                        "main(int argc, char *argv[])\n" +
                        "{\n" +
                        "    char *a;\n" +
                        "    int64_t *b;\n" +
                        "    b = (int64_t *)a;\n" +
                        "    printf(\"Hi\");\n" +
                        "    printf(NULL);\n" +
                        "    printf(\"%s %d\\\\n\", \"35\");\n" +
                        "    func1();\n" +
                        "}\n" +
                        "'''\n" +
                        "    )\n" +
                        "    sh \"cc -Wall -W -Wcast-align -o output a.c\"\n" +
                        "    step([$class: 'WarningsPublisher',\n" +
                        "         canComputeNew: false,\n" +
                        "         canResolveRelativePaths: false,\n" +
                        "         consoleParsers: [[parserName: 'Clang (LLVM based)']],\n" +
                        "         defaultEncoding: '',\n" +
                        "         excludePattern: '',\n" +
                        "         healthy: '',\n" +
                        "         includePattern: '',\n" +
                        "         messagesPattern: '',\n" +
                        "         unHealthy: ''])\n" +
                        "}\n");
        job.sandbox.check();
        job.save();
        Build build = job.startBuild();
        build.shouldSucceed();

        assertThat(job.getLastBuild(), hasAction("LLVM/Clang Warnings"));
        build.open();
        assertThat(driver, hasContent(Pattern.compile("LLVM/Clang Warnings: [1-9][0-9]*"))); // some warnings, number depends on actual compiler version
    }

    /**
     * Verifies that environment variables are expanded in the file name pattern.
     */
    @Test @Issue("JENKINS-34157") @WithPlugins("envinject")
    public void should_resolve_environment_variables() {
        FreeStyleJob job = createFreeStyleJob(RESOURCES + "jenkins-32150", new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceScanner(CLANG, "${ENV_PREFIX}/${PARAMETER}");
                settings.addWorkspaceScanner(JAVA_COMPILER, "${BUILD_NUMBER}/nothing");
                settings.addWorkspaceScanner(JAVA_DOC, "${BUILD_NUMBER}_${Reference}\\ND4\\ReleaseTools\\Build\\Log\\warning.log");
            }
        });

        job.configure();
        new EnvInjectConfig.Environment(job).properties.sendKeys("ENV_PREFIX=**");
        String parameter = "PARAMETER";
        job.addParameter(StringParameter.class).setName(parameter);
        String reference = "Reference";
        job.addParameter(StringParameter.class).setName(reference);
        job.save();

        Node slave = createSlaveForJob(job);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("slavename", slave.getName());
        parameters.put(parameter, "compile-log.txt");
        parameters.put(reference, "master");

        Build build = job.startBuild(parameters).shouldSucceed();
        assertThatActionExists(job, build, "LLVM/Clang Warnings");

        build.open();
        int count = 10;
        assertThat(driver, hasContent("LLVM/Clang Warnings: " + count));

        assertThat(build.getConsole(), containsString("[WARNINGS] Parsing warnings in files '**/compile-log.txt' with parser Clang (LLVM based)"));
        assertThat(build.getConsole(), containsRegexp("\\[WARNINGS\\] Parsing warnings in files '[\\d]+/nothing'"));
        assertThat(build.getConsole(), containsRegexp("\\[WARNINGS\\] Parsing warnings in files '[\\d]+_master\\\\ND4\\\\ReleaseTools\\\\Build\\\\Log\\\\warning.log'"));
    }

    /**
     * Creates a multi-branch freestyle job (without any branch options set). Configures three warnings parsers.
     * Currently no build is started yet.
     */
    @Test @Issue("JENKINS-33582") @WithPlugins("multi-branch-project-plugin")
    public void should_find_warnings_in_multi_branch_project() {
        setupJob(SEVERAL_PARSERS_FILE_FULL_PATH, FreeStyleMultiBranchJob.class,
                WarningsBuildSettings.class, new AnalysisConfigurator<WarningsBuildSettings>() {
                    @Override
                    public void configure(WarningsBuildSettings settings) {
                        settings.addWorkspaceScanner(JAVA_COMPILER, "**/*");
                        settings.addWorkspaceScanner(MS_BUILD, "**/*");
                        settings.addWorkspaceScanner(JAVA_DOC, "**/*");
                    }
                });

        // TODO: run a build and verify the results
    }

    /**
     * Verifies that Jenkins scans the workspace for all available files to resolve relative paths
     * of files with warnings when the option 'resolve-relative-paths' is enabled.
     */
    @Test @Issue("JENKINS-32150") @WithPlugins("analysis-core@1.76")
    public void should_resolve_workspace_files() {
        FreeStyleJob job = createFreeStyleJob(RESOURCES + "jenkins-32150", new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceScanner(CLANG, "**/compile-log.txt");
                settings.setCanResolveRelativePaths(true);
            }
        });

        Build build = buildSuccessfulJob(job);

        assertThatActionExists(job, build, "LLVM/Clang Warnings");

        build.open();
        int count = 10;
        assertThat(driver, hasContent("LLVM/Clang Warnings: " + count));

        WarningsAction action = new WarningsAction(build);

        assertThatWarningsCountInSummaryIs(action, count);
        assertThatNewWarningsCountInSummaryIs(action, count);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(count));
        assertThat(action.getNumberOfNewWarnings(), is(count));
        assertThat(action.getNumberOfFixedWarnings(), is(0));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(0));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(count));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(0));

        assertThatFilesTabIsCorrectlyFilled(action);
        assertThatWarningsTabIsCorrectlyFilled(action);

        verifySourceLine(action, "file-in-subdir.txt", 2,
                "02 EXAMPLE IN SUBDIR",
                "Some other warning");
        verifySourceLine(action, "file.txt", 6,
                "06 EXAMPLE",
                "Some warning SECOND");
        // Since multi-file-in-subdir.txt is contained twice in the workspace no source code is resolved
        verifySourceLine(action, "multi-file-in-subdir.txt", 3,
                "03 Is the file 'multi-file-in-subdir.txt' contained more than once in your workspace?",
                "Another warning");
    }

    private void assertThatFilesTabIsCorrectlyFilled(WarningsAction ca) {
        SortedMap<String, Integer> expectedFileDetails = new TreeMap<>();
        expectedFileDetails.put("file-in-subdir.txt", 4);
        expectedFileDetails.put("file.txt", 2);
        expectedFileDetails.put("multi-file-in-subdir.txt", 2);
        expectedFileDetails.put("multi-file-in-subdir.txt", 2); // FIXME: list and not map
        assertThat(ca.getFileTabContents(), is(expectedFileDetails));
    }

    private void assertThatWarningsTabIsCorrectlyFilled(WarningsAction ca) {
        SortedMap<String, String> expectedWarnings = new TreeMap<>();
        expectedWarnings.put("multi-file-in-subdir.txt:5", "..");
        expectedWarnings.put("multi-file-in-subdir.txt:10", "..");
        expectedWarnings.put("file-in-subdir.txt:2", "directory-a");
        expectedWarnings.put("file-in-subdir.txt:4", "directory-a");
        expectedWarnings.put("file-in-subdir.txt:7", "directory-a");
        expectedWarnings.put("file-in-subdir.txt:9", "directory-a");
        expectedWarnings.put("file.txt:1", "-");
        expectedWarnings.put("file.txt:6", "-");
        expectedWarnings.put("multi-file-in-subdir.txt:3", "-");
        expectedWarnings.put("multi-file-in-subdir.txt:8", "-");

        assertThat(ca.getWarningsTabContentsAsStrings(), is(expectedWarnings));
    }

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
        MatrixProject job = setupJob(RESOURCES + file, MatrixProject.class,
                WarningsBuildSettings.class, new AnalysisConfigurator<WarningsBuildSettings>() {
                    @Override
                    public void configure(WarningsBuildSettings settings) {
                        settings.addConsoleParser("GNU C Compiler 4 (gcc)");
                    }
                });

        job.configure();
        job.addUserAxis("user_axis", "one two three");
        job.addShellStep("cat " + file + "| grep $user_axis");
        job.save();

        Build build = buildSuccessfulJob(job);

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

        // sometimes the details is not refreshed yet (https://issues.jenkins-ci.org/browse/JENKINS-31431) 
        // so let's add an sleep and a refresh 

        sleep(1000);
        driver.navigate().refresh();
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

        FreeStyleJob job = createFreeStyleJob(new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceScanner(JAVA_COMPILER, "**/*");
                settings.addWorkspaceScanner(MS_BUILD, "**/*");
            }
        });
        buildSuccessfulJob(job);

        editJob(job, WarningsBuildSettings.class, new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceScanner(JAVA_DOC, "**/*");
                settings.setExcludePattern(".*Catalyst.*");
                settings.setBuildFailedTotalAll("0");
            }
        });
        configureEmailNotification(job, "Warnings: ${WARNINGS_RESULT}",
                "Warnings: ${WARNINGS_COUNT}-${WARNINGS_FIXED}-${WARNINGS_NEW}");

        buildFailingJob(job);

        verifyReceivedMail("Warnings: FAILURE", "Warnings: 142-12-8");
    }

    private FreeStyleJob createFreeStyleJob(final AnalysisConfigurator<WarningsBuildSettings> buildConfigurator) {
        return createFreeStyleJob(SEVERAL_PARSERS_FILE_FULL_PATH, buildConfigurator);
    }

    private FreeStyleJob createFreeStyleJob(final String resourceToCopy, final AnalysisConfigurator<WarningsBuildSettings> buildConfigurator) {
        return setupJob(resourceToCopy, FreeStyleJob.class, WarningsBuildSettings.class, buildConfigurator);
    }

    /**
     * Checks that no warnings are reported if the build does nothing.
     */
    @Test
    public void should_detect_no_errors_in_console_log_and_workspace_when_there_are_none() {
        FreeStyleJob job = createNoFilesFreeStyleJob(new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addConsoleParser("Maven");
                settings.addWorkspaceScanner(JAVA_COMPILER, "**/*");
            }
        });

        Build build = buildSuccessfulJob(job);

        assertThatActionIsMissing(job, build, "Java Warnings");
        assertThatActionIsMissing(job, build, "Maven Warnings");

        build.open();

        assertThat(driver, hasContent("Java Warnings: 0"));
        assertThat(driver, hasContent("Maven Warnings: 0"));
    }

    private FreeStyleJob createNoFilesFreeStyleJob(final AnalysisConfigurator<WarningsBuildSettings> configurator) {
        return setupJob(null, FreeStyleJob.class, WarningsBuildSettings.class, configurator);
    }

    /**
     * Checks that no warnings are reported if these are located in a different file.
     */
    @Test
    public void should_not_detect_errors_in_ignored_parts_of_the_workspace() {
        FreeStyleJob job = createNoFilesFreeStyleJob(new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceScanner("Maven", "no_errors_here.log");
            }
        });

        job.configure();
        job.addShellStep("mvn clean install > errors.log || true");
        job.save();

        Build build = buildSuccessfulJob(job);

        assertThatActionIsMissing(job, build, "Maven Warnings");

        build.open();
        assertThat(driver, hasContent("Maven Warnings: 0"));
    }

    /**
     * Sets up a list view with a warnings column. Builds a freestyle job and checks if the column shows the correct
     * number of warnings and provides a direct link to the actual warning results.
     */
    @Test
    public void should_set_warnings_count_in_list_view_column_for_freestyle_project() {
        FreeStyleJob job = createFreeStyleJobWith3Parsers();
        catWarningsToConsole(job);
        buildSuccessfulJob(job);

        ListView view = addListViewColumn(WarningsColumn.class);

        assertValidLink(job.name);
        view.delete();
    }

    /**
     * Sets up a list view with a warnings column. Builds a matrix job and checks if the column shows the correct number
     * of warnings and provides a direct link to the actual warning results.
     */
    @Test @Issue("23446")
    public void should_set_warnings_count_in_list_view_column_for_matrix_project() {
        MatrixProject job = createMatrixProject();
        catWarningsToConsole(job);
        buildJobAndWait(job).shouldSucceed();

        ListView view = addListViewColumn(WarningsColumn.class);

        assertValidLink(job.name);
        view.delete();
    }

    private MatrixProject createMatrixProject() {
        return setupJob(SEVERAL_PARSERS_FILE_FULL_PATH, MatrixProject.class,
                WarningsBuildSettings.class, create3ParserConfiguration());
    }

    private void assertValidLink(final String jobName) {
        By warningsLinkMatcher = by.css("a[href$='job/" + jobName + "/warnings']");

        assertThat(jenkins.all(warningsLinkMatcher).size(), is(1));
        WebElement link = jenkins.getElement(warningsLinkMatcher);
        assertThat(link.getText().trim(), is(String.valueOf(TOTAL)));

        link.click();
        assertThat(driver, hasContent("Aggregated Compiler Warnings"));
    }

    /**
     * Checks that warning results are correctly created for a matrix job with the parsers "Java", "JavaDoc" and
     * "MSBuild" if the console log contains multiple warnings of these types.
     */
    @Test
    public void should_detect_warnings_of_multiple_compilers_in_console_matrix() {
        MatrixProject job = createMatrixProject();

        catWarningsToConsole(job);

        job.configure();
        job.addUserAxis("user_axis", "axis1 axis2 axis3");
        job.save();

        verify3ParserResults(job, 3);
    }

    /**
     * Runs a job with warning threshold configured once and validates that build is marked as unstable.
     */
    @Test @Issue("19614")
    public void should_set_build_to_unstable_if_total_warnings_threshold_set() {
        FreeStyleJob job = createFreeStyleJob(new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addConsoleParser(JAVA_COMPILER);
                settings.addConsoleParser(JAVA_DOC);
                settings.addConsoleParser(MS_BUILD);
                settings.setBuildUnstableTotalAll("0");
                settings.setNewWarningsThresholdFailed("0");
                settings.setUseDeltaValues(true);
            }
        });

        catWarningsToConsole(job);
        buildUnstableJob(job);
    }

    /**
     * Checks that warning results are correctly created for a freestyle project with the parsers "Java", "JavaDoc" and
     * "MSBuild" if the console log contains multiple warnings of these types.
     */
    @Test
    public void should_detect_warnings_of_multiple_compilers_in_console_freestyle() {
        FreeStyleJob job = createFreeStyleJobWith3Parsers();

        catWarningsToConsole(job);

        verify3ParserResults(job, 1);
    }

    private FreeStyleJob createFreeStyleJobWith3Parsers() {
        return createFreeStyleJob(create3ParserConfiguration());
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
                settings.addConsoleParser(JAVA_COMPILER);
                settings.addConsoleParser(JAVA_DOC);
                settings.addConsoleParser(MS_BUILD);
            }
        };
    }

    /**
     * Checks that warning results are correctly created for the workspace parsers "Java", "JavaDoc" and "MSBuild" if a
     * file with multiple warnings of these types is copied to the workspace.
     */
    @Test
    public void should_detect_warnings_of_multiple_compilers_in_workspace() {
        FreeStyleJob job = createFreeStyleJob(new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceScanner(JAVA_COMPILER, "**/*");
                settings.addWorkspaceScanner(JAVA_DOC, "**/*");
                settings.addWorkspaceScanner(MS_BUILD, "**/*");
            }
        });

        verify3ParserResults(job, 1);
    }

    private void verify3ParserResults(final Job job, final int numberOfRuns) {
        Build build = buildSuccessfulJob(job);
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
    public void should_skip_failed_builds() {
        FreeStyleJob job = runBuildWithRunAlwaysOption(false);
        Build build = buildJobAndWait(job).shouldFail();

        assertThatActionIsMissing(job, build, "Java Warnings");

        build.open();
        assertThat(driver, not(hasContent("Java Warnings:")));
    }

    private void assertThatActionIsMissing(final FreeStyleJob job, final Build build, final String parser) {
        assertThat(job, not(hasAction(parser)));
        assertThat(build, not(hasAction(parser)));
        assertThat(job.getLastBuild(), not(hasAction("Java Warnings")));
    }

    /**
     * Checks that the warnings plugin will not skip build results if "Run always" is checked.
     */
    @Test
    public void should_not_skip_failed_builds_with_option_run_always() {
        FreeStyleJob job = runBuildWithRunAlwaysOption(true);
        Build build = buildJobAndWait(job).shouldFail();

        assertThatActionExists(job, build, "Java Warnings");

        WarningsAction action = new WarningsAction(build);

        assertThatWarningsCountInSummaryIs(action, 131);
        assertThatNewWarningsCountInSummaryIs(action, 131);
    }

    private FreeStyleJob runBuildWithRunAlwaysOption(final boolean canRunOnFailed) {
        FreeStyleJob job = createFreeStyleJob(new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceScanner(JAVA_COMPILER, "**/*");
                settings.setCanRunOnFailed(canRunOnFailed);
            }
        });

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
    public void should_detect_errors_in_console_log() {
        FreeStyleJob job = createNoFilesFreeStyleJob(new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addConsoleParser("Maven");
            }
        });

        job.configure();
        job.addShellStep("mvn clean install || true");
        job.save();

        Build build = buildSuccessfulJob(job);
        assertThatActionExists(job, build, "Maven Warnings");

        WarningsAction action = new WarningsAction(build);

        assertThatWarningsCountInSummaryIs(action, 1);
        assertThatNewWarningsCountInSummaryIs(action, 1);

        action.open();

        assertThat(action.getNumberOfNewWarnings(), is(1));
        assertThat(action.getNumberOfWarnings(), is(1));
        assertThat(action.getNumberOfFixedWarnings(), is(0));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(1));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(0));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(0));
    }

    private void assertThatActionExists(final Job job, final Build build, final String parser) {
        assertThat(job.getLastBuild(), hasAction(parser));
        assertThat(job, hasAction(parser));
        assertThat(build, hasAction(parser));
    }

    /**
     * Checks whether the warnings plugin picks only specific warnings. The warnings to exclude are given by three
     * exclude patterns {".*ignore1.*, .*ignore2.*, .*default.*"}. The result should be a build with 4 Java Warnings
     * (from a file that contains 9 warnings).
     */
    @Test
    public void should_skip_warnings_in_ignored_parts() {
        FreeStyleJob job = createFreeStyleJob(WARNINGS_FILE_FOR_INCLUDE_EXCLUDE_TESTS, new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceScanner(JAVA_COMPILER, "**/*");
                settings.setIncludePattern(".*/.*");
                settings.setExcludePattern(".*/ignore1/.*, .*/ignore2/.*, .*/default/.*");
            }
        });

        Build build = buildSuccessfulJob(job);
        assertThatActionExists(job, build, "Java Warnings");

        WarningsAction action = new WarningsAction(build);

        assertThatWarningsCountInSummaryIs(action, 4);
        assertThatNewWarningsCountInSummaryIs(action, 4);
    }

    /**
     * Checks whether the warnings plugin picks only specific warnings. The warnings to include are given by two include
     * patterns {".*include.*", ".*default.*"}. The result should be a build with 5 Java Warnings (from a file that
     * contains 9 warnings).
     */
    @Test
    public void should_include_warnings_specified_in_included_parts() {
        FreeStyleJob job = createFreeStyleJob(WARNINGS_FILE_FOR_INCLUDE_EXCLUDE_TESTS, new AnalysisConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceScanner(JAVA_COMPILER, "**/*");
                settings.setIncludePattern(".*/include*/.*, .*/default/.*");
            }
        });
        Build build = buildSuccessfulJob(job);
        assertThatActionExists(job, build, "Java Warnings");

        WarningsAction action = new WarningsAction(build);

        assertThatWarningsCountInSummaryIs(action, 5);
        assertThatNewWarningsCountInSummaryIs(action, 5);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(5));
        assertThat(action.getNumberOfNewWarnings(), is(5));
        assertThat(action.getNumberOfFixedWarnings(), is(0));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(0));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(5));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(0));
    }
}