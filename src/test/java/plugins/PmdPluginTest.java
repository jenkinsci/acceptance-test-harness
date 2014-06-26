package plugins;

import org.jenkinsci.test.acceptance.junit.Bug;
import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AbstractCodeStylePluginBuildConfigurator;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.plugins.pmd.*;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.Slave;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasAction;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

/**
 * Feature: Tests for PMD plugin
 */
@WithPlugins("pmd")
public class PmdPluginTest extends AbstractCodeStylePluginHelper {

    /**
     * Scenario: Configure a job with PMD post-build steps
     * Given I have installed the "pmd" plugin
     * And a job
     * When I configure the job
     * And I add "Publish PMD analysis results" post-build action
     * And I copy resource "pmd_plugin/pmd.xml" into workspace
     * And I set path to the pmd result "pmd.xml"
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And build page should has pmd summary "0 warnings"
     */
    @Test
    public void configure_a_job_with_PMD_post_build_steps() {
        AbstractCodeStylePluginBuildConfigurator<PmdFreestyleBuildSettings> buildConfigurator = new AbstractCodeStylePluginBuildConfigurator<PmdFreestyleBuildSettings>() {
            @Override
            public void configure(PmdFreestyleBuildSettings settings) {
                settings.pattern.set("pmd.xml");
            }
        };

        FreeStyleJob job = setupJob("/pmd_plugin/pmd.xml", FreeStyleJob.class,
                PmdFreestyleBuildSettings.class, buildConfigurator);

        Build lastBuild = buildJobWithSuccess(job);

        assertThat(lastBuild.open(), hasContent("0 warnings"));
    }

    /**
     * Scenario: Configure a job with PMD post-build steps to run always
     * Given I have installed the "pmd" plugin
     * And a job
     * When I configure the job
     * And I add "Publish PMD analysis results" post-build action
     * And I copy resource "pmd_plugin/pmd.xml" into workspace
     * And I set path to the pmd result "pmd.xml"
     * And I add always fail build step
     * And I set publish always pdm
     * And I save the job
     * And I build the job
     * Then the build should fail
     * And build page should has pmd summary "0 warnings"
     */
    @Test
    public void configure_a_job_with_PMD_post_build_steps_run_always() {
        AbstractCodeStylePluginBuildConfigurator<PmdFreestyleBuildSettings> buildConfigurator = new AbstractCodeStylePluginBuildConfigurator<PmdFreestyleBuildSettings>() {
            @Override
            public void configure(PmdFreestyleBuildSettings settings) {
                settings.pattern.set("pmd.xml");
                settings.setCanRunOnFailed(true);


            }
        };

        FreeStyleJob job = setupJob("/pmd_plugin/pmd.xml", FreeStyleJob.class,
                PmdFreestyleBuildSettings.class, buildConfigurator);

        // TODO Maybe edit resource to check whether it's a directory, file or normal step?
        job.configure();
        job.addShellStep("false");
        job.save();

        Build b = job.startBuild().waitUntilFinished().shouldFail();

        assertThat(b.open(), hasContent("0 warnings"));
    }

