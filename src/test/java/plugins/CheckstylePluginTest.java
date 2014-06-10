package plugins;

import org.jenkinsci.test.acceptance.junit.Bug;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginMavenBuildConfigurator;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckstyleAction;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckstyleMavenBuildSettings;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckstylePublisher;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Slave;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasAction;

/**
 * Feature: Allow publishing of Checkstyle report
 * In order to be able to check code style of my project
 * As a Jenkins user
 * I want to be able to publish Checkstyle report
 */
@WithPlugins("checkstyle")
public class CheckstylePluginTest extends AbstractCodeStylePluginHelper {

    /**
     * Builds a job with checkstyle enabled and verifies that checkstyle details are displayed in the build overview.
     */
    @Test
    public void record_checkstyle_report() {
        FreeStyleJob job = setupJob("/checkstyle_plugin/checkstyle-result.xml", CheckstylePublisher.class, "checkstyle-result.xml");
        buildJobWithSuccess(job);

        assertThat(job.getLastBuild(), hasAction("Checkstyle Warnings"));
        assertThat(job, hasAction("Checkstyle Warnings"));
    }

    /**
     * Builds a job with checkstyle and verifies that the information checkstyle provides in the tabs about the build
     * are the information we expect.
     */
    @Test
    public void view_checkstyle_report() {
        final FreeStyleJob job = setupJob("/checkstyle_plugin/checkstyle-result.xml", CheckstylePublisher.class, "checkstyle-result.xml");
        buildJobWithSuccess(job).open();

        final CheckstyleAction ca = new CheckstyleAction(job);
        assertThat(ca.getResultLinkByXPathText("776 warnings"), is("checkstyleResult"));
        assertThat(ca.getResultLinkByXPathText("776 new warnings"), is("checkstyleResult/new"));
        assertThat(ca.getWarningNumber(), is(776));
        assertThat(ca.getNewWarningNumber(), is(776));
        assertThat(ca.getFixedWarningNumber(), is(0));
        assertThat(ca.getHighWarningNumber(), is(776));
        assertThat(ca.getNormalWarningNumber(), is(0));
        assertThat(ca.getLowWarningNumber(), is(0));
        assertFileTab(ca);
        assertCategoryTab(ca);
        assertTypeTab(ca);
    }

    private void assertFileTab(final CheckstyleAction ca) {
        final SortedMap<String, Integer> expectedFileDetails = new TreeMap<>();
        expectedFileDetails.put("JavaProvider.java", 18);
        expectedFileDetails.put("PluginImpl.java", 8);
        expectedFileDetails.put("RemoteLauncher.java", 63);
        expectedFileDetails.put("SFTPClient.java", 76);
        expectedFileDetails.put("SFTPFileSystem.java", 34);
        expectedFileDetails.put("SSHConnector.java", 96);
        expectedFileDetails.put("SSHLauncher.java", 481);
        assertThat(ca.getFileTabContents(), is(expectedFileDetails));
    }

    private void assertCategoryTab(final CheckstyleAction ca) {
        final SortedMap<String, Integer> expectedCategories = new TreeMap<>();
        expectedCategories.put("Blocks", 28);
        expectedCategories.put("Checks", 123);
        expectedCategories.put("Coding", 61);
        expectedCategories.put("Design", 47);
        expectedCategories.put("Imports", 3);
        expectedCategories.put("Javadoc", 104);
        expectedCategories.put("Naming", 4);
        expectedCategories.put("Regexp", 23);
        expectedCategories.put("Sizes", 164);
        expectedCategories.put("Whitespace", 219);
        assertThat(ca.getCategoriesTabContents(), is(expectedCategories));
    }

