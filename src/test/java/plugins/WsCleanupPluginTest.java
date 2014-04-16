package plugins;

import static org.jenkinsci.test.acceptance.po.Workspace.workspaceContains;
import static org.hamcrest.CoreMatchers.not;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.ws_cleanup.WsCleanup;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Before;
import org.junit.Test;

@WithPlugins("ws-cleanup")
public class WsCleanupPluginTest extends AbstractJUnitTest {
    FreeStyleJob job;

    @Before
    public void setUp() {
        job = jenkins.jobs.create(FreeStyleJob.class);
    }

    @Test
    public void do_not_clean_by_default() {
        job.configure();
        job.addShellStep("touch ws_file");
        job.save();

        job.queueBuild().shouldSucceed();
        assertThat(job, workspaceContains("ws_file"));
    }

    @Test
    public void clean_up_after_build() {
        job.configure();
        job.addShellStep("touch ws_file");
        job.addPublisher(WsCleanup.PostBuild.class);
        job.save();

        job.queueBuild().shouldSucceed();
        assertThat(job, not(workspaceContains("ws_file")));
    }

    @Test
    public void clean_up_before_build() {
        job.configure();
        job.addShellStep("mkdir ws_dir");
        new WsCleanup.PreBuild(job);
        job.save();

        job.queueBuild().shouldSucceed();
        job.queueBuild().shouldSucceed();

        assertThat(job, workspaceContains("ws_dir"));
    }
}
