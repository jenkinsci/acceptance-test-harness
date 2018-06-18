package plugins;

import java.util.HashMap;
import java.util.List;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.warnings.IssuesRecorder;
import org.jenkinsci.test.acceptance.plugins.warnings.IssuesRecorder.StaticAnalysisTool;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsResultDetailsPage;
import org.jenkinsci.test.acceptance.plugins.warnings.WarningsResultDetailsPage.tabs;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import static org.assertj.core.api.Assertions.*;

/**
 * Acceptance tests for the White Mountains release of the warnings plug-in.
 *
 * @author Ullrich Hafner
 */
@WithPlugins("warnings")
public class AnalysisPluginsTest extends AbstractJUnitTest {
    /**
     * Simple test to check that the console log shows an error message if no report file has been found.
     */
    @Test
    public void should_log_error_in_console_if_no_report_file_found() {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);

        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        recorder.setTool("CheckStyle");
        recorder.openAdvancedOptions();
        recorder.setEnabledForFailure(true);
        job.save();

        Build build = job.startBuild().waitUntilFinished();

        assertThat(build.getConsole()).contains("[CheckStyle] [ERROR] No files found for pattern '**/checkstyle-result.xml'. Configuration error?\n");
    }

    /**
     * Simple test to check that there are some duplicate code warnings.
     */
    @Test
    public void should_have_duplicate_code_warnings(){
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        job.copyResource(resource("/warnings_plugin/duplicate_code/cpd.xml"));
        job.copyResource(resource("/warnings_plugin/duplicate_code/Main.java"));

        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        StaticAnalysisTool tool = recorder.setTool("CPD");
        tool.setNormalThreshold(1);
        tool.setHighThreshold(2);
        job.save();

        Build build = job.startBuild().waitUntilFinished();

        WarningsResultDetailsPage page = getWarningsResultDetailsPage("cpd", build);
        page.openTab(tabs.Details);
        List<HashMap<String, WebElement>> issuesTable = page.getIssuesTable();
        HashMap<String, WebElement> firstRowOfIssuesTable = issuesTable.get(0);
        assertThat(firstRowOfIssuesTable.keySet()).contains("Details");
        //build.openStatusPage();
        //assertThat(build.getConsole()).contains("found 20 issues (skipped 0 duplicates)");
    }


    private WarningsResultDetailsPage getWarningsResultDetailsPage(final String plugin,final Build build){
        WarningsResultDetailsPage resultPage = new WarningsResultDetailsPage(build, plugin);
        resultPage.open();
        return resultPage;
    }

}
