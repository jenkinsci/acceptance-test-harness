package plugins;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Kohsuke Kawaguchi
 */
@WithPlugins("PrioritySorter")
public class PrioritySorterPluginTest extends AbstractJUnitTest {
    @Inject
    SlaveController slave;

    @Inject
    JenkinsController jenkinsc;

    @Before
    public void setUp() throws Exception {
        jenkinsc.restart();
        slave.install(jenkins).get();
    }

    /**
     Scenario: Match jobs by name
       When I configure absolute sorting strategy with 2 priorities
       And I set priority 2 for job "low_priority"
       And I set priority 1 for job "high_priority"

       And I create a job named "low_priority"
       And I tie the job to the "slave" label
       And I queue a build
       And I create a job named "high_priority"
       And I tie the job to the "slave" label
       And I queue a build

       And I add the label "slave" to the slave
       Then the build should succeed
       And jobs should be executed in order on the slave
           | high_priority | low_priority |
     */
    @Test
    public void match_jobs_by_name() {

    }

    /**
     Scenario: Match jobs by view
       When I configure absolute sorting strategy with 2 priorities

       And I set priority 2 for view "normal"
       And I create a view named "normal"
       And I create job "P2" in the view
       And I tie the job to the "slave" label
       And I queue a build

       And I set priority 1 for view "prioritized"
       And I create a view named "prioritized"
       And I create job "P1" in the view
       And I tie the job to the "slave" label
       And I queue a build

       And I add the label "slave" to the slave
       Then the build should succeed
       And jobs should be executed in order on the slave
           | P1 | P2 |
     */
    @Test
    public void match_jobs_by_view() {

    }
}
