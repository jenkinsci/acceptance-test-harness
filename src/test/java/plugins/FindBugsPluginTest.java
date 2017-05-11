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

import java.util.SortedMap;
import java.util.TreeMap;

import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisConfigurator;
import org.jenkinsci.test.acceptance.plugins.analysis_core.NullConfigurator;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindBugsAction;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindBugsFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindBugsMavenSettings;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Container;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.jvnet.hudson.test.Issue;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.Matchers.*;
import static org.junit.Assume.*;

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
    private static final int TOTAL_NUMBER_OF_WARNINGS = 6;

    /**
     * Runs job two times to check if new and fixed warnings are displayed. Afterwards, the first build
     * is deleted and Jenkins is restarted. Then the results of the second build are validated again: the detail
     * pages should then show the same results (see JENKINS-24940).
     */
    @Test @Issue("24940")
    public void should_report_new_and_fixed_warnings_in_consecutive_builds() {
        assumeTrue("This test requires a restartable Jenkins", jenkins.canRestart());

        FreeStyleJob job = createFreeStyleJob();
        Build firstBuild = buildJobAndWait(job);
        editJob("/findbugs_plugin/forSecondRun/findbugsXml.xml", false, job,
                FindBugsFreestyleSettings.class);

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
     * Builds a freestyle project and checks if new warning are displayed.
     */
    @Test
    public void should_link_to_source_code_in_real_project() {
        FreeStyleJob job = createJob(jenkins, "/findbugs_plugin/sample_findbugs_project", FreeStyleJob.class,
                FindBugsFreestyleSettings.class,
                settings -> settings.pattern.set("target/findbugsXml.xml"));
        setMavenGoal(job, "clean package findbugs:findbugs");

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
        MavenModuleSet job = createMavenJob(settings -> settings.setBuildUnstableTotalAll("0"));

        buildJobAndWait(job).shouldBeUnstable();
    }

    /**
     * Builds a maven project and checks if it failed.
     */
    @Test
    public void should_set_result_to_failed_if_warning_found() {
        MavenModuleSet job = createMavenJob(settings -> settings.setBuildFailedTotalAll("0"));

        buildJobAndWait(job).shouldFail();
    }

    private void assertThatFindBugsResultExists(final Job job, final PageObject build) {
        String actionName = "FindBugs Warnings";
        assertThat(job, hasAction(actionName));
        assertThat(job.getLastBuild(), hasAction(actionName));
        assertThat(build, hasAction(actionName));
    }

    @Override
    protected FindBugsAction createProjectAction(final Job job) {
        return new FindBugsAction(job);
    }

    @Override
    protected FindBugsAction createResultAction(final Build build) {
        return new FindBugsAction(build);
    }

    @Override
    protected FreeStyleJob createFreeStyleJob(final Container owner) {
        return createFreeStyleJob(owner, settings -> settings.pattern.set(PATTERN_WITH_6_WARNINGS));
    }

    @Override
    protected WorkflowJob createPipeline() {
        return createPipelineWith(FILE_WITH_6_WARNINGS, "FindBugsPublisher");
    }

    @Override
    protected int getNumberOfWarnings() {
        return TOTAL_NUMBER_OF_WARNINGS;
    }

    @Override
    protected int getNumberOfHighPriorityWarnings() {
        return 2;
    }

    @Override
    protected int getNumberOfNormalPriorityWarnings() {
        return 4;
    }

    @Override
    protected int getNumberOfLowPriorityWarnings() {
        return 0;
    }

    private FreeStyleJob createFreeStyleJob() {
        return createFreeStyleJob(jenkins);
    }

    private FreeStyleJob createFreeStyleJob(final Container owner,
            final AnalysisConfigurator<FindBugsFreestyleSettings> buildConfigurator) {
        return createFreeStyleJob(FILE_WITH_6_WARNINGS, owner, buildConfigurator);
    }

    private FreeStyleJob createFreeStyleJob(final String file, final Container owner,
            final AnalysisConfigurator<FindBugsFreestyleSettings> buildConfigurator) {
        return createJob(owner, file, FreeStyleJob.class, FindBugsFreestyleSettings.class, buildConfigurator);
    }

    private MavenModuleSet createMavenJob() {
        return createMavenJob(new NullConfigurator<>());
    }

    private MavenModuleSet createMavenJob(AnalysisConfigurator<FindBugsMavenSettings> configurator) {
        return createMavenJob("/findbugs_plugin/sample_findbugs_project",
                "clean package findbugs:findbugs", FindBugsMavenSettings.class, configurator);
    }

    @Override
    protected void assertThatDetailsAreFilled(final FindBugsAction action) {
        assertXmlApiMatchesExpected(action.getBuild(), "findbugsResult/api/xml?depth=0",
                PLUGIN_ROOT + "api_depth_0-2_x.xml", false);

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
}