    /**
     * Scenario: Configure a job with PMD post-build steps which display some warnings
     * Given I have installed the "pmd" plugin
     * And a job
     * When I configure the job
     * And I add "Publish PMD analysis results" post-build action
     * And I copy resource "pmd_plugin/pmd-warnings.xml" into workspace
     * And I set path to the pmd result "pmd-warnings.xml"
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And the build should have "PMD Warnings" action
     * And build page should has pmd summary "9 warnings"
     */
    @Test
    public void configure_a_job_with_PMD_post_build_steps_which_display_some_warnings() {
        AbstractCodeStylePluginBuildConfigurator<PmdFreestyleBuildSettings> buildConfigurator = new AbstractCodeStylePluginBuildConfigurator<PmdFreestyleBuildSettings>() {
            @Override
            public void configure(PmdFreestyleBuildSettings settings) {
                settings.pattern.set("pmd-warnings.xml");
            }
        };

        FreeStyleJob job = setupJob("/pmd_plugin/pmd-warnings.xml", FreeStyleJob.class,
                PmdFreestyleBuildSettings.class, buildConfigurator);

        Build lastBuild = buildJobWithSuccess(job);
        assertThat(lastBuild, hasAction("PMD Warnings"));
        lastBuild.open();
        PmdAction pa = new PmdAction(job);
        assertThat(pa.getResultLinkByXPathText("9 warnings"), is("pmdResult"));
        assertThat(pa.getResultLinkByXPathText("9 new warnings"), is("pmdResult/new"));
        assertThat(pa.getWarningNumber(), is(9));
        assertThat(pa.getNewWarningNumber(), is(9));
        assertThat(pa.getFixedWarningNumber(), is(0));
        assertThat(pa.getHighWarningNumber(), is(0));
        assertThat(pa.getNormalWarningNumber(), is(3));
        assertThat(pa.getLowWarningNumber(), is(6));
        assertFileTab(pa);
        assertTypeTab(pa);
        assertWarningsTab(pa);
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

    /*
     * Builds a job and tests if the PMD api (with depth=0 parameter set) responds with the expected output.
     * Difference in whitespaces are ok.
     */
    @Test
    @Category(SmokeTest.class)
    public void xml_api_report_depth_0() throws IOException, SAXException, ParserConfigurationException {
        AbstractCodeStylePluginBuildConfigurator<PmdFreestyleBuildSettings> buildConfigurator = new AbstractCodeStylePluginBuildConfigurator<PmdFreestyleBuildSettings>() {
            @Override
            public void configure(PmdFreestyleBuildSettings settings) {
                settings.pattern.set("pmd-warnings.xml");
            }
        };

        FreeStyleJob job = setupJob("/pmd_plugin/pmd-warnings.xml", FreeStyleJob.class,
                PmdFreestyleBuildSettings.class, buildConfigurator);

        Build build = buildJobWithSuccess(job);
        String apiUrl = "pmdResult/api/xml?depth=0";
        String expectedXmlPath = "/pmd_plugin/api_depth_0.xml";
        assertXmlApiMatchesExpected(build, apiUrl, expectedXmlPath);
    }

    /**
     * Runs job two times to check if new and fixed warnings are displayed.
     */
    @Test
    public void configure_a_job_with_PMD_post_build_steps_which_display_some_warnings_two_runs() {
        FreeStyleJob job = setUpPmdFreestyleJob();
        buildJobAndWait(job);
        editJob("/pmd_plugin/forSecondRun/pmd-warnings.xml", false, job);
        Build lastBuild = buildJobWithSuccess(job);
        assertThat(lastBuild, hasAction("PMD Warnings"));
        lastBuild.open();
        PmdAction pa = new PmdAction(job);
        assertThat(pa.getResultLinkByXPathText("8 warnings"), is("pmdResult"));
        assertThat(pa.getResultLinkByXPathText("1 new warning"), is("pmdResult/new"));
        assertThat(pa.getResultLinkByXPathText("2 fixed warnings"), is("pmdResult/fixed"));
        assertThat(pa.getWarningNumber(), is(8));
        assertThat(pa.getNewWarningNumber(), is(1));
        assertThat(pa.getFixedWarningNumber(), is(2));
        assertThat(pa.getHighWarningNumber(), is(0));
        assertThat(pa.getNormalWarningNumber(), is(2));
        assertThat(pa.getLowWarningNumber(), is(6));
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
        editJob("/pmd_plugin/forSecondRun/pmd-warnings.xml", false, job);
        buildJobWithSuccess(job);

        assertAreaLinksOfJobAreLike(job, "^\\d+/pmdResult");
    }

    /*
     * Runs a job with warning threshold configured once and validates that build is marked as unstable.
     */
    @Test
    @Bug("19614")
    public void build_with_warning_threshold_set_should_be_unstable() {
        AbstractCodeStylePluginBuildConfigurator<PmdFreestyleBuildSettings> buildConfigurator = new AbstractCodeStylePluginBuildConfigurator<PmdFreestyleBuildSettings>() {
            @Override
            public void configure(PmdFreestyleBuildSettings settings) {
                settings.pattern.set("pmd-warnings.xml");
                settings.setBuildUnstableTotalAll("0");
                settings.setNewWarningsThresholdFailed("0");
                settings.setUseDeltaValues(true);
            }
        };
        FreeStyleJob job = setupJob("/pmd_plugin/pmd-warnings.xml", FreeStyleJob.class,
                PmdFreestyleBuildSettings.class, buildConfigurator);

        Build build = buildJobAndWait(job);
        assertThat(build.isUnstable(), is(true));
    }

    private MavenModuleSet setupSimpleMavenJob() {
        return setupSimpleMavenJob(null);
    }

    private MavenModuleSet setupSimpleMavenJob(AbstractCodeStylePluginBuildConfigurator<PmdMavenBuildSettings> configurator) {
        String projectPath = "/pmd_plugin/sample_pmd_project";
        String goal = "clean package pmd:pmd";
        return setupMavenJob(projectPath, goal, PmdMavenBuildSettings.class, configurator);
    }

    /**
     * Builds a freestyle project and checks if new warning are displayed.
     */
    @Test
    public void build_simple_freestyle_mavengoals_project() {
        AbstractCodeStylePluginBuildConfigurator<PmdFreestyleBuildSettings> buildConfigurator = new AbstractCodeStylePluginBuildConfigurator<PmdFreestyleBuildSettings>() {
            @Override
            public void configure(PmdFreestyleBuildSettings settings) {
                settings.pattern.set("target/pmd.xml");
            }
        };
        FreeStyleJob job = setupJob("/pmd_plugin/sample_pmd_project", FreeStyleJob.class, PmdFreestyleBuildSettings.class, buildConfigurator, "clean package pmd:pmd"
        );
        Build lastBuild = buildJobWithSuccess(job);
        assertThat(lastBuild, hasAction("PMD Warnings"));
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
        assertThat(lastBuild, hasAction("PMD Warnings"));
        lastBuild.open();
        PmdAction pmd = new PmdAction(job);
        assertThat(pmd.getNewWarningNumber(), is(2));
    }

    /**
     * Builds a maven project and checks if it is unstable.
     */
    @Test
    public void build_simple_maven_project_and_check_if_it_is_unstable() {
        AbstractCodeStylePluginBuildConfigurator<PmdMavenBuildSettings> buildConfigurator =
                new AbstractCodeStylePluginBuildConfigurator<PmdMavenBuildSettings>() {
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
        AbstractCodeStylePluginBuildConfigurator<PmdMavenBuildSettings> buildConfigurator =
                new AbstractCodeStylePluginBuildConfigurator<PmdMavenBuildSettings>() {
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
        Slave slave = makeASlaveAndConfigureJob(job);
        Build build = buildJobOnSlaveWithSuccess(job, slave);
        assertThat(build.getNode(), is(slave.getName()));
        assertThat(build, hasAction("PMD Warnings"));
        assertThat(job, hasAction("PMD Warnings"));
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
        AbstractCodeStylePluginBuildConfigurator<PmdFreestyleBuildSettings> buildConfigurator = new AbstractCodeStylePluginBuildConfigurator<PmdFreestyleBuildSettings>() {
            @Override
            public void configure(PmdFreestyleBuildSettings settings) {
                settings.pattern.set("pmd-warnings.xml");
            }
        };
        return setupJob("/pmd_plugin/pmd-warnings.xml", FreeStyleJob.class,
                PmdFreestyleBuildSettings.class, buildConfigurator);
    }

}
