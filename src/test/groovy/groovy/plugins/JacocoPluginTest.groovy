package groovy.plugins

import org.jenkinsci.test.acceptance.geb.GebSpec
import org.jenkinsci.test.acceptance.junit.SmokeTest
import org.jenkinsci.test.acceptance.junit.WithPlugins
import org.junit.experimental.categories.Category
import org.jenkinsci.test.acceptance.plugins.jacoco.JacocoPublisher
import org.jenkinsci.test.acceptance.plugins.jacoco.JacocoResultPage
import org.jenkinsci.test.acceptance.po.Job

/**
 * Checks the successfully execution of jacoco coverage reports.
 *
 * @author christian.fritz
 */
@WithPlugins("jacoco")
class JacocoPluginTest extends GebSpec {
    private Job job;

    @Category(SmokeTest.class)
    def "Check successfully execution and summary"() {
        given: "A job with jacoco results"
        job = jenkins.jobs.create();
        job.copyDir(resource("/jacoco/test"));
        def publisher = job.addPublisher(JacocoPublisher.class);
        publisher.changeBuildStatus.value(true);
        job.save();

        when: "the build started and succeed"
        def build = job.startBuild().waitUntilFinished().shouldSucceed();

        then: "jacoco appears within the navigation area."
        assert build.navigationLinks.containsValue("Coverage Report")
        to JacocoResultPage, build
        assert summaryCoverage.instruction == 45.45
        assert summaryCoverage.branch == 50.0
        assert summaryCoverage.complexity == 33.33
        assert summaryCoverage.lines == 50.0
        assert summaryCoverage.methods == 50.0
        assert summaryCoverage.classes == 100.0

        to JacocoResultPage, build, "(default)"
        assert breakdownCoverageLine."App".instruction == 45.45
        assert breakdownCoverageLine."App".branch == 50.0
        assert breakdownCoverageLine."App".complexity == 33.33
        assert breakdownCoverageLine."App".lines == 50.0
        assert breakdownCoverageLine."App".methods == 50.0
        assert breakdownCoverageLine."App".classes == 100.0
    }
}
