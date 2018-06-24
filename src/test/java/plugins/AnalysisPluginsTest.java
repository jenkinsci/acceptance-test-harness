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
 * @author Michaela Reitschuster
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
