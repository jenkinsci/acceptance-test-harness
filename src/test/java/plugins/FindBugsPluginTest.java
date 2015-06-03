/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package plugins;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import org.jenkinsci.test.acceptance.po.Job;
import org.jvnet.hudson.test.Issue;
import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisConfigurator;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindBugsAction;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindBugsColumn;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindBugsFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindBugsMavenSettings;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindBugsPortlet;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.Node;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xml.sax.SAXException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Acceptance tests for the FindBugs plugin.
 *
 * @author Martin Kurz
 * @author Fabian Trampusch
 * @author Ullrich Hafner
 */
@WithPlugins("findbugs")
public class FindBugsPluginTest extends AbstractAnalysisTest<FindBugsAction> {
    private static final String PATTERN_WITH_6_WARNINGS = "findbugsXml.xml";
    private static final String FILE_WITH_6_WARNINGS = "/findbugs_plugin/" + PATTERN_WITH_6_WARNINGS;
    private static final String PLUGIN_ROOT = "/findbugs_plugin/";

    @Override
    protected FindBugsAction createProjectAction(final FreeStyleJob job) {
        return new FindBugsAction(job);
    }

    /**
     * Checks that the plug-in sends a mail after a build has been failed. The content of the mail
     * contains several tokens that should be expanded in the mail with the correct values.
     */
    @Test @Issue("JENKINS-25501") @WithPlugins("email-ext")
    public void should_send_mail_with_expanded_tokens() {
        setUpMailer();

        FreeStyleJob job = createFreeStyleJob(new AnalysisConfigurator<FindBugsFreestyleSettings>() {
            @Override
            public void configure(FindBugsFreestyleSettings settings) {
                settings.setBuildFailedTotalAll("0");
                settings.pattern.set(PATTERN_WITH_6_WARNINGS);
            }
        });

        configureEmailNotification(job, "FindBugs: ${FINDBUGS_RESULT}",
                "FindBugs: ${FINDBUGS_COUNT}-${FINDBUGS_FIXED}-${FINDBUGS_NEW}");

        buildFailingJob(job);

        verifyReceivedMail("FindBugs: FAILURE", "FindBugs: 6-0-6");
    }

    @Override
    protected FreeStyleJob createFreeStyleJob() {
        return createFreeStyleJob(new AnalysisConfigurator<FindBugsFreestyleSettings>() {
            @Override
            public void configure(FindBugsFreestyleSettings settings) {
                settings.pattern.set(PATTERN_WITH_6_WARNINGS);
            }
        });
    }

    private FreeStyleJob createFreeStyleJob(final AnalysisConfigurator<FindBugsFreestyleSettings> buildConfigurator) {
        return createFreeStyleJob(FILE_WITH_6_WARNINGS, buildConfigurator);
    }

    private FreeStyleJob createFreeStyleJob(final String file, final AnalysisConfigurator<FindBugsFreestyleSettings> buildConfigurator) {
        return setupJob(file, FreeStyleJob.class, FindBugsFreestyleSettings.class, buildConfigurator);
    }

