package plugins;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.nodelabelparameter.LabelParameter;
import org.jenkinsci.test.acceptance.plugins.nodelabelparameter.NodeParameter;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Test;

import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.*;

/**
 Feature: Use node name and label as parameter
   In order to control where a build should run at the time it is triggered
   As a Jenkins user
   I want to specify the name of the slave or the label as a build parameter
 */
@WithPlugins("nodelabelparameter")
public class NodeLabelParameterPluginTest extends AbstractJUnitTest {

    @Inject
    SlaveController slave;

    @Inject
    SlaveController slave2;

    /**
     Scenario: Build on a particular slave
       Given I have installed the "nodelabelparameter" plugin
       And a job
       And a slave named "slave42"
       When I configure the job
       And I add node parameter "slavename"
       And I save the job
       And I build the job with parameter
           | slavename | slave42 |
       Then the build should run on "slave42"
     */
    @Test
    public void build_on_a_particular_slave() throws Exception {
        FreeStyleJob j = jenkins.jobs.create();

        Slave s = slave.install(jenkins).get();
        j.configure();
        j.addParameter(NodeParameter.class).setName("slavename");
        j.save();

        Build b = j.startBuild(singletonMap("slavename", s.getName())).shouldSucceed();
        assertThat(b.getNode(), is(s.getName()));
    }

    /**
     Scenario: Run on label
       Given I have installed the "nodelabelparameter" plugin
       And a job
       And a slave named "slave42"
       And a slave named "slave43"
       When I configure the job
       And I add label parameter "slavelabel"
       And I save the job
       And I build the job with parameter
           | slavelabel | slave42 |
       Then the build should run on "slave42"
       And I build the job with parameter
           | slavelabel | !slave42 && !slave43 |
       Then the build should run on "master"
     */
    @Test
    public void run_on_label() throws Exception {
        FreeStyleJob j = jenkins.jobs.create();

        Slave s1 = slave.install(jenkins).get();
        Slave s2 = slave2.install(jenkins).get();

        j.configure();
        j.addParameter(LabelParameter.class).setName("slavelabel");
        j.save();

        Build b = j.startBuild(singletonMap("slavelabel", s1.getName())).shouldSucceed();
        assertThat(b.getNode(), is(s1.getName()));

        b = j.startBuild(singletonMap("slavelabel", String.format("!%s && !%s",s1.getName(), s2.getName()))).shouldSucceed();
        assertThat(b.getNode(), is("master"));
    }

    /**
     Scenario: Run on several slaves
       Given I have installed the "nodelabelparameter" plugin
       And a job
       And a slave named "slave42"
       When I configure the job
       And I add node parameter "slavename"
       And I allow multiple nodes
       And I enable concurrent builds
       And I save the job
       And I build the job with parameter
           | slavename | slave42, master |
       Then the job should have 2 builds
       And  the job should be built on "master"
       And  the job should be built on "slave42"
     */
    @Test
    public void run_on_several_slaves() throws Exception {
        FreeStyleJob j = jenkins.jobs.create();

        Slave s = slave.install(jenkins).get();

        j.configure();
        NodeParameter p = j.addParameter(NodeParameter.class);
        p.setName("slavename");
        p.allowMultiple.check();
        j.concurrentBuild.check();
        j.save();

        Build b = j.startBuild(singletonMap("slavename", s.getName()+",master")).shouldSucceed();

        assertThat(j.getNextBuildNumber(), is(3));

        j.getLastBuild().waitUntilFinished();

        j.shouldHaveBuiltOn(jenkins, "master");
        j.shouldHaveBuiltOn(jenkins,s.getName());
    }

    /**
     * This test is intended to check that an offline slave is not ignored
     * when selected for a job and the job is configured with "Node eligibility" setting
     * is set to "All Nodes"
     *
     * It is expected that the job is pending due to the offline status of the slave.
     * But it will be reactivated as soon as the slave status becomes online.
     */

    @Test
    public void run_on_a_particular_offline_slave() throws Exception {
        FreeStyleJob j = jenkins.jobs.create();

        Slave s = slave.install(jenkins).get();

        j.configure();
        j.addParameter(NodeParameter.class).setName("slavename");
        j.save();

        //as the slave has been started after creation, we have to take it down again
        s.markOffline();
        assertTrue(s.isOffline());

        //use scheduleBuild instead of startBuild to avoid a timeout waiting for Build being started
        Build b = j.scheduleBuild(singletonMap("slavename", s.getName()));

        String pendingBuildText = find(by.xpath("//img[@alt='pending']/../..")).getText();
        String refText=String.format("(pending—%s is offline) [NodeParameterValue: slavename=%s]",s.getName(),s.getName());

        assertTrue(pendingBuildText.contains(refText));
        assertTrue(!b.hasStarted());

        //TODO: reactivate slave and verify that the build is started
    }

    /**
     * This test is intended to check that an offline slave is not ignored
     * when selected for a job and the job is configured with "Node eligibility" setting
     * is set to "All Nodes"
     *
     * It is expected that the job is pending due to the offline status of the slave.
     * But it will be reactivated as soon as the slave status becomes online.
     */
    @Test
    public void run_on_a_particular_offline_slave_with_ignore() throws Exception {
        FreeStyleJob j = jenkins.jobs.create();

        Slave s = slave.install(jenkins).get();
        j.configure();
        NodeParameter p = j.addParameter(NodeParameter.class);
        p.setName("slavename");
        p.disallowMultiple.check();
        p.ignoreOffline.click();

        j.save();

        //as the slave has been started after creation, we have to take it down again
        s.markOffline();
        assertTrue(s.isOffline());

        //use scheduleBuild instead of startBuild to avoid a timeout waiting for Build being started
        Build b = j.scheduleBuild(singletonMap("slavename", s.getName()));

        String pendingBuildText = find(by.xpath("//img[@alt='pending']/../..")).getText();
        String refText=String.format("(pending—All nodes of label ‘Job triggered without a valid online node, given where: %s’ are offline)",s.getName());

        assertTrue(pendingBuildText.contains(refText));
        assertTrue(!b.hasStarted());
    }

}
