package plugins;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.warnings.IssuesRecorder;
import org.jenkinsci.test.acceptance.plugins.warnings.IssuesRecorder.StaticAnalysisTool;
import org.jenkinsci.test.acceptance.plugins.warnings.StatusPage;
import org.jenkinsci.test.acceptance.plugins.warnings.SummaryBoxPageAreaAssert;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsResultDetailsPage;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsResultDetailsPage.Tabs;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.assertj.core.api.Assertions.*;

/**
 * Acceptance tests for the White Mountains release of the warnings plug-in.
 *
 * @author Ullrich Hafner
 * @author Michaela Reitschuster
 * @author Alexandra Wenzel
 * @author Manuel Hampp
 * @author Anna-Maria Hardi
 * @author Stephan Plöderl
 */
@WithPlugins("warnings")
public class AnalysisPluginsTest extends AbstractJUnitTest {
    private static final String WARNINGS_PLUGIN_PREFIX = "/warnings_plugin/white-mountains/";

    /**
     * Simple test to check that there are some duplicate code warnings.
     */
    @Test
    public void should_have_duplicate_code_warnings() {
        FreeStyleJob job = createFreeStyleJob("duplicate_code/cpd.xml", "duplicate_code/Main.java");

        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        StaticAnalysisTool tool = recorder.setTool("CPD");
        tool.setNormalThreshold(1);
        tool.setHighThreshold(2);
        job.save();

        Build build = job.startBuild().waitUntilFinished();

        WarningsResultDetailsPage page = getWarningsResultDetailsPage("cpd", build);
        page.openTab(Tabs.DETAILS);
        List<Map<String, WebElement>> issuesTable = page.getIssuesTable();
        Map<String, WebElement> firstRowOfIssuesTable = issuesTable.get(0);
        assertThat(firstRowOfIssuesTable.keySet()).contains("Details");
    }

    private WarningsResultDetailsPage getWarningsResultDetailsPage(final String id, final Build build) {
        WarningsResultDetailsPage resultPage = new WarningsResultDetailsPage(build, id);
        resultPage.open();
        return resultPage;
    }