    /**
     * Builds a job and checks if warnings of Findbugs are displayed. Checks as well, if the content of the tabs is
     * the one we expect.
     */
    @Test
    public void should_find_warnings_in_freestyle_job() {
        FreeStyleJob job = createFreeStyleJob();

        Build build = buildSuccessfulJob(job);

        assertThatFindBugsResultExists(job, build);

        build.open();

        FindBugsAction action = new FindBugsAction(build);

        assertThatWarningsCountInSummaryIs(action, 6);
        assertThatNewWarningsCountInSummaryIs(action, 6);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(6));
        assertThat(action.getNumberOfNewWarnings(), is(6));
        assertThat(action.getNumberOfFixedWarnings(), is(0));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(2));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(4));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(0));

        assertThatFilesTabIsCorrectlyFilled(action);
        assertThatCategoriesTabIsCorrectlyFilled(action);
        assertThatTypesTabIsCorrectlyFilled(action);
        assertThatWarningsTabIsCorrectlyFilled(action);
    }

    private void assertThatFilesTabIsCorrectlyFilled(FindBugsAction action) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("SSHConnector.java", 1);
        expectedContent.put("SSHLauncher.java", 5);
        assertThat(action.getFileTabContents(), is(expectedContent));
    }

    private void assertThatCategoriesTabIsCorrectlyFilled(FindBugsAction action) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("BAD_PRACTICE", 1);
        expectedContent.put("CORRECTNESS", 3);
        expectedContent.put("STYLE", 2);
        assertThat(action.getCategoriesTabContents(), is(expectedContent));
    }

    private void assertThatTypesTabIsCorrectlyFilled(FindBugsAction action) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("DE_MIGHT_IGNORE", 1);
        expectedContent.put("NP_NULL_ON_SOME_PATH", 1);
        expectedContent.put("NP_NULL_PARAM_DEREF", 2);
        expectedContent.put("REC_CATCH_EXCEPTION", 2);
        assertThat(action.getTypesTabContents(), is(expectedContent));
    }

    private void assertThatWarningsTabIsCorrectlyFilled(FindBugsAction action) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("SSHConnector.java:138", 138);
        expectedContent.put("SSHLauncher.java:437", 437);
        expectedContent.put("SSHLauncher.java:679", 679);
        expectedContent.put("SSHLauncher.java:960", 960);
        expectedContent.put("SSHLauncher.java:960", 960);
        expectedContent.put("SSHLauncher.java:971", 971);
        assertThat(action.getWarningsTabContents(), is(expectedContent));
    }

    /**
     * Builds a job and tests if the findbugs api (with depth=0 parameter set) responds with the expected output.
     * Difference in whitespaces are ok.
     */
    @Test
    public void should_return_results_via_remote_api() throws IOException, SAXException, ParserConfigurationException {
        FreeStyleJob job = createFreeStyleJob();

        Build build = buildSuccessfulJob(job);

        assertXmlApiMatchesExpected(build, "findbugsResult/api/xml?depth=0", PLUGIN_ROOT + "api_depth_0.xml");
    }

    /**
     * Runs job two times to check if new and fixed warnings are displayed. Afterwards, the first build
     * is deleted and Jenkins is restarted. Then the results of the second build are validated again: the detail
     * pages should then show the same results (see JENKINS-24940).
     */
    @Test @Issue("24940")
    public void should_report_new_and_fixed_warnings_in_consecutive_builds() {
        FreeStyleJob job = createFreeStyleJob();
        Build firstBuild = buildJobAndWait(job);
        editJob("/findbugs_plugin/forSecondRun/findbugsXml.xml", false, job);

        Build lastBuild = buildSuccessfulJob(job);

        assertThatFindBugsResultExists(job, lastBuild);

        lastBuild.open();

        verifyWarningCounts(lastBuild);

        firstBuild.delete();
        jenkins.restart();
        lastBuild.open();

        verifyWarningCounts(lastBuild);
    }

    private void verifyWarningCounts(final Build build) {
        FindBugsAction action = new FindBugsAction(build);

        assertThatWarningsCountInSummaryIs(action, 5);
        assertThatNewWarningsCountInSummaryIs(action, 1);
        assertThatFixedWarningsCountInSummaryIs(action, 2);

        action.open();

        assertThat(action.getNumberOfWarnings(), is(5));
        assertThat(action.getNumberOfNewWarnings(), is(1));
        assertThat(action.getNumberOfFixedWarnings(), is(2));
        assertThat(action.getNumberOfWarningsWithHighPriority(), is(3));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(2));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(0));

        action.openNew();

        assertThat(action.getNumberOfWarningsWithHighPriority(), is(1));
        assertThat(action.getNumberOfWarningsWithNormalPriority(), is(0));
        assertThat(action.getNumberOfWarningsWithLowPriority(), is(0));

        action.openFixed();

        assertThat(action.getNumberOfRowsInFixedWarningsTable(), is(2));
    }

    /**
     * Runs job two times to check if the links of the graph are relative.
     */
    @Test @Issue("JENKINS-21723")
    public void should_have_relative_graph_links() {
        FreeStyleJob job = createFreeStyleJob();
        buildJobAndWait(job);
        editJob("/findbugs_plugin/forSecondRun/findbugsXml.xml", false, job);

        buildSuccessfulJob(job);

        assertAreaLinksOfJobAreLike(job, "findbugs");
    }

    /**
     * Builds a freestyle project and checks if new warning are displayed.
     */
    @Test
    public void should_link_to_source_code_in_real_project() {
        AnalysisConfigurator<FindBugsFreestyleSettings> buildConfigurator = new AnalysisConfigurator<FindBugsFreestyleSettings>() {
            @Override
            public void configure(FindBugsFreestyleSettings settings) {
                settings.pattern.set("target/findbugsXml.xml");
            }
        };
        FreeStyleJob job = setupJob("/findbugs_plugin/sample_findbugs_project", FreeStyleJob.class,
                FindBugsFreestyleSettings.class, buildConfigurator, "clean package findbugs:findbugs");

        Build build = buildSuccessfulJob(job);

        assertThatFindBugsResultExists(job, build);

        build.open();

        FindBugsAction action = new FindBugsAction(build);
        action.open();

        assertThat(action.getNumberOfNewWarnings(), is(1));

        verifySourceLine(action, "Main.java", 18,
                "18         if(o == null) {",
                "Redundant nullcheck of o, which is known to be non-null in Main.main(String[])");
    }

    private MavenModuleSet createMavenJob() {
        return createMavenJob(null);
    }

    private MavenModuleSet createMavenJob(AnalysisConfigurator<FindBugsMavenSettings> configurator) {
        return setupMavenJob("/findbugs_plugin/sample_findbugs_project", "clean package findbugs:findbugs",
                FindBugsMavenSettings.class, configurator);
    }

    /**
     * Builds a maven project and checks if a new warning is displayed.
     */
    @Test @Category(SmokeTest.class)
    public void should_retrieve_results_from_maven_job() {
        MavenModuleSet job = createMavenJob();

        Build build = buildSuccessfulJob(job);

        assertThatFindBugsResultExists(job, build);

        build.open();

        FindBugsAction action = new FindBugsAction(build);
        action.open();

        assertThat(action.getNumberOfNewWarnings(), is(1));
    }

    /**
     * Builds a maven project and checks if it is unstable.
     */
    @Test
    public void should_set_result_to_unstable_if_warning_found() {
        MavenModuleSet job = createMavenJob(new AnalysisConfigurator<FindBugsMavenSettings>() {
            @Override
            public void configure(FindBugsMavenSettings settings) {
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
        MavenModuleSet job = createMavenJob(new AnalysisConfigurator<FindBugsMavenSettings>() {
            @Override
            public void configure(FindBugsMavenSettings settings) {
                settings.setBuildFailedTotalAll("0");
            }
        });

        buildJobAndWait(job).shouldFail();
    }

    /**
     * Builds a job on an slave and checks if warnings of Findbugs are displayed.
     */
    @Test
    public void should_retrieve_results_from_slave() throws ExecutionException, InterruptedException {
        FreeStyleJob job = createFreeStyleJob();
        Node slave = makeASlaveAndConfigureJob(job);

        Build lastBuild = buildJobOnSlaveWithSuccess(job, slave);

        assertThat(lastBuild.getNode(), is(slave));
        assertThatFindBugsResultExists(job, lastBuild);
        assertThatFindBugsResultExists(job, job);
    }

    private void assertThatFindBugsResultExists(final Job job, final PageObject build) {
        String actionName = "FindBugs Warnings";
        assertThat(job, hasAction(actionName));
        assertThat(job.getLastBuild(), hasAction(actionName));
        assertThat(build, hasAction(actionName));
    }

    /**
     * Sets up a list view with a warnings column. Builds a job and checks if the column shows the correct number of
     * warnings and provides a direct link to the actual warning results.
     */
    @Test @Issue("24436")
    public void should_set_warnings_count_in_list_view_column() {
        MavenModuleSet job = createMavenJob();
        buildJobAndWait(job).shouldSucceed();

        ListView view = addListViewColumn(FindBugsColumn.class);

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
        buildSuccessfulJob(job);

        DashboardView view = addDashboardViewAndBottomPortlet(FindBugsPortlet.class);

        assertValidLink(job.name);
        view.delete();
    }

    private void assertValidLink(final String jobName) {
        By warningsLinkMatcher = by.css("a[href$='job/" + jobName + "/findbugs']");

        assertThat(jenkins.all(warningsLinkMatcher).size(), is(1));
        WebElement link = jenkins.getElement(warningsLinkMatcher);
        assertThat(link.getText().trim(), is("1"));

        link.click();
        assertThat(driver, hasContent("FindBugs Result"));
    }
}
