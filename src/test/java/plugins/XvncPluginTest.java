package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import org.jenkinsci.test.acceptance.docker.fixtures.XvncSlaveContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.xvnc.XvncGlobalJobConfig;
import org.jenkinsci.test.acceptance.plugins.xvnc.XvncJobConfig;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.jvnet.hudson.test.Issue;

@WithPlugins({"xvnc", "ssh-slaves"})
@Category(DockerTest.class)
@WithDocker
public class XvncPluginTest extends AbstractJUnitTest {

    private XvncSlaveContainer container;

    @Before
    public void setUp() {
        container = new XvncSlaveContainer();

        container.start();
        Slave slave = container.connect(jenkins);
        slave.setLabels("xvnc");
        slave.save();
    }

    @After
    public void tearDown() {
        if (container != null) {
            container.stop();
        }
    }

    private FreeStyleJob createJob() {
        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class);
        job.configure();
        job.setLabelExpression("xvnc");
        return job;
    }

    @Test
    public void run_xvnc_during_the_build() {
        FreeStyleJob job = createJob();
        new XvncJobConfig(job).useXvnc().takeScreenshot();
        job.save();

        Build build = job.startBuild().shouldSucceed();
        assertThat(build, XvncJobConfig.runXvnc());
        assertThat(build, XvncJobConfig.tookScreenshot());
        build.getArtifact("screenshot.jpg").assertThatExists(true);
    }

    @Test
    public void use_specific_display_number() {
        jenkins.configure();
        new XvncGlobalJobConfig(jenkins.getConfigPage()).useDisplayNumber(42);
        jenkins.save();

        FreeStyleJob job = createJob();
        new XvncJobConfig(job).useXvnc();
        job.save();

        Build build = job.startBuild().shouldSucceed();
        assertThat(build, XvncJobConfig.runXvnc());
        assertThat(build, XvncJobConfig.usedDisplayNumber(42));
    }

    @WithPlugins({"xvnc", "workflow-aggregator"})
    @Issue("JENKINS-26477")
    @Test
    public void workflow() {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(
                "node('xvnc') {wrap([$class: 'Xvnc', takeScreenshot: true, useXauthority: true]) {sh 'xmessage hello &'}}");
        job.sandbox.check();
        job.save();
        Build build = job.startBuild().shouldSucceed();
        assertThat(build.getConsole(), containsString("+ xmessage hello"));
        assertThat(build, XvncJobConfig.runXvnc());
        assertThat(build, XvncJobConfig.tookScreenshot());
        build.getArtifact("screenshot.jpg").assertThatExists(true); // TODO should this be moved into tookScreenshot?
    }
}
