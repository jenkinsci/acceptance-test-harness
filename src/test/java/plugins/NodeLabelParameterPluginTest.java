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

        Build b = j.queueBuild(singletonMap("slavename", s.getName())).shouldSucceed();
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

        Build b = j.queueBuild(singletonMap("slavelabel", s1.getName())).shouldSucceed();
        assertThat(b.getNode(), is(s1.getName()));

        b = j.queueBuild(singletonMap("slavelabel", String.format("!%s && !%s",s1.getName(), s2.getName()))).shouldSucceed();
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

        Build b = j.queueBuild(singletonMap("slavename", s.getName()+",master")).shouldSucceed();

        assertThat(j.getNextBuildNumber(), is(3));

        j.getLastBuild().waitUntilFinished();

        j.shouldHaveBuiltOn(jenkins, "master");
        j.shouldHaveBuiltOn(jenkins,s.getName());
    }

    @Test
    public void run_on_a_particular_offline_slave() throws Exception {
        
    }
}
