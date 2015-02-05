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

import org.jenkinsci.test.acceptance.junit.Bug;
import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisConfigurator;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindbugsAction;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindbugsColumn;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindbugsFreestyleBuildSettings;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindbugsMavenBuildSettings;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindbugsWarningsPerProjectDashboardViewPortlet;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.Node;
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
public class FindbugsPluginTest extends AbstractAnalysisTest {
    private static final String PATTERN_WITH_6_WARNINGS = "findbugsXml.xml";
    private static final String FILE_WITH_6_WARNINGS = "/findbugs_plugin/" + PATTERN_WITH_6_WARNINGS;

    /**
     * Checks that the plug-in sends a mail after a build has been failed. The content of the mail
     * contains several tokens that should be expanded in the mail with the correct values.
     */
    @Test @Bug("25501") @WithPlugins("email-ext")
    public void should_send_mail_with_expanded_tokens() {
        setUpMailer();

        AnalysisConfigurator<FindbugsFreestyleBuildSettings> buildConfigurator =
                new AnalysisConfigurator<FindbugsFreestyleBuildSettings>() {
                    @Override
                    public void configure(FindbugsFreestyleBuildSettings settings) {
                        settings.setBuildFailedTotalAll("0");
                        settings.pattern.set(PATTERN_WITH_6_WARNINGS);
                    }
                };
        FreeStyleJob job = setupJob(FILE_WITH_6_WARNINGS, FreeStyleJob.class,
                FindbugsFreestyleBuildSettings.class, buildConfigurator);

        configureEmailNotification(job, "FindBugs: ${FINDBUGS_RESULT}",
                "FindBugs: ${FINDBUGS_COUNT}-${FINDBUGS_FIXED}-${FINDBUGS_NEW}");

        job.startBuild().shouldFail();

        verifyReceivedMail("FindBugs: FAILURE", "FindBugs: 6-0-6");
    }

    /**
     * Builds a job and checks if warnings of Findbugs are displayed. Checks as well, if the content of the tabs is
     * the one we expect.
     */
    @Test
    public void record_analysis() {
        FreeStyleJob job = setUpFindbugsFreestyleJob();
        Build lastBuild = buildJobWithSuccess(job);

        assertThat(lastBuild, hasAction("FindBugs Warnings"));
        assertThat(job, hasAction("FindBugs Warnings"));
        lastBuild.open();
        FindbugsAction fa = new FindbugsAction(job);
        assertThat(fa.getResultLinkByXPathText("6 warnings"), is("findbugsResult"));
        assertThat(fa.getResultLinkByXPathText("6 new warnings"), is("findbugsResult/new"));
        assertThat(fa.getWarningNumber(), is(6));
        assertThat(fa.getNewWarningNumber(), is(6));
        assertThat(fa.getFixedWarningNumber(), is(0));
        assertThat(fa.getHighWarningNumber(), is(2));
        assertThat(fa.getNormalWarningNumber(), is(4));
        assertThat(fa.getLowWarningNumber(), is(0));

        assertFilesTab(fa);
        assertCategoriesTab(fa);
        assertTypesTab(fa);
        assertWarningsTab(fa);
    }

