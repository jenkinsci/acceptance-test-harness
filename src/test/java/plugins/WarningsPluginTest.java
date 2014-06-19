package plugins;

import org.hamcrest.Matcher;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsAction;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasAction;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

/**
 * Feature: Adds Warnings collection support
 * In order to be able to collect and analyze warnings
 * As a Jenkins user
 * I want to install and configure Warnings plugin
 */
@WithPlugins("warnings")
public class WarningsPluginTest extends AbstractCodeStylePluginHelper {

    private FreeStyleJob job;

    @Before
    public void setUp() {
        job = jenkins.jobs.create();
    }

    /**
     * Scenario: Detect no errors in console log and workspace when there are none
     * Given I have installed the "warnings" plugin
     * And a job
     * When I configure the job
     * And I add "Scan for compiler warnings" post-build action
     * And I add console parser for "Maven"
     * And I add workspace parser for "Java Compiler (javac)" applied at "** / *"
     * And I save the job
     * And I build the job
     * Then build should have 0 "Java" warnings
     * Then build should have 0 "Maven" warnings
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
     * Scenario: Detect errors in console log
     * Given I have installed the "warnings" plugin
     * And a job
     * When I configure the job
     * And I add "Scan for compiler warnings" post-build action
     * And I add console parser for "Maven"
     * And I add a shell build step "mvn clean install || true"
     * And I save the job
     * And I build the job
     * Then build should have 1 "Maven" warning
     */
    @Test
    public void detect_errors_in_console_log() {
        job.configure();
        job.addPublisher(WarningsPublisher.class)
                .addConsoleScanner("Maven");
        job.addShellStep("mvn clean install || true");
        job.save();

        Build b = buildJobWithSuccess(job);

        assertThat(b, hasAction("Maven Warnings"));
        WarningsAction wa = new WarningsAction(job);
        assertThat(wa.getResultLinkByXPathText("1 warning"), is("warnings29Result"));
        assertThat(wa.getResultLinkByXPathText("1 new warning"), is("warnings29Result/new"));
        assertThat(driver, hasContent("Maven Warnings: 1"));
        assertThat(wa.getNewWarningNumber(), is(1));
        assertThat(wa.getWarningNumber(), is(1));
        assertThat(wa.getFixedWarningNumber(), is(0));
        assertThat(wa.getHighWarningNumber(), is(1));
        assertThat(wa.getNormalWarningNumber(), is(0));
        assertThat(wa.getLowWarningNumber(), is(0));
    }

