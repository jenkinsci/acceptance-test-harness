package plugins;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AbstractCodeStylePluginBuildConfigurator;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsAction;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsBuildSettings;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Tests various aspects of the warnings plug-in. Most tests copy an existing file with several warnings into the
 * workspace. This file is then analyzed by console and workspace parsers.
 */
@WithPlugins("warnings")
public class WarningsPluginTest extends AbstractCodeStylePluginHelper {
    /** Contains warnings for Javac parser. Warnings have file names preset for include/exclude filter tests. */
    private static final String WARNINGS_FILE_FOR_INCLUDE_EXCLUDE_TESTS = "/warnings_plugin/warningsForRegEx.txt";
    private static final String SEVERAL_PARSERS_FILE_NAME = "warningsAll.txt";
    /** Contains warnings for several parsers. */
    private static final String SEVERAL_PARSERS_FILE_FULL_PATH = "/warnings_plugin/" + SEVERAL_PARSERS_FILE_NAME;

    /**
     * Checks that no warnings are reported if the build does nothing.
     */
    @Test
    public void detect_no_errors_in_console_log_and_workspace_when_there_are_none() {
        AbstractCodeStylePluginBuildConfigurator<WarningsBuildSettings> buildConfigurator = new AbstractCodeStylePluginBuildConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addConsoleScanner("Maven");
                settings.addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
            }
        };
        FreeStyleJob job = setupJob(null, FreeStyleJob.class, WarningsBuildSettings.class, buildConfigurator);

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
    public void do_not_detect_errors_in_ignored_parts_of_the_workspace() {
        AbstractCodeStylePluginBuildConfigurator<WarningsBuildSettings> buildConfigurator = new AbstractCodeStylePluginBuildConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceFileScanner("Maven", "no_errors_here.log");
            }
        };
        FreeStyleJob job = setupJob(null, FreeStyleJob.class, WarningsBuildSettings.class, buildConfigurator);

        job.configure();
        job.addShellStep("mvn clean install > errors.log || true");
        job.save();

        Build build = buildJobWithSuccess(job);

        assertThatActionIsMissing(job, build, "Maven Warnings");

        build.open();
        assertThat(driver, hasContent("Maven Warnings: 0"));
    }

    /**
     * Checks that warning results are correctly created for the parsers
     * "Java", "JavaDoc" and "MSBuild" if the console log contains multiple warnings of these types.
     */
    @Test
    public void detect_warnings_of_multiple_compilers_in_console() {
        AbstractCodeStylePluginBuildConfigurator<WarningsBuildSettings> buildConfigurator = new AbstractCodeStylePluginBuildConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addConsoleScanner("Java Compiler (javac)");
                settings.addConsoleScanner("JavaDoc Tool");
                settings.addConsoleScanner("MSBuild");
            }
        };
        FreeStyleJob job = setupJob(SEVERAL_PARSERS_FILE_FULL_PATH, FreeStyleJob.class,
                WarningsBuildSettings.class, buildConfigurator);

        job.configure();
        job.addShellStep("cat " + SEVERAL_PARSERS_FILE_NAME);
        job.save();

        verify3ParserResults(job);
    }

    /**
     * Checks that warning results are correctly created for the workspace parsers
     * "Java", "JavaDoc" and "MSBuild" if a file with multiple warnings of these types is copied to the workspace.
     */
    @Test
    public void detect_warnings_of_multiple_compilers_in_workspace() {
        AbstractCodeStylePluginBuildConfigurator<WarningsBuildSettings> buildConfigurator = new AbstractCodeStylePluginBuildConfigurator<WarningsBuildSettings>() {
            @Override
            public void configure(WarningsBuildSettings settings) {
                settings.addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
                settings.addWorkspaceFileScanner("JavaDoc Tool", "**/*");
                settings.addWorkspaceFileScanner("MSBuild", "**/*");
            }
        };
        FreeStyleJob job = setupJob(SEVERAL_PARSERS_FILE_FULL_PATH, FreeStyleJob.class,
                WarningsBuildSettings.class, buildConfigurator);

        verify3ParserResults(job);
    }

    private void verify3ParserResults(final FreeStyleJob job) {
        Build build = buildJobWithSuccess(job);
        assertThatActionExists(job, build, "Java Warnings");
        assertThatActionExists(job, build, "JavaDoc Warnings");
        assertThatActionExists(job, build, "MSBuild Warnings");

        build.open();
        assertThat(driver, hasContent("Java Warnings: 131"));
        assertThat(driver, hasContent("JavaDoc Warnings: 8"));
        assertThat(driver, hasContent("MSBuild Warnings: 15"));
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
        AbstractCodeStylePluginBuildConfigurator<WarningsBuildSettings> buildConfigurator = new AbstractCodeStylePluginBuildConfigurator<WarningsBuildSettings>() {
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
        AbstractCodeStylePluginBuildConfigurator<WarningsBuildSettings> buildConfigurator = new AbstractCodeStylePluginBuildConfigurator<WarningsBuildSettings>() {
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
     * Checks whether the warnings plugin picks only specific warnings. The warnings to exclude are given by three exclude
     * patterns {".*ignore1.*, .*ignore2.*, .*default.*"}. The result should be a build with 4 Java Warnings (from a file that
     * contains 9 warnings).
     */
    @Test
    public void skip_warnings_in_ignored_parts() {
        AbstractCodeStylePluginBuildConfigurator<WarningsBuildSettings> buildConfigurator = new AbstractCodeStylePluginBuildConfigurator<WarningsBuildSettings>() {
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
        AbstractCodeStylePluginBuildConfigurator<WarningsBuildSettings> buildConfigurator = new AbstractCodeStylePluginBuildConfigurator<WarningsBuildSettings>() {
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