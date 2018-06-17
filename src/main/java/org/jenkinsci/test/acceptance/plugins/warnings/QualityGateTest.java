package org.jenkinsci.test.acceptance.plugins.warnings;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
/**
 * Tests if the qualitygate functionality is working.
 *
 * @author Michaela Reitschuster
 */
@WithPlugins("warnings")
public class QualityGateTest extends AbstractJUnitTest {


    /**
     * Simple test to check that the console log shows that Build was a failure when thresholds of qualitygate have been
     * reached.
     */
    @Test
    public void shouldLogFailureInConsoleWhenQualityGateThresholdWasReached() {
       Build build = setUpBuildWithQualityGateConfiguration();
       assertThat(build.getConsole()).contains("Finished: FAILURE");
    }


    private Build setUpBuildWithQualityGateConfiguration(){
        FreeStyleJob job = jenkins.getJobs().create(FreeStyleJob.class);
        job.copyResource("/warnings_plugin/checkstyle-result.xml");
        IssuesRecorder recorder = job.addPublisher(IssuesRecorder.class);
        recorder.setTool("CheckStyle");
        recorder.openAdvancedOptions();
        recorder.setEnabledForFailure(true);
        recorder.addQualityGateConfiguration();
        job.save();

        return job.startBuild().waitUntilFinished();
    }

}
