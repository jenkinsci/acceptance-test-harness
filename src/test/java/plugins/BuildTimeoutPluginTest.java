package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.build_timeout.BuildTimeout;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

@WithPlugins("build-timeout")
public class BuildTimeoutPluginTest extends AbstractJUnitTest {
    @Test
    public void fail_build_with_absolute_time() {
        FreeStyleJob j = jenkins.jobs.create();
        j.configure();
        {
            j.addShellStep("sleep 300");

            BuildTimeout t = new BuildTimeout(j);
            t.abortAfter(3);
            t.writeDescription();
        }
        j.save();

        j.startBuild().waitUntilFinished(300).shouldAbort();
    }

    @Test
    public void fail_build_if_likely_stuck() {
        FreeStyleJob j = jenkins.jobs.create();
        j.configure();
        j.addShellStep("sleep 1");
        j.save();

        for (int i = 0; i < 3; i++) {
            j.startBuild().shouldSucceed();
        }

        j.configure();
        {
            j.addShellStep("sleep 300");

            BuildTimeout t = new BuildTimeout(j);
            t.abortWhenStuck();
            t.writeDescription();
        }
        j.save();

        j.startBuild().waitUntilFinished(300).shouldAbort();
    }
}
