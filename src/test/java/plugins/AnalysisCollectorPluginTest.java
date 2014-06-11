package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginPostBuildStep;
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

import static org.jenkinsci.test.acceptance.Matchers.hasAction;
import static org.junit.Assert.assertThat;

/**
 * Feature: Tests for Static Code Analysis Collector (analysis-collector)
 *
 * @author Michael Prankl
 */
@WithPlugins({"analysis-collector", "checkstyle", "pmd", "findbugs", "tasks"})
public class AnalysisCollectorPluginTest extends AbstractJUnitTest {

    @Test
    public void test() {
        FreeStyleJob job = setupJob("/analysis_collector_plugin", publishers());
        Build lastBuild = job.startBuild();
        assertThat(job, hasAction("Static Analysis Warnings"));
        assertThat(lastBuild, hasAction("Static Analysis Warnings"));
        // TODO Resources for job with FindBugs, Checkstyle, PMD, Tasks, Dry
        // TODO PO for Static Analysis Results
    }

    private <T extends AbstractCodeStylePluginPostBuildStep> List<Class<T>> publishers() {
        final List<Class<T>> publishers = new ArrayList<>();
        publishers.add((Class<T>) CheckstylePublisher.class);
        publishers.add((Class<T>) PmdPublisher.class);
        publishers.add((Class<T>) FindbugsPublisher.class);
        publishers.add((Class<T>) TaskScannerPublisher.class);
        publishers.add((Class<T>) AnalysisCollectorPublisher.class);
        return publishers;
    }

    /**
     * Setup a job with given resources and publishers.
     *
     * @param resourceToCopy   Resource to copy to build (Directory or File path)
     * @param publisherClasses Publishers to add
     * @param <T>              Type of the publisher
     * @return the made job
     */
    public <T extends AbstractCodeStylePluginPostBuildStep> FreeStyleJob setupJob(String resourceToCopy, List<Class<T>> publisherClasses) {
        final FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        copyResources(resourceToCopy, job);
        for (Class<T> publisher : publisherClasses) {
            job.addPublisher(publisher);
        }
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
