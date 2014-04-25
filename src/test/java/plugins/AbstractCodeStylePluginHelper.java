package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginPostBuildStep;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;

public abstract class AbstractCodeStylePluginHelper extends AbstractJUnitTest {

    /**
     * Setup a job with the given resource and publisher.
     * @param resourceToCopy Resource to copy to to build
     * @param publisher Publisher to add
     * @param publisherPattern Publisher pattern to set
     * @param <T> Type of the publisher
     * @return The made job
     */
    public <T extends AbstractCodeStylePluginPostBuildStep> FreeStyleJob setupJob(String resourceToCopy, Class<T> publisher, String publisherPattern) {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.copyResource(resource(resourceToCopy));
        job.addPublisher(publisher).pattern.set(publisherPattern);
        job.save();
        return job;
    }

    /**
     * Edits a job with the given resource and publisherPattern
     * @param job Job to edit
     * @param newResourceToCopy Second resource to copy to differ the result
     * @param publisherPattern Publisher pattern to set
     * @param <T> Type of the publisher
     * @return The made job
     */
    public <T extends AbstractCodeStylePluginPostBuildStep> FreeStyleJob editJobAndDelLastResource(FreeStyleJob job, String newResourceToCopy, String publisherPattern) {
        job.configure();
        job.removeFirstBuildStep();
        job.copyResource(resource(newResourceToCopy), publisherPattern);
        job.save();
        return job;
    }

    /**
     *  Build Job and wait until finished.
     *  @param job Job to build
     *  @return The made build
     */
    public Build buildJobAndWait(FreeStyleJob job) {
        return job.queueBuild().waitUntilFinished();
    }

    /**
     *  Build Job successfully once.
     *  @param job Job to build
     *  @return The made build
     */
    public Build buildJobWithSuccess(FreeStyleJob job) {
        return buildJobAndWait(job).shouldSucceed();
    }
}