    private void assertTypeTab(final CheckstyleAction ca) {
        final SortedMap<String, Integer> expectedTypes = new TreeMap<>();
        expectedTypes.put("AvoidInlineConditionalsCheck", 9);
        expectedTypes.put("AvoidStarImportCheck", 1);
        expectedTypes.put("ConstantNameCheck", 1);
        expectedTypes.put("DesignForExtensionCheck", 35);
        expectedTypes.put("EmptyBlockCheck", 1);
        expectedTypes.put("FileTabCharacterCheck", 47);
        expectedTypes.put("FinalParametersCheck", 120);
        expectedTypes.put("HiddenFieldCheck", 44);
        expectedTypes.put("JavadocMethodCheck", 88);
        expectedTypes.put("JavadocPackageCheck", 1);
        expectedTypes.put("JavadocStyleCheck", 9);
        expectedTypes.put("JavadocTypeCheck", 3);
        expectedTypes.put("JavadocVariableCheck", 3);
        expectedTypes.put("LineLengthCheck", 160);
        expectedTypes.put("MagicNumberCheck", 8);
        expectedTypes.put("MethodNameCheck", 1);
        expectedTypes.put("NeedBracesCheck", 26);
        expectedTypes.put("ParameterNameCheck", 2);
        expectedTypes.put("ParameterNumberCheck", 4);
        expectedTypes.put("RegexpSinglelineCheck", 23);
        expectedTypes.put("RightCurlyCheck", 1);
        expectedTypes.put("TodoCommentCheck", 3);
        expectedTypes.put("UnusedImportsCheck", 2);
        expectedTypes.put("VisibilityModifierCheck", 12);
        expectedTypes.put("WhitespaceAfterCheck", 66);
        expectedTypes.put("WhitespaceAroundCheck", 106);
        assertThat(ca.getTypesTabContents(), is(expectedTypes));
    }

    /**
     * Builds a job and tests if the checkstyle api (with depth=0 parameter set) responds with the expected output.
     * Difference in whitespaces are ok.
     */
    @Test
    public void xml_api_report_depth_0() throws IOException, SAXException, ParserConfigurationException {
        final FreeStyleJob job = setupJob("/checkstyle_plugin/checkstyle-result.xml", CheckstylePublisher.class, "checkstyle-result.xml");
        final Build build = buildJobWithSuccess(job);
        final String apiUrl = "checkstyleResult/api/xml?depth=0";
        final String expectedXmlPath = "/checkstyle_plugin/api_depth_0.xml";
        assertXmlApiMatchesExpected(build, apiUrl, expectedXmlPath);
    }

    /**
     * Runs job two times to check if new and fixed warnings are displayed.
     */
    @Test
    public void view_checkstyle_report_two_runs_and_changed_results() {
        FreeStyleJob job = setupJob("/checkstyle_plugin/checkstyle-result.xml", CheckstylePublisher.class, "checkstyle-result.xml");
        buildJobAndWait(job);
        editJobAndChangeLastRessource(job, "/checkstyle_plugin/checkstyle-result-2.xml", "checkstyle-result.xml");

        Build lastBuild = buildJobWithSuccess(job);
        assertThat(lastBuild, hasAction("Checkstyle Warnings"));
        lastBuild.open();
        CheckstyleAction ca = new CheckstyleAction(job);
        assertThat(ca.getResultLinkByXPathText("679 warnings"), is("checkstyleResult"));
        assertThat(ca.getResultLinkByXPathText("3 new warnings"), is("checkstyleResult/new"));
        assertThat(ca.getResultLinkByXPathText("100 fixed warnings"), is("checkstyleResult/fixed"));
        assertThat(ca.getWarningNumber(), is(679));
        assertThat(ca.getNewWarningNumber(), is(3));
        assertThat(ca.getFixedWarningNumber(), is(100));
        assertThat(ca.getHighWarningNumber(), is(679));
        assertThat(ca.getNormalWarningNumber(), is(0));
        assertThat(ca.getLowWarningNumber(), is(0));
    }

    /**
     * Runs job two times to check if the links of the graph are relative.
     */
    @Test
    @Bug("21723")
    @Ignore("Until JENKINS-21723 is fixed")
    public void view_checkstyle_report_job_graph_links() throws Exception {
        FreeStyleJob job = setupJob("/checkstyle_plugin/checkstyle-result.xml", CheckstylePublisher.class, "checkstyle-result.xml");
        buildJobAndWait(job);
        editJobAndChangeLastRessource(job, "/checkstyle_plugin/checkstyle-result-2.xml", "checkstyle-result.xml");
        buildJobWithSuccess(job);

        assertAreaLinksOfJobAreLike(job, "^\\d+/checkstyleResult");
    }

