package groovy.plugins

import org.jenkinsci.test.acceptance.geb.GebSpec
import org.jenkinsci.test.acceptance.junit.WithPlugins
import org.jenkinsci.test.acceptance.plugins.jacoco.JacocoPublisher
import org.jenkinsci.test.acceptance.po.Job

/**
 *
 * @author christian.fritz
 */
@WithPlugins("jacoco")
class JacocoPluginTest extends GebSpec {
    private Job j;

    def "Check successfully execution"() {
        given: "I have a job job with jacoco results in it"
        j = jenkins.jobs.create();
        j.copyDir(resource("/jacoco/test"));
        j.addPublisher(JacocoPublisher.class);
        //changeBuildStatus.checked=true;
    }
}
