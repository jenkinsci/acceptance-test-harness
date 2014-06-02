package groovy.plugins

import org.jenkinsci.test.acceptance.geb.GebSpec
import org.jenkinsci.test.acceptance.junit.WithPlugins
import org.jenkinsci.test.acceptance.plugins.jacoco.JacocoPublisher
import org.jenkinsci.test.acceptance.po.Job

/**
 * Checks the successfully execution of jacoco coverage reports.
 *
 * @author christian.fritz
 */
@WithPlugins("jacoco")
class JacocoPluginTest extends GebSpec {
    private Job job;

    def "Check successfully execution"() {
        given: "A job with jacoco results"
        job = jenkins.jobs.create();
        job.copyDir(resource("/jacoco/test"));
        def publisher = job.addPublisher(JacocoPublisher.class);
        publisher.changeBuildStatus.value(true);
        job.save();

        when: "the build started and succeed"
        job.startBuild().waitUntilFinished().shouldSucceed();

        then: "jacoco appears within the navigation area."
        assert job.navigationLinks.containsValue("Coverage Trend")
    }
}