    private void assertFilesTab(FindbugsAction fa) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("SSHConnector.java", 1);
        expectedContent.put("SSHLauncher.java", 5);
        assertThat(fa.getFileTabContents(), is(expectedContent));
    }

    private void assertCategoriesTab(FindbugsAction fa) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("BAD_PRACTICE", 1);
        expectedContent.put("CORRECTNESS", 3);
        expectedContent.put("STYLE", 2);
        assertThat(fa.getCategoriesTabContents(), is(expectedContent));
    }

    private void assertTypesTab(FindbugsAction fa) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("DE_MIGHT_IGNORE", 1);
        expectedContent.put("NP_NULL_ON_SOME_PATH", 1);
        expectedContent.put("NP_NULL_PARAM_DEREF", 2);
        expectedContent.put("REC_CATCH_EXCEPTION", 2);
        assertThat(fa.getTypesTabContents(), is(expectedContent));
    }

    private void assertWarningsTab(FindbugsAction fa) {
        SortedMap<String, Integer> expectedContent = new TreeMap<>();
        expectedContent.put("SSHConnector.java:138", 138);
        expectedContent.put("SSHLauncher.java:437", 437);
        expectedContent.put("SSHLauncher.java:679", 679);
        expectedContent.put("SSHLauncher.java:960", 960);
        expectedContent.put("SSHLauncher.java:960", 960);
        expectedContent.put("SSHLauncher.java:971", 971);
        assertThat(fa.getWarningsTabContents(), is(expectedContent));
    }

    /**
     * Builds a job and tests if the findbugs api (with depth=0 parameter set) responds with the expected output.
     * Difference in whitespaces are ok.
     */
    @Test
    public void xml_api_report_depth_0() throws IOException, SAXException, ParserConfigurationException {
        FreeStyleJob job = setUpFindbugsFreestyleJob();
        Build build = buildJobWithSuccess(job);
        String apiUrl = "findbugsResult/api/xml?depth=0";
        String expectedXmlPath = "/findbugs_plugin/api_depth_0.xml";
        assertXmlApiMatchesExpected(build, apiUrl, expectedXmlPath);
    }

    /**
     * Runs job two times to check if new and fixed warnings are displayed.
     */
    @Test
    public void record_analysis_two_runs() {
        FreeStyleJob job = setUpFindbugsFreestyleJob();
        buildJobAndWait(job);
        editJob("/findbugs_plugin/forSecondRun/findbugsXml.xml", false, job);

        Build lastBuild = buildJobWithSuccess(job);
        assertThat(lastBuild, hasAction("FindBugs Warnings"));
        lastBuild.open();
        FindbugsAction fa = new FindbugsAction(job);
        assertThat(fa.getResultLinkByXPathText("5 warnings"), is("findbugsResult"));
        assertThat(fa.getResultLinkByXPathText("1 new warning"), is("findbugsResult/new"));
        assertThat(fa.getResultLinkByXPathText("2 fixed warnings"), is("findbugsResult/fixed"));
        assertThat(fa.getWarningNumber(), is(5));
        assertThat(fa.getNewWarningNumber(), is(1));
        assertThat(fa.getFixedWarningNumber(), is(2));
        assertThat(fa.getHighWarningNumber(), is(3));
        assertThat(fa.getNormalWarningNumber(), is(2));
        assertThat(fa.getLowWarningNumber(), is(0));
    }

    /**
     * Runs job two times to check if the links of the graph are relative.
     */
    @Test @Bug("21723")
    public void view_findbugs_report_job_graph_links() {
        FreeStyleJob job = setUpFindbugsFreestyleJob();
        buildJobAndWait(job);
        editJob("/findbugs_plugin/forSecondRun/findbugsXml.xml", false, job);
        buildJobWithSuccess(job);

        assertAreaLinksOfJobAreLike(job, "findbugs");
    }

    /**
     * Builds a freestyle project and checks if new warning are displayed.
     */
    @Test
    public void build_simple_freestyle_mavengoals_project() {
        AnalysisConfigurator<FindbugsFreestyleBuildSettings> buildConfigurator = new AnalysisConfigurator<FindbugsFreestyleBuildSettings>() {
            @Override
            public void configure(FindbugsFreestyleBuildSettings settings) {
                settings.pattern.set("target/findbugsXml.xml");
            }
        };
        FreeStyleJob job = setupJob("/findbugs_plugin/sample_findbugs_project", FreeStyleJob.class,
                FindbugsFreestyleBuildSettings.class, buildConfigurator, "clean package findbugs:findbugs");

        Build lastBuild = buildJobWithSuccess(job);
        assertThat(lastBuild, hasAction("FindBugs Warnings"));
        lastBuild.open();
        FindbugsAction findbugs = new FindbugsAction(job);
        assertThat(findbugs.getNewWarningNumber(), is(1));

        verifySourceLine(findbugs, "Main.java", 18,
                "18         if(o == null) {",
                "Redundant nullcheck of o, which is known to be non-null in Main.main(String[])");
    }

    private MavenModuleSet setupSimpleMavenJob() {
        return setupSimpleMavenJob(null);
    }

    private MavenModuleSet setupSimpleMavenJob(AnalysisConfigurator<FindbugsMavenBuildSettings> configurator) {
        String projectPath = "/findbugs_plugin/sample_findbugs_project";
        String goal = "clean package findbugs:findbugs";
        return setupMavenJob(projectPath, goal, FindbugsMavenBuildSettings.class, configurator);
    }

    /**
     * Builds a maven project and checks if a new warning is displayed.
     */
    @Test @Category(SmokeTest.class)
    public void build_simple_maven_project() {
        MavenModuleSet job = setupSimpleMavenJob();
        Build lastBuild = buildJobWithSuccess(job);
        assertThat(lastBuild, hasAction("FindBugs Warnings"));
        lastBuild.open();
        FindbugsAction findbugs = new FindbugsAction(job);
        assertThat(findbugs.getNewWarningNumber(), is(1));
    }

    /**
     * Builds a maven project and checks if it is unstable.
     */
    @Test
    public void build_simple_maven_project_and_check_if_it_is_unstable() {
        AnalysisConfigurator<FindbugsMavenBuildSettings> buildConfigurator =
                new AnalysisConfigurator<FindbugsMavenBuildSettings>() {
                    @Override
                    public void configure(FindbugsMavenBuildSettings settings) {
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
        AnalysisConfigurator<FindbugsMavenBuildSettings> buildConfigurator =
                new AnalysisConfigurator<FindbugsMavenBuildSettings>() {
                    @Override
                    public void configure(FindbugsMavenBuildSettings settings) {
                        settings.setBuildFailedTotalAll("0");
                    }
                };
        MavenModuleSet job = setupSimpleMavenJob(buildConfigurator);
        buildJobAndWait(job).shouldFail();
    }

    /**
     * Builds a job on an slave and checks if warnings of Findbugs are displayed.
     */
    @Test
    public void record_analysis_build_on_slave() throws ExecutionException, InterruptedException {
        FreeStyleJob job = setUpFindbugsFreestyleJob();

        Node slave = makeASlaveAndConfigureJob(job);

        Build lastBuild = buildJobOnSlaveWithSuccess(job, slave);

        assertThat(lastBuild.getNode(), is(slave));
        assertThat(lastBuild, hasAction("FindBugs Warnings"));
        assertThat(job, hasAction("FindBugs Warnings"));
    }

    /**
     * Build a job and check set up a dashboard list-view. Check, if the dashboard view shows correct warning count.
     */
    @Test
    public void build_a_job_and_check_if_dashboard_list_view_shows_correct_warnings() {
        MavenModuleSet job = setupSimpleMavenJob();
        buildJobAndWait(job).shouldSucceed();
        ListView view = addDashboardListViewColumn(FindbugsColumn.class);

        By expectedDashboardLinkMatcher = by.css("a[href$='job/" + job.name + "/findbugs']");
        assertThat(jenkins.all(expectedDashboardLinkMatcher).size(), is(1));
        WebElement dashboardLink = jenkins.getElement(expectedDashboardLinkMatcher);
        assertThat(dashboardLink.getText().trim(), is("1"));

        view.delete();
    }

    /**
     * Build a job and check set up a "dashboard"-style view. Check, if the dashboard view shows correct warning count.
     */
    @Test @WithPlugins("dashboard-view")
    public void build_a_job_and_check_if_dashboard_view_shows_correct_warnings() {
        MavenModuleSet job = setupSimpleMavenJob();
        buildJobAndWait(job).shouldSucceed();

        DashboardView view = addDashboardViewAndBottomPortlet(FindbugsWarningsPerProjectDashboardViewPortlet.class);

        By expectedDashboardLinkMatcher = by.css("a[href='job/" + job.name + "/findbugs']");
        assertThat(jenkins.all(expectedDashboardLinkMatcher).size(), is(1));
        WebElement dashboardLink = jenkins.getElement(expectedDashboardLinkMatcher);
        assertThat(dashboardLink.getText().trim(), is("1"));

        view.delete();
    }

    /**
     * Makes a Freestyle Job with Findbugs and a warnigns-file.
     *
     * @return The new Job
     */
    private FreeStyleJob setUpFindbugsFreestyleJob() {
        AnalysisConfigurator<FindbugsFreestyleBuildSettings> buildConfigurator = new AnalysisConfigurator<FindbugsFreestyleBuildSettings>() {
            @Override
            public void configure(FindbugsFreestyleBuildSettings settings) {
                settings.pattern.set(PATTERN_WITH_6_WARNINGS);
            }
        };
        FreeStyleJob job = setupJob(FILE_WITH_6_WARNINGS, FreeStyleJob.class,
                FindbugsFreestyleBuildSettings.class, buildConfigurator);
        return job;
    }

}