    private MavenModuleSet setupSimpleMavenJob() {
        return setupSimpleMavenJob(null);
    }

    private MavenModuleSet setupSimpleMavenJob(AbstractCodeStylePluginMavenBuildConfigurator<CheckstyleMavenBuildSettings> configurator) {
        final String projectPath = "/checkstyle_plugin/sample_checkstyle_project";
        final String goal = "clean package checkstyle:checkstyle";
        return setupMavenJob(projectPath, goal, CheckstyleMavenBuildSettings.class, configurator);
    }

    /**
     * Builds a freestyle project and checks if new warning are displayed.
     */
    @Test
    public void build_simple_freestyle_mavengoals_project() {
        final FreeStyleJob job = setupFreestyleJobWithMavenGoals("/checkstyle_plugin/sample_checkstyle_project", "clean package checkstyle:checkstyle", CheckstylePublisher.class, "target/checkstyle-result.xml");
        Build lastBuild = buildJobWithSuccess(job);
        assertThat(lastBuild, hasAction("Checkstyle Warnings"));
        lastBuild.open();
        CheckstyleAction checkstyle = new CheckstyleAction(job);
        assertThat(checkstyle.getNewWarningNumber(), is(12));
    }

    /**
     * Builds a maven project and checks if new warning are displayed.
     */
    @Test
    public void build_simple_maven_project() {
        final MavenModuleSet job = setupSimpleMavenJob();
        Build lastBuild = buildJobWithSuccess(job);
        assertThat(lastBuild, hasAction("Checkstyle Warnings"));
        lastBuild.open();
        CheckstyleAction checkstyle = new CheckstyleAction(job);
        assertThat(checkstyle.getNewWarningNumber(), is(12));
    }

    /**
     * Builds a maven project and checks if it is unstable.
     */
    @Test
    public void build_simple_maven_project_and_check_if_it_is_unstable() {
        final AbstractCodeStylePluginMavenBuildConfigurator<CheckstyleMavenBuildSettings> buildConfigurator =
                new AbstractCodeStylePluginMavenBuildConfigurator<CheckstyleMavenBuildSettings>() {
                    @Override
                    public void configure(CheckstyleMavenBuildSettings settings) {
                        settings.setBuildUnstableTotalAll("0");
                    }
                };
        final MavenModuleSet job = setupSimpleMavenJob(buildConfigurator);
        buildJobAndWait(job).shouldBeUnstable();
    }

    /**
     * Builds a maven project and checks if it failed.
     */
    @Test
    public void build_simple_maven_project_and_check_if_failed() {
        final AbstractCodeStylePluginMavenBuildConfigurator<CheckstyleMavenBuildSettings> buildConfigurator =
                new AbstractCodeStylePluginMavenBuildConfigurator<CheckstyleMavenBuildSettings>() {
                    @Override
                    public void configure(CheckstyleMavenBuildSettings settings) {
                        settings.setBuildFailedTotalAll("0");
                    }
                };
        final MavenModuleSet job = setupSimpleMavenJob(buildConfigurator);
        buildJobAndWait(job).shouldFail();
    }

    /**
     * Builds a job on a slave with checkstyle and verifies that the information checkstyle provides in the tabs about the build
     * are the information we expect.
     */
    @Test
    public void view_checkstyle_report_build_on_slave() throws Exception {
        FreeStyleJob job = setupJob("/checkstyle_plugin/checkstyle-result.xml", CheckstylePublisher.class, "checkstyle-result.xml");

        Slave slave = makeASlaveAndConfigureJob(job);

        Build build = buildJobOnSlaveWithSuccess(job, slave);

        assertThat(build.getNode(), is(slave.getName()));
        assertThat(job.getLastBuild(), hasAction("Checkstyle Warnings"));
        assertThat(job, hasAction("Checkstyle Warnings"));
    }

}
