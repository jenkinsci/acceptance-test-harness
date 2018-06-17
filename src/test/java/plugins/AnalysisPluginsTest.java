package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckStyleFreestyleSettings;
import org.jenkinsci.test.acceptance.plugins.warnings.IssuesRecorder;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
     * Simple test to check that warnings of checkstyle and pmd file are handled separately if aggregation is not activated.
     */
    @Test
    public void should_log_ok_in_console_with_not_activated_aggregation() {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);

        job.copyResource(resource("/aggregation/checkstyle.xml"));
        job.copyResource(resource("/aggregation/pmd-warnings.xml"));

        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        recorder.setTool("CheckStyle","**/checkstyle.xml");
        recorder.addTool("PMD", "**/pmd-warnings.xml");

        recorder.setEnabledForAggregation(false);

        job.save();

        Build build = job.startBuild().waitUntilFinished();

        assertThat(build.getConsole()).contains("[CheckStyle] Resolved module names for 6 issues\n");
        assertThat(build.getConsole()).contains("[pmd] Resolved module names for 4 issues\n");
    }

    /**
     * Simple test to check that warnings of checkstyle and pmd file are summed up if aggregation is activated.
     */
    @Test
    public void should_log_ok_in_console_with_activated_aggregation() {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);

        job.copyResource(resource("/aggregation/checkstyle.xml"));
        job.copyResource(resource("/aggregation/pmd-warnings.xml"));

        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        recorder.setTool("CheckStyle","**/checkstyle.xml");
        recorder.addTool("PMD", "**/pmd-warnings.xml");

        recorder.setEnabledForAggregation(true);

        job.save();

        Build build = job.startBuild().waitUntilFinished();

        assertThat(build.getConsole()).contains("[analysis] Created analysis result for 10 issues (found 10 new issues, fixed 0 issues)\n");
    }
}
