package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Test;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Feature: configure slaves
 * In order to effectively use more machines
 * As a user
 * I want to be able to configure slaves and jobs to distribute load
 */
public class ConfigureSlavesTest extends AbstractJUnitTest {

    @Inject
    SlaveController slave1;

    /**
     * Scenario: Tie a job to a specified label
     * Given a job
     * And a dumb slave
     * When I add the label "test" to the slave
     * And I configure the job
     * And I tie the job to the "test" label
     * And I build the job
     * Then the job should be tied to the "test" label
     * And the build should run on the slave
     */
    @Test
    public void tie_job_to_specifid_label() throws Exception {
        FreeStyleJob j = jenkins.jobs.create();

        Slave s = slave1.install(jenkins).get();

        s.configure();
        s.setLabels("test");
        s.save();

        j.configure();
        j.setLabelExpression("test");
        j.save();

        Build b = j.startBuild().shouldSucceed();
        j.shouldBeTiedToLabel("test");
        assertThat(b.getNode(), is(s.getName()));
    }
}