    /**
     * Simple test to check that warnings of checkstyle and pmd file are handled separately if aggregation is not
     * activated.
     */
    @Test
    public void should_log_ok_in_console_with_not_activated_aggregation() {
        FreeStyleJob job = createFreeStyleJob("aggregation/checkstyle.xml", "aggregation/pmd.xml");

        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle", "**/checkstyle.xml");
            recorder.addTool("PMD", "**/pmd.xml");
            recorder.setEnabledForAggregation(false);
        });

        job.save();

        Build build = job.startBuild().waitUntilFinished();

        assertThat(build.getConsole()).contains("[CheckStyle] Created analysis result for 6 issues");
        assertThat(build.getConsole()).contains("[PMD] Created analysis result for 4 issues");
    }

    /**
     * Simple test to check that warnings of checkstyle and pmd file are summed up if aggregation is activated.
     */
    @Test
    public void should_log_ok_in_console_with_activated_aggregation() {
        FreeStyleJob job = createFreeStyleJob("aggregation/checkstyle.xml", "aggregation/pmd.xml");

        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle", "**/checkstyle.xml");
            recorder.addTool("PMD", "**/pmd.xml");
            recorder.setEnabledForAggregation(true);
        });

        job.save();

        Build build = job.startBuild().waitUntilFinished();

        assertThat(build.getConsole()).contains("[Static Analysis] Created analysis result for 10 issues");
    }

    private FreeStyleJob createFreeStyleJob(final String... resourcesToCopy) {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        for (String resource : resourcesToCopy) {
            job.copyResource(resource(WARNINGS_PLUGIN_PREFIX + resource));
        }
        return job;
    }

    /**
     * Test to check that the issue filter can be configured and is applied.
     */
    @Test
    public void should_log_filter_applied_in_console() {
        FreeStyleJob job = createFreeStyleJob("issue_filter/checkstyle-result.xml");

        job.addPublisher(IssuesRecorder.class, recorder -> {
            recorder.setTool("CheckStyle");
            recorder.openAdvancedOptions();
            recorder.setEnabledForFailure(true);
            recorder.addIssueFilter("Exclude categories", "Checks");
            recorder.addIssueFilter("Include types", "JavadocMethodCheck");
        });

        job.save();

        Build build = job.startBuild().waitUntilFinished();

        assertThat(build.getConsole()).contains(
                "[CheckStyle] Applying 2 filters on the set of 4 issues (3 issues have been removed)");
    }

    /**
     * Test to check that the issue filter can be configured and is applied.
     */
    @Test
    public void should_show_correct_plugin_result_boxes() {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        job.copyResource(WARNINGS_PLUGIN_PREFIX + "build_status_test/build_01");
        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);

        recorder.setTool("CheckStyle");
        recorder.addTool("FindBugs");
        recorder.addTool("PMD");
        recorder.openAdvancedOptions();
        recorder.setEnabledForFailure(true);

        job.save();

        Build build1 = job.startBuild().waitUntilFinished();
        visit(job.getConfigUrl());
        job.copyResource(WARNINGS_PLUGIN_PREFIX + "build_status_test/build_02");
        job.save();
        Build build2 = job.startBuild().waitUntilFinished();

        visit(build2.url);

        List<String> plugins = Arrays.asList("checkstyle", "pmd", "findbugs");

        StatusPage statusPage = new StatusPage(build2, plugins, false);

        SummaryBoxPageAreaAssert.assertThat(statusPage.getSummaryBoxByName("checkstyle")).hasWarningDiv();
        SummaryBoxPageAreaAssert.assertThat(statusPage.getSummaryBoxByName("pmd")).hasWarningDiv();
        SummaryBoxPageAreaAssert.assertThat(statusPage.getSummaryBoxByName("findbugs")).hasWarningDiv();

        statusPage.getSummaryBoxByName("checkstyle").getTitleDivResultLink().click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build2.url + "checkstyleResult/");

        // TODO: Ask why we have to reinit statusPage object
        visit(build2.url);
        statusPage = new StatusPage(build2, plugins, false);

        statusPage.getSummaryBoxByName("checkstyle").getTitleDivResultInfoLink().click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build2.url + "checkstyleResult/info/");

        visit(build2.url);
        statusPage = new StatusPage(build2, plugins, false);

        statusPage.getSummaryBoxByName("checkstyle").findClickableResultEntryByNamePart("new").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build2.url + "checkstyleResult/new/");

        visit(build2.url);
        statusPage = new StatusPage(build2, plugins, false);

        statusPage.getSummaryBoxByName("checkstyle").findClickableResultEntryByNamePart("Reference").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build1.url + "checkstyleResult/");

        visit(build2.url);
        statusPage = new StatusPage(build2, plugins, false);

        String noWarningsResult = statusPage.getSummaryBoxByName("findbugs")
                .findResultEntryTextByNamePart("No warnings for");
        assertThat(noWarningsResult).isEqualTo("No warnings for 2 builds, i.e. since build 1");

        visit(build2.url);
        statusPage = new StatusPage(build2, plugins, false);

    }

    /**
     * Test to check that the issue filter can be configured and is applied.
     */
    @Test
    public void should_show_correct_aggregation_result_box() {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        job.copyResource(WARNINGS_PLUGIN_PREFIX + "build_status_test/build_01");
        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);

        recorder.setTool("CheckStyle");
        recorder.addTool("FindBugs");
        recorder.addTool("PMD");
        recorder.openAdvancedOptions();
        recorder.setEnabledForFailure(true);
        recorder.setEnabledForAggregation(true);
        job.save();

        Build build1 = job.startBuild().waitUntilFinished();
        visit(job.getConfigUrl());
        job.copyResource(WARNINGS_PLUGIN_PREFIX + "build_status_test/build_02");
        job.save();

        Build build2 = job.startBuild().waitUntilFinished();

        visit(build2.url);

        List<String> plugins = Arrays.asList("checkstyle", "pmd", "findbugs");

        StatusPage statusPage = new StatusPage(build2, plugins, true);

        SummaryBoxPageAreaAssert.assertThat(statusPage.getSummaryBoxByName("analysis")).hasWarningDiv();
        String resultsFrom = statusPage.getSummaryBoxByName("analysis")
                .findResultEntryTextByNamePart("Static analysis results from");
        // FIXME: Field should also contains findbugs, even if there is no issue...
        //assertThat(resultsFrom.toLowerCase()).contains(plugins);

        statusPage.getSummaryBoxByName("analysis").getTitleDivResultLink().click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build2.url + "analysisResult/");

        visit(build2.url);
        statusPage = new StatusPage(build2, plugins, true);

        statusPage.getSummaryBoxByName("analysis").getTitleDivResultInfoLink().click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build2.url + "analysisResult/info/");

        visit(build2.url);
        statusPage = new StatusPage(build2, plugins, true);

        statusPage.getSummaryBoxByName("analysis").findClickableResultEntryByNamePart("2 new warnings").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build2.url + "analysisResult/new/");

        visit(build2.url);
        statusPage = new StatusPage(build2, plugins, true);

        statusPage.getSummaryBoxByName("analysis").findClickableResultEntryByNamePart("One fixed warning").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build2.url + "analysisResult/fixed/");

        visit(build2.url);
        statusPage = new StatusPage(build2, plugins, true);

        statusPage.getSummaryBoxByName("analysis").findClickableResultEntryByNamePart("Reference build").click();
        assertThat(jenkins.getCurrentUrl()).isEqualTo(build1.url + "analysisResult/");

        visit(build2.url);
        statusPage = new StatusPage(build2, plugins, true);
    }

    /**
     * Test to check that the issue filter can be configured and is applied.
     */
    @Test
    public void should_show_correct_plugin_result_boxes_with_qualitiy_gate() {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        job.copyResource(WARNINGS_PLUGIN_PREFIX + "build_status_test/build_01");
        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);

        recorder.setTool("CheckStyle");
        recorder.addTool("FindBugs");
        recorder.addTool("PMD");
        recorder.openAdvancedOptions();
        recorder.setEnabledForFailure(true);

        //recorder.set
        job.save();

        Build build1 = job.startBuild().waitUntilFinished();
        visit(job.getConfigUrl());
        job.copyResource(WARNINGS_PLUGIN_PREFIX + "build_status_test/build_02");
        job.save();

        Build build2 = job.startBuild().waitUntilFinished();

        visit(build2.url);
    }

    /**
     * Simple test to check that the console log shows that build was a failure when thresholds of qualitygate have been
     * reached.
     */
    @Test
    public void should_log_failure__when_qualitygate_thresholds_are_reached() {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        job.copyResource("/warnings_plugin/checkstyle-result.xml");
        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        recorder.setTool("CheckStyle");
        recorder.openAdvancedOptions();
        recorder.setEnabledForFailure(true);
        recorder.addQualityGateConfiguration(5);
        job.save();

        Build build = job.startBuild().waitUntilFinished();
        assertThat(build.getConsole()).contains("Finished: FAILURE");
    }

}

