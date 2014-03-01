import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.junit.Test;

import javax.inject.Inject;
import java.util.concurrent.Callable;

/**
 * Plugin test for Ant.
 *
 * Also acting as an example for writing tests in plain-old JUnit.
 *
 * @author Kohsuke Kawaguchi
 */
public class AntPluginTest extends AbstractJUnitTest {
    @Inject
    Jenkins jenkins;

    @Test
    public void allow_user_to_use_Ant_in_freestyle_project() throws Exception {
        jenkins.getPluginManager().installPlugin("ant");
        final FreeStyleJob j = jenkins.createJob(FreeStyleJob.class);
        j.configure(new Callable<Object>() {
            public Object call() throws Exception {
                j.addCreateFileStep("build.xml",resource("echo-helloworld.xml").asText());
                j.addBuildStep(AntBuildStep.class).setTarget("hello");
                return null;
            }
        });

        j.queueBuild().shouldSucceed();
    }
}
