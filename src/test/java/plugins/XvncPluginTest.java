package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.xvnc.XvncGlobalJobConfig;
import org.jenkinsci.test.acceptance.plugins.xvnc.XvncJobConfig;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.plugins.xvnc.XvncJobConfig.*;

@WithPlugins("xvnc")
public class XvncPluginTest extends AbstractJUnitTest {
    FreeStyleJob job;

    @Before
    public void setUp() {
        job = jenkins.jobs.create(FreeStyleJob.class);
    }

    @Test
    @Native("vncserver")
    public void run_xvnc_during_the_build() {
        job.configure();
        new XvncJobConfig(job).useXvnc();
        job.save();

        Build build = job.startBuild().shouldSucceed();
        assertThat(build, runXvnc());
    }

    @Test
    @Native({"vncserver", "import"})
    public void take_screenshot_at_the_end_of_the_build() {
        job.configure();
        new XvncJobConfig(job).useXvnc().takeScreenshot();
        job.save();

        Build build = job.startBuild().shouldSucceed();
        assertThat(build, runXvnc());
        assertThat(build, tookScreenshot());
        build.getArtifact("screenshot.jpg").assertThatExists(true);
    }

    @Test
    public void use_specific_display_number() {
        jenkins.configure();
        // Do not actually run vnc as DISPLAY_NUMBER can collide with accupied one.
        new XvncGlobalJobConfig(jenkins.getConfigPage())
                .useDisplayNumber(42)
                .command("echo 'Fake vncserver on :$DISPLAY_NUMBER' display")
        ;
        jenkins.save();

        job.configure();
        new XvncJobConfig(job).useXvnc();
        job.save();

        Build build = job.startBuild().shouldSucceed();
        assertThat(build, runXvnc());
        assertThat(build, usedDisplayNumber(42));
    }
}
