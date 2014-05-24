package plugins;

import org.hamcrest.Matcher;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 Feature: Adds Warnings collection support
   In order to be able to collect and analyze warnings
   As a Jenkins user
   I want to install and configure Warnings plugin
 */
// TODO: should derive from AbstractCodeStylePluginHelper
@WithPlugins("warnings")
public class WarningsPluginTest extends AbstractJUnitTest {

    private FreeStyleJob job;

    @Before
    public void setUp() {
        job = jenkins.jobs.create();
    }

    /**
     Scenario: Detect no errors in console log and workspace when there are none
       Given I have installed the "warnings" plugin
       And a job
       When I configure the job
       And I add "Scan for compiler warnings" post-build action
       And I add console parser for "Maven"
       And I add workspace parser for "Java Compiler (javac)" applied at "** / *"
       And I save the job
       And I build the job
       Then build should have 0 "Java" warnings
       Then build should have 0 "Maven" warnings
     */
    @Test
    public void detect_no_errors_in_console_log_and_workspace_when_there_are_none() {
        job.configure();
        WarningsPublisher pub = job.addPublisher(WarningsPublisher.class);
        pub.addConsoleScanner("Maven");
        pub.addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
        job.save();

        Build b = job.startBuild().shouldSucceed();

        assertThatBuildHasWarnings(b, 0,"Java");
        assertThatBuildHasWarnings(b, 0, "Maven");
    }

    /**
     Scenario: Detect errors in console log
       Given I have installed the "warnings" plugin
       And a job
       When I configure the job
       And I add "Scan for compiler warnings" post-build action
       And I add console parser for "Maven"
       And I add a shell build step "mvn clean install || true"
       And I save the job
       And I build the job
       Then build should have 1 "Maven" warning
     */
    @Test
    public void detect_errors_in_console_log() {
        job.configure();
        job.addPublisher(WarningsPublisher.class)
                .addConsoleScanner("Maven");
        job.addShellStep("mvn clean install || true");
        job.save();

        Build b = job.startBuild().shouldSucceed();

        assertThatBuildHasWarnings(b,1,"Maven");
    }

    /**
     Scenario: Detect errors in workspace
       Given I have installed the "warnings" plugin
       And a job
       When I configure the job
       And I add "Scan for compiler warnings" post-build action
       And I add workspace parser for "Java Compiler (javac)" applied at "** /*"
       And I add a shell build step
           """
               echo '@Deprecated class a {} class b extends a {}' > a.java
               javac -Xlint a.java 2> out.log || true
           """
       And I save the job
       And I build the job
       Then build should have 1 "Java" warning
     */
    @Test
    public void detect_errors_in_workspace() {
        job.configure();
        job.addPublisher(WarningsPublisher.class)
                .addWorkspaceFileScanner("Java Compiler (javac)", "**/*");
        job.addShellStep(
                "echo '@Deprecated class a {} class b extends a {}' > a.java\n"+
                "javac -Xlint a.java 2> out.log || true");
        job.save();

        Build b = job.startBuild().shouldSucceed();

        assertThatBuildHasWarnings(b,1,"Java");
    }

    /**
     Scenario: Do not detect errors in ignored parts of the workspace
       Given I have installed the "warnings" plugin
       And a job
       When I configure the job
       And I add "Scan for compiler warnings" post-build action
       And I add workspace parser for "Maven" applied at "no_errors_here.log"
       And I add a shell build step "mvn clean install > errors.log || true"
       And I save the job
       And I build the job
       Then build should have 0 "Maven" warning
     */
    @Test
    public void do_not_detect_errors_in_ignored_parts_of_the_workspace() {
        job.configure();
        job.addPublisher(WarningsPublisher.class)
                .addWorkspaceFileScanner("Maven", "no_errors_here.log");
        job.addShellStep("mvn clean install > errors.log || true");
        job.save();

        Build b = job.startBuild().shouldSucceed();

        assertThatBuildHasWarnings(b,0,"Maven");
    }

    private void assertThatBuildHasWarnings(Build b, int i, String kind) {

//        job.open();
//        find(by.xpath("div[@class='test-trend-caption'][text()='%s Warnings Trend']",kind));

        Matcher<PageObject> m = hasAction(String.format("%s Warnings", kind));
        if (i==0)   m = not(m);

        assertThat(job, m);
        assertThat(b, m);
        b.open();
        assertThat(driver, hasContent(String.format("%s Warnings: %d",kind,i)));
    }
}
