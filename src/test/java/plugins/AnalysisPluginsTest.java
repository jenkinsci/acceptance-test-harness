package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
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
     * Test to check that the issue filter can be configured and is applied.
     */
    @Test
    public void should_log_filter_applied_in_console() {
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        job.copyResource("/warnings_plugin/issue_filter_test/checkstyle-result.xml");
        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);

        recorder.setTool("CheckStyle");
        recorder.openAdvancedOptions();
        recorder.setEnabledForFailure(true);

        // set filters
        recorder.addIssueFilter("Exclude categories", "Checks");
        recorder.addIssueFilter("Include types", "JavadocMethodCheck");

        job.save();

        Build build = job.startBuild().waitUntilFinished();

        assertThat(build.getConsole()).contains("[checkstyle] Applying 2 filters on the set of 4 issues (3 issues have been removed)");
    }
}

