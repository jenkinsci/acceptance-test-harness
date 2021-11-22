package plugins;

import com.google.inject.Inject;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.job_config_history.JobConfigHistory;
import org.jenkinsci.test.acceptance.plugins.priority_sorter.PriorityConfig;
import org.jenkinsci.test.acceptance.plugins.priority_sorter.PriorityConfig.Group;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.ListView;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.jenkinsci.test.acceptance.po.Slave.runBuildsInOrder;

import static org.junit.Assume.assumeTrue;

/**
 * @author Kohsuke Kawaguchi
 */
@WithPlugins("PrioritySorter")
public class PrioritySorterPluginTest extends AbstractJUnitTest {
    private static final String LABEL = "slave";

    @Inject
    private SlaveController slaves;

    private Slave slave;

    @Before
    public void setUp() throws Exception {
//        assumeTrue("This test requires a restartable Jenkins", jenkins.canRestart());
//        jenkins.restart(); // Priority sorter plugin needs this
        slave = slaves.install(jenkins).get();
    }

    @Test
    public void match_jobs_by_name() {
        PriorityConfig priority = jenkins.action(PriorityConfig.class);
        priority.configure();
        Group low = priority.addGroup();
        low.priority.select("5");
        low.pattern("low_priority");
        Group high = priority.addGroup();
        high.priority.select("1");
        high.pattern("high_priority");
        priority.save();

        FreeStyleJob lowPriority = jenkins.jobs.create(FreeStyleJob.class, "low_priority");
        tieToLabel(lowPriority, LABEL);
        Build plBuild = lowPriority.scheduleBuild();

        FreeStyleJob highPriority = jenkins.jobs.create(FreeStyleJob.class, "high_priority");
        tieToLabel(highPriority, LABEL);
        Build hpBuild = highPriority.scheduleBuild();

        slave.configure();
        slave.setLabels(LABEL);
        slave.save();

        hpBuild.shouldSucceed();
        plBuild.shouldSucceed();

        assertThat(slave, runBuildsInOrder(highPriority, lowPriority));
    }

    @Test
    public void match_jobs_by_view() {
        FreeStyleJob p2 = jenkins.views.create(ListView.class, "normal").jobs.create(FreeStyleJob.class, "P2");
        tieToLabel(p2, LABEL);

        FreeStyleJob p1 = jenkins.views.create(ListView.class, "prioritized").jobs.create(FreeStyleJob.class, "P1");
        tieToLabel(p1, LABEL);

        PriorityConfig priority = jenkins.action(PriorityConfig.class);
        priority.configure();
        final Group low = priority.addGroup();
        low.priority.select("5");
        low.byView("normal");
        final Group high = priority.addGroup();
        high.priority.select("1");
        high.byView("prioritized");
        priority.save();

        Build p2b = p2.scheduleBuild();
        Build p1b = p1.scheduleBuild();

        // Set label after scheduling build so the test is deterministic
        slave.configure();
        slave.setLabels(LABEL);
        slave.save();

        p1b.shouldSucceed();
        p2b.shouldSucceed();

        assertThat(slave, runBuildsInOrder(p1, p2));
    }

    // Reproduce regression fixed in https://github.com/jenkinsci/priority-sorter-plugin/commit/e46b2b1fbc4396f441c69692eb328fb982325572
    @Test @WithPlugins("jobConfigHistory")
    public void saving_global_config_should_not_create_job_change() {
        FreeStyleJob job = jenkins.jobs.create();
        job.save();

        JobConfigHistory action = job.action(JobConfigHistory.class);
        final int expected = action.getChanges().size();

        final JenkinsConfig global = jenkins.getConfigPage();
        global.configure();
        global.numExecutors.set(42);
        global.save();

        assertThat(action.getChanges().size(), equalTo(expected));
    }

    private void tieToLabel(FreeStyleJob job, String label) {
        job.configure();
        job.setLabelExpression(label);
        job.save();
    }
}
