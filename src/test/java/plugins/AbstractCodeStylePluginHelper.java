package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.plugins.AbstractCodeStylePluginPostBuildStep;
import org.jenkinsci.test.acceptance.plugins.checkstyle.CheckstylePublisher;
import org.jenkinsci.test.acceptance.plugins.pmd.PmdPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

public abstract class AbstractCodeStylePluginHelper extends AbstractJUnitTest {

    /**
     * Setup a job with the given resource and publisher and build it once.
     * @param resourceToCopy Resource to copy to to build
     * @param publisher Publisher to add
     * @param publisherPattern Publisher pattern to set
     * @param <T> Type of the publisher
     * @return The made job
     */
    public <T extends AbstractCodeStylePluginPostBuildStep> FreeStyleJob setupJobAndRunOnceShouldSucceed(String resourceToCopy, Class<T> publisher, String publisherPattern) {
            FreeStyleJob job = jenkins.jobs.create();
            job.configure();
            job.copyResource(resource(resourceToCopy));
            job.addPublisher(publisher).pattern.set(publisherPattern);
            job.save();
            job.queueBuild().waitUntilFinished().shouldSucceed();
            return job;
    }

    /**
     * Setup a job with the given resource and publisher and build it once.
     * @param resourceToCopy Resource to copy to to build
     * @param publisher Publisher to add
     * @param publisherPattern Publisher pattern to set
     * @param secondResourceToCopy Second resource to copy to differ the result
     * @param <T> Type of the publisher
     * @return The made job
     */
    public <T extends AbstractCodeStylePluginPostBuildStep> FreeStyleJob setupJobAndRunTwiceShouldSucceed(String resourceToCopy, Class<T> publisher, String publisherPattern, String secondResourceToCopy) {
        FreeStyleJob job = setupJobAndRunOnceShouldSucceed(resourceToCopy, publisher, publisherPattern);
        job.getLastBuild().shouldSucceed();
        job.configure();
        job.removeFirstBuildStep();
        job.copyResource(resource(secondResourceToCopy), publisherPattern);
        job.save();
        job.queueBuild().waitUntilFinished().shouldSucceed();
        return job;
    }

}