    /**
     * Scenario: Detect errors in workspace
     * Given I have installed the "warnings" plugin
     * And a job
     * When I configure the job
     * And I add "Scan for compiler warnings" post-build action
     * And I add workspace parser for "Java Compiler (javac)" applied at "** /*"
     * And I add a shell build step
     * """
     * echo '@Deprecated class a {} class b extends a {}' > a.java
     * javac -Xlint a.java 2> out.log || true
     * """
     * And I save the job
     * And I build the job
     * Then build should have 1 "Java" warning
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
     * Scenario: Do not detect errors in ignored parts of the workspace
     * Given I have installed the "warnings" plugin
     * And a job
     * When I configure the job
     * And I add "Scan for compiler warnings" post-build action
     * And I add workspace parser for "Maven" applied at "no_errors_here.log"
     * And I add a shell build step "mvn clean install > errors.log || true"
     * And I save the job
     * And I build the job
     * Then build should have 0 "Maven" warning
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
     * Scenario: Detect multiple log results at once in console log
     * Given I have installed the "warnings" plugin
     * And a job
     * When I configure the job
     * And I add "Scan for compiler warnings" post-build action
     * And I add console parser for "Java", "JavaDoc" and "MSBuild"
     * And I add a shell build step "cat /warnings_plugin/warningsALL.txt"
     * And I save the job
     * And I build the job
     * Then build should have 131 Java Warnings, 8 JavaDoc Warnings and 15 MSBuild warnings
     */
    @Test
    public void detect_warnings_of_multiple_compilers_in_console(){
        job.configure();
        WarningsPublisher wp = job.addPublisher(WarningsPublisher.class);
        wp.addConsoleScanner("Java Compiler (javac)");
        wp.addConsoleScanner("JavaDoc Tool");
        wp.addConsoleScanner("MSBuild");
        String warningsPath = this.getClass().getResource("/warnings_plugin/warningsAll.txt").getPath();
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
     Scenario: Detect multiple log results at once in workspace
     Given I have installed the "warnings" plugin
     And a job
     When I configure the job
     And I add "Scan for compiler warnings" post-build action
     And I add workspace parser for "Java", "JavaDoc" and "MSBuild"
     And I add a shell build step "cat /warnings_plugin/warningsALL.txt >> errors.log"
     And I save the job
     And I build the job
     Then build should have 131 Java Warnings, 8 JavaDoc Warnings and 15 MSBuild Warnings
     */
    @Test
    public void detect_warnings_of_multiple_compilers_in_workspace(){
        job.configure();
        WarningsPublisher wp = job.addPublisher(WarningsPublisher.class);
        wp.addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
        wp.addWorkspaceFileScanner("JavaDoc Tool", "**/*");
        wp.addWorkspaceFileScanner("MSBuild", "**/*");
        String warningsPath = this.getClass().getResource("/warnings_plugin/warningsAll.txt").getPath();
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
     Scenario: Warnings pluign skips failed builds by default
     Given I have installed the "warnings" plugin
     And a job that shall fail
     When I configure the job
     And I add "Scan for compiler warnings" post-build action
     And I add console parser for "Java"
     And I add a shell build step "exit 1"
     And I save the job
     And I build the job
     Then build should fail and warnings plugin shall skip build
     */
    @Test
    public void skip_failed_builds(){
        job.configure();
        WarningsPublisher wp = job.addPublisher(WarningsPublisher.class);
        wp.addConsoleScanner("Java Compiler (javac");
        job.addShellStep("exit 1");
        job.save();
        Build b = job.startBuild().shouldFail();
        assertThat(b, not(hasAction("Java Warnings")));
        b.open();
        assertThat(driver, not(hasContent("Java Warnings:")));
    }

    /**
     Scenario: Warnings plugin shall not skip build results if "Run always" is checked
     Given I have installed the "warnings" plugin
     And a job that shall fail
     When I configure the job
     And I add "Scan for compiler warnings" post-build action
     And I add workspace parser for "Java"
     And I add a shell build step "cat /warnings_plugin/warningsALL.txt >> errors.log"
     And I add a shell build step "exit 1"
     And I configure the Advanced option "Run always"
     And I save the job
     And I build the job
     Then build should fail and should have 131 Java Warnings
     */
    @Test
    public void do_not_skip_failed_builds_with_option_run_always(){
        job.configure();
        WarningsPublisher wp = job.addPublisher(WarningsPublisher.class);
        wp.addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
        wp.openAdvancedOptions();
        wp.runAlways();
        String warningsPath = this.getClass().getResource("/warnings_plugin/warningsALL.txt").getPath();
        job.addShellStep("cat " + warningsPath + " >> errors.log");
        job.addShellStep("exit 1");
        job.save();
        Build b = job.startBuild().shouldFail();
        assertThat(b, hasAction("Java Warnings"));
        b.open();
        assertThat(driver, hasContent("Java Warnings: 131"));
    }

    /**
     Scenario: Warnings plugin shall ignore specified parts
     Given I have installed the "warnings" plugin
     And a job
     When I configure the job
     And I add "Scan for compiler warnings" post-build action
     And I add console parser for "Java"
     And I add a shell build step "cat /warnings_plugin/warningsForRegEx.txt"
     And I add Warnings to include ".*\/.*"
     And I add Warnings to ignore ".*\/ignore1/.*, .*\/ignore2/.*, .*\/default/.*"
     And I save the job
     And I build the job
     Then build should have 5 Java Warnings
     */
    @Test
    public void skip_warnings_in_ignored_parts(){
        job.configure();
        WarningsPublisher wp = job.addPublisher(WarningsPublisher.class);
        wp.addConsoleScanner("Java Compiler (javac)");
        wp.openAdvancedOptions();
        wp.addWarningsToInclude(".*/.*");
        wp.addWarningsToIgnore(".*/ignore1/.*, .*/ignore2/.*, .*/default/.*");
        String warningsPath = this.getClass().getResource("/warnings_plugin/warningsForRegEx.txt").getPath();
        job.addShellStep("cat " + warningsPath);
        job.save();
        Build b = buildJobWithSuccess(job);
        assertThat(b, hasAction("Java Warnings"));
        b.open();
        assertThat(driver, hasContent("Java Warnings: 4"));
    }

    /**
     Scenario: Warnings plugin shall only include specified parts
     Given I have installed the "warnings" plugin
     And a job
     When I configure the job
     And I add "Scan for compiler warnings" post-build action
     And I add workspace parser for "Java"
     And I add Warnings to include .*\/include*\/.*, .*\/default/.*"
     And I save the job
     And I build the job
     Then build should have 5 Java Warnings
     */
    @Test
    public void include_warnings_specified_in_included_parts(){
        job.configure();
        WarningsPublisher wp = job.addPublisher(WarningsPublisher.class);
        wp.addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
        wp.openAdvancedOptions();
        wp.addWarningsToInclude(".*/include*/.*, .*/default/.*");
        String warningsPath = this.getClass().getResource("/warnings_plugin/warningsForRegEx.txt").getPath();
        job.addShellStep("cat " + warningsPath + " >> errors.log");
        job.save();
        Build b = buildJobWithSuccess(job);
        assertThat(b, hasAction("Java Warnings"));
        b.open();
        assertThat(driver, hasContent("Java Warnings: 5"));
    }
}