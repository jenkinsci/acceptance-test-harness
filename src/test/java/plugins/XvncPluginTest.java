package plugins;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.xvnc.XvncGlobalJobConfig;
import org.jenkinsci.test.acceptance.plugins.xvnc.XvncJobConfig;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.XvncSlaveContainer;
import static org.jenkinsci.test.acceptance.plugins.xvnc.XvncJobConfig.*;
import org.jenkinsci.test.acceptance.po.Slave;

@WithPlugins("xvnc")
public class XvncPluginTest extends AbstractJUnitTest {
    FreeStyleJob job;

    @Inject DockerContainerHolder<XvncSlaveContainer> containerHolder;

    @Before
    public void setUp() {
        Slave slave = containerHolder.get().connect(jenkins);
        slave.setLabels("xvnc");
        slave.save();
        job = jenkins.jobs.create(FreeStyleJob.class);
        job.configure();
        job.setLabelExpression("xvnc");
        job.save();
    }

    @Test
    public void run_xvnc_during_the_build() {
        job.configure();
        new XvncJobConfig(job).useXvnc();
        job.save();

        Build build = job.startBuild().shouldSucceed();
        assertThat(build, runXvnc());
    }

    @Test
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
        new XvncGlobalJobConfig(jenkins.getConfigPage())
                .useDisplayNumber(42)
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
