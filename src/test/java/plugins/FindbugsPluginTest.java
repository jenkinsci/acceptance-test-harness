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

import org.jenkinsci.test.acceptance.junit.Bug;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindbugsAction;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindbugsPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

@WithPlugins("findbugs")
public class FindbugsPluginTest extends AbstractCodeStylePluginHelper {

    /**
     * Builds a job and checks if warnings of Findbugs are displayed. Checks as well, if the content of the tabs is
     * the one we expect.
     */
    @Test
    public void record_analysis() {
        FreeStyleJob job = setupJob("/findbugs_plugin/findbugsXml.xml", FindbugsPublisher.class, "findbugsXml.xml");
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
        final FreeStyleJob job = setupJob("/findbugs_plugin/findbugsXml.xml", FindbugsPublisher.class, "findbugsXml.xml");
        final Build build = buildJobWithSuccess(job);
        final String apiUrl = "findbugsResult/api/xml?depth=0";
        final String expectedXmlPath = "/findbugs_plugin/api_depth_0.xml";
        assertXmlApiMatchesExpected(build, apiUrl, expectedXmlPath);
    }

    /**
     * Runs job two times to check if new and fixed warnings are displayed.
     */
    @Test
    public void record_analysis_two_runs() {
        final FreeStyleJob job = setupJob("/findbugs_plugin/findbugsXml.xml", FindbugsPublisher.class, "findbugsXml.xml");
        buildJobAndWait(job);
        editJobAndChangeLastRessource(job, "/findbugs_plugin/findbugsXml-2.xml", "findbugsXml.xml");

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
    @Test @Bug("21723")  @Ignore("Until JENKINS-21723 is fixed")
    public void view_findbugs_report_job_graph_links() {
        final FreeStyleJob job = setupJob("/findbugs_plugin/findbugsXml.xml", FindbugsPublisher.class, "findbugsXml.xml");
        buildJobAndWait(job);
        editJobAndChangeLastRessource(job, "/findbugs_plugin/findbugsXml-2.xml", "findbugsXml.xml");
        buildJobWithSuccess(job);

        assertAreaLinksOfJobAreLike(job, "^\\d+/findbugsResult");
    }
}
