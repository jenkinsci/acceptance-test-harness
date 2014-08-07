package plugins;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AbstractCodeStylePluginBuildConfigurator;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsAction;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsBuildSettings;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Feature: Adds Warnings collection support In order to be able to collect and analyze warnings As a Jenkins user I
 * want to install and configure Warnings plugin
 */
@WithPlugins("warnings")
public class WarningsPluginTest extends AbstractCodeStylePluginHelper {
    /** Contains warnings for Javac parser. Warnings have file names preset for include/exclude filter tests. */
    private static final String WARNINGS_FILE_FOR_INCLUDE_EXCLUDE_TESTS = "/warnings_plugin/warningsForRegEx.txt";
    /** Contains warnings for several parsers. */
    private static final String WARNINGS_FILE_SEVERAL_PARSERS = "/warnings_plugin/warningsAll.txt";

    private FreeStyleJob job;

    @Before
    public void setUp() {
        job = jenkins.jobs.create();
    }

    /**
     * Scenario: Detect no errors in console log and workspace when there are none Given I have installed the "warnings"
     * plugin And a job When I configure the job And I add "Scan for compiler warnings" post-build action And I add
     * console parser for "Maven" And I add workspace parser for "Java Compiler (javac)" applied at "** / *" And I save
     * the job And I build the job Then build should have 0 "Java" warnings Then build should have 0 "Maven" warnings
     */
    @Test
    public void detect_no_errors_in_console_log_and_workspace_when_there_are_none() {
        job.configure();
        WarningsPublisher pub = job.addPublisher(WarningsPublisher.class);
        pub.addConsoleScanner("Maven");
        pub.addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
        job.save();

        Build b = buildJobWithSuccess(job);

        assertThat(b, not(hasAction("Java Warnings")));
        assertThat(b, not(hasAction("Maven Warnings")));
        b.open();
        assertThat(driver, hasContent("Java Warnings: 0"));
        assertThat(driver, hasContent("Maven Warnings: 0"));
    }

    /**
     * Scenario: Detect errors in workspace Given I have installed the "warnings" plugin And a job When I configure the
     * job And I add "Scan for compiler warnings" post-build action And I add workspace parser for "Java Compiler
     * (javac)" applied at "** /*" And I add a shell build step """ echo '@Deprecated class a {} class b extends a {}' >
     * a.java javac -Xlint a.java 2> out.log || true """ And I save the job And I build the job Then build should have 1
     * "Java" warning
     */
    @Test
    public void detect_errors_in_workspace() {
        job.configure();
        job.addPublisher(WarningsPublisher.class)
                .addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
        job.addShellStep(
                "echo '@Deprecated class a {} class b extends a {}' > a.java\n" +
                        "javac -Xlint a.java 2> out.log || true"
        );
        job.save();

        Build b = buildJobWithSuccess(job);

        assertThat(b, hasAction("Java Warnings"));
        b.open();
        assertThat(driver, hasContent("Java Warnings: 1"));
    }

    /**
     * Scenario: Do not detect errors in ignored parts of the workspace Given I have installed the "warnings" plugin And
     * a job When I configure the job And I add "Scan for compiler warnings" post-build action And I add workspace
     * parser for "Maven" applied at "no_errors_here.log" And I add a shell build step "mvn clean install > errors.log
     * || true" And I save the job And I build the job Then build should have 0 "Maven" warning
     */
    @Test
    public void do_not_detect_errors_in_ignored_parts_of_the_workspace() {
        job.configure();
        job.addPublisher(WarningsPublisher.class)
                .addWorkspaceFileScanner("Maven", "no_errors_here.log");
        job.addShellStep("mvn clean install > errors.log || true");
        job.save();

        Build b = buildJobWithSuccess(job);

        assertThat(b, not(hasAction("Maven Warnings")));
        b.open();
        assertThat(driver, hasContent("Maven Warnings: 0"));
    }

    /**
     * Scenario: Detect multiple log results at once in console log Given I have installed the "warnings" plugin And a
     * job When I configure the job And I add "Scan for compiler warnings" post-build action And I add console parser
     * for "Java", "JavaDoc" and "MSBuild" And I add a shell build step "cat /warnings_plugin/warningsALL.txt" And I
     * save the job And I build the job Then build should have 131 Java Warnings, 8 JavaDoc Warnings and 15 MSBuild
     * warnings
     */
    @Test
    public void detect_warnings_of_multiple_compilers_in_console() {
        job.configure();
        WarningsPublisher wp = job.addPublisher(WarningsPublisher.class);
        wp.addConsoleScanner("Java Compiler (javac)");
        wp.addConsoleScanner("JavaDoc Tool");
        wp.addConsoleScanner("MSBuild");
        String warningsPath = this.getClass().getResource(WARNINGS_FILE_SEVERAL_PARSERS).getPath();
        job.addShellStep("cat " + warningsPath);
        job.save();
        Build b = buildJobWithSuccess(job);
        assertThat(b, hasAction("Java Warnings"));
        assertThat(b, hasAction("JavaDoc Warnings"));
        assertThat(b, hasAction("MSBuild Warnings"));
        b.open();
        assertThat(driver, hasContent("Java Warnings: 131"));
        assertThat(driver, hasContent("JavaDoc Warnings: 8"));
        assertThat(driver, hasContent("MSBuild Warnings: 15"));
    }

    /**
     * Scenario: Detect multiple log results at once in workspace Given I have installed the "warnings" plugin And a job
     * When I configure the job And I add "Scan for compiler warnings" post-build action And I add workspace parser for
     * "Java", "JavaDoc" and "MSBuild" And I add a shell build step "cat /warnings_plugin/warningsALL.txt >> errors.log"
     * And I save the job And I build the job Then build should have 131 Java Warnings, 8 JavaDoc Warnings and 15
     * MSBuild Warnings
     */
    @Test
    public void detect_warnings_of_multiple_compilers_in_workspace() {
        job.configure();
        WarningsPublisher wp = job.addPublisher(WarningsPublisher.class);
        wp.addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
        wp.addWorkspaceFileScanner("JavaDoc Tool", "**/*");
        wp.addWorkspaceFileScanner("MSBuild", "**/*");
        String warningsPath = this.getClass().getResource(WARNINGS_FILE_SEVERAL_PARSERS).getPath();
        job.addShellStep("cat " + warningsPath + " >> errors.log");
        job.save();
        Build b = buildJobWithSuccess(job);
        assertThat(b, hasAction("Java Warnings"));
        assertThat(b, hasAction("JavaDoc Warnings"));
        assertThat(b, hasAction("MSBuild Warnings"));
        b.open();
        assertThat(driver, hasContent("Java Warnings: 131"));
        assertThat(driver, hasContent("JavaDoc Warnings: 8"));
        assertThat(driver, hasContent("MSBuild Warnings: 15"));
    }

    /**
     * Checks that the warnings plugin will not skip build results if "Run always" is checked.
     */
    @Test
    public void skip_failed_builds() {
        FreeStyleJob job = runBuildWithRunAlwaysOption(false);
        Build build = buildJobAndWait(job).shouldFail();

        assertThat(build, not(hasAction("Java Warnings")));
        assertThat(job.getLastBuild(), not(hasAction("Java Warnings")));

        build.open();
        assertThat(driver, not(hasContent("Java Warnings:")));
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
        FreeStyleJob job = setupJob(WARNINGS_FILE_SEVERAL_PARSERS, FreeStyleJob.class,
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

    private void assertThatActionExists(final Job job, final Build build, final String type) {
        assertThat(build, hasAction(type));
        assertThat(job.getLastBuild(), hasAction(type));
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