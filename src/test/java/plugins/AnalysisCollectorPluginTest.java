package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginPostBuildStep;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisCollectorAction;
import org.jenkinsci.test.acceptance.plugins.analysis_collector.AnalysisCollectorPublisher;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckstylePublisher;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindbugsPublisher;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdPublisher;
import org.jenkinsci.test.acceptance.plugins.tasks.TaskScannerPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.jenkinsci.test.acceptance.Matchers.hasAction;
import static org.junit.Assert.assertThat;

/**
 * Feature: Tests for Static Code Analysis Collector (analysis-collector)
 *
 * @author Michael Prankl
 */
@WithPlugins({"analysis-collector", "checkstyle", "pmd", "findbugs", "tasks"})
public class AnalysisCollectorPluginTest extends AbstractJUnitTest {

    /**
     * Scenario: First build with new warnings
     * Given I have job with artifacts of static analysis tools
     * And this artifacts are published by their corresponding plugins
     * When I add a post-build step to publish a combined static analyis result
     * Then the job and build will have a action "Static Analysis Warning"
     * And this action will show the combined static analysis result
     */
    @Test
    public void first_build_new_warnings() {
        FreeStyleJob job = setupJob("/analysis_collector_plugin");
        Build lastBuild = job.startBuild().waitUntilFinished();
        assertThat(job, hasAction("Static Analysis Warnings"));
        assertThat(lastBuild, hasAction("Static Analysis Warnings"));
        AnalysisCollectorAction result = new AnalysisCollectorAction(job);
        assertThat(result.getWarningNumber(), is(795));
        assertThat(result.getHighWarningNumber(), is(779));
        assertThat(result.getNormalWarningNumber(), is(9));
        assertThat(result.getLowWarningNumber(), is(7));
        assertThat(result.getNewWarningNumber(), is(795));
    }

    /**
     * Setup a job with given resources and needed publishers.
     *
     * @param resourceToCopy Resource to copy to build (Directory or File path)
     * @return the made job
     */
    public FreeStyleJob setupJob(String resourceToCopy) {
        final FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        copyResources(resourceToCopy, job);
        job.addPublisher(CheckstylePublisher.class);
        job.addPublisher(PmdPublisher.class);
        job.addPublisher(FindbugsPublisher.class);
        TaskScannerPublisher taskScannerPublisher = job.addPublisher(TaskScannerPublisher.class);
        taskScannerPublisher.highPriorityTags.sendKeys("PRIO1");
        taskScannerPublisher.normalPriorityTags.sendKeys("PRIO2,TODO");
        taskScannerPublisher.lowPriorityTags.sendKeys("PRIO3");
        job.addPublisher(AnalysisCollectorPublisher.class);
        job.save();
        return job;
    }

    private void copyResources(String resourceToCopy, FreeStyleJob job) {
        final Resource res = resource(resourceToCopy);
        //decide whether to utilize copyResource or copyDir
        if (res.asFile().isDirectory())
            job.copyDir(res);
        else
            job.copyResource(res);
    }
}
