package plugins;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.xerces.jaxp.DocumentBuilderFactoryImpl;
import org.custommonkey.xmlunit.XMLAssert;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckstyleAction;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckstylePublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Feature: Allow publishing of Checkstyle report
   In order to be able to check code style of my project
   As a Jenkins user
   I want to be able to publish Checkstyle report
 */
@WithPlugins("checkstyle")
public class CheckstylePluginTest extends AbstractJUnitTest {
    /**
     * Scenario: Record Checkstyle report
         Given I have installed the "checkstyle" plugin
         And a job
         When I configure the job
         And I copy resource "checkstyle_plugin/checkstyle-result.xml" into workspace
         And I add "Publish Checkstyle analysis results" post-build action
         And I set up "checkstyle-result.xml" as the Checkstyle results
         And I save the job
         And I build the job
         Then the build should have "Checkstyle Warnings" action
         And the job should have "Checkstyle Warnings" action
     */
    @Test
    public void record_checkstyle_report() {
        FreeStyleJob job = setupJob();
        Build b = job.queueBuild().waitUntilFinished().shouldSucceed();

        assertThat(b, hasAction("Checkstyle Warnings"));
        assertThat(job, hasAction("Checkstyle Warnings"));
    }

    /**
     *   Scenario: View Checkstyle report
         Given I have installed the "checkstyle" plugin
         And a job
         When I configure the job
         And I copy resource "checkstyle_plugin/checkstyle-result.xml" into workspace
         And I add "Publish Checkstyle analysis results" post-build action
         And I set up "checkstyle-result.xml" as the Checkstyle results
         And I save the job
         And I build the job
         Then the build should succeed
         When I visit Checkstyle report
         Then I should see there are 776 warnings
         And I should see there are 776 new warnings
         And I should see there are 0 fixed warnings
         And I should see there are 776 high priority warnings
         And I should see there are 0 normal priority warnings
         And I should see there are 0 low priority warnings
     */
    @Test
    public void view_checkstyle_report() {
        final FreeStyleJob job = setupJob();
        final Build b = job.queueBuild().waitUntilFinished().shouldSucceed();
        final CheckstyleAction ca = new CheckstyleAction(job);

        assertWarningsTrendAndSummary(ca);
        assertFileTab(ca);
        assertCategoryTab(ca);
        assertTypeTab(ca);
    }

    private void assertWarningsTrendAndSummary(final CheckstyleAction ca) {
        assertThat(ca.getWarningNumber(), is(776));
        assertThat(ca.getNewWarningNumber(), is(776));
        assertThat(ca.getFixedWarningNumber(), is(0));
        assertThat(ca.getHighWarningNumber(), is(776));
        assertThat(ca.getNormalWarningNumber(), is(0));
        assertThat(ca.getLowWarningNumber(), is(0));
    }

    private void assertTypeTab(final CheckstyleAction ca) {
        final SortedMap<String, Integer> expectedTypes = new TreeMap<String, Integer>();
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
        assertThat(ca.getTypesAndNumberOfWarnings(), is(expectedTypes));
    }

    private void assertCategoryTab(final CheckstyleAction ca) {
        final SortedMap<String, Integer> expectedCategories = new TreeMap<String, Integer>();
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
        assertThat(ca.getCategoriesAndNumberOfWarnings(), is(expectedCategories));
    }

    private void assertFileTab(final CheckstyleAction ca) {
        final SortedMap<String, Integer> expectedFileDetails = new TreeMap<String, Integer>();
        expectedFileDetails.put("JavaProvider.java", 18);
        expectedFileDetails.put("PluginImpl.java", 8);
        expectedFileDetails.put("RemoteLauncher.java", 63);
        expectedFileDetails.put("SFTPClient.java", 76);
        expectedFileDetails.put("SFTPFileSystem.java", 34);
        expectedFileDetails.put("SSHConnector.java", 96);
        expectedFileDetails.put("SSHLauncher.java", 481);
        assertThat(ca.getFileNamesAndNumberOfWarnings(), is(expectedFileDetails));
    }

    @Test
    public void xml_api_report_depth_0() throws IOException, SAXException, ParserConfigurationException {
        final FreeStyleJob job = setupJob();
        final Build build = job.queueBuild().waitUntilFinished().shouldSucceed();
        final String xmlUrl = build.url("checkstyleResult/api/xml?depth=0").toString();
        final DocumentBuilder documentBuilder = DocumentBuilderFactoryImpl.newInstance().newDocumentBuilder();
        final Document result = documentBuilder.parse(xmlUrl);

        final Document expected = documentBuilder.parse(resource("/checkstyle_plugin/api_depth_0.xml").asFile());
        XMLAssert.assertXMLEqual(result, expected);
    }

    private FreeStyleJob setupJob() {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.copyResource(resource("/checkstyle_plugin/checkstyle-result.xml"));
        job.addPublisher(CheckstylePublisher.class)
            .pattern.set("checkstyle-result.xml");
        job.save();
        return job;
    }
}
