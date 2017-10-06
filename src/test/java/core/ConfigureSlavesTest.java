package core;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Node;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.inject.Inject;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConfigureSlavesTest extends AbstractJUnitTest {

    @Inject
    SlaveController slave1;

    @Test
    @Category(SmokeTest.class)
    public void tie_job_to_specifid_label() throws Exception {
        FreeStyleJob j = jenkins.jobs.create();

        Node s = slave1.install(jenkins).get();

        s.configure();
        s.setLabels("test");
        s.save();

        j.configure();
        j.setLabelExpression("test");
        j.save();

        Build b = j.startBuild().shouldSucceed();
        j.shouldBeTiedToLabel("test");
        assertThat(b.getNode(), is(s));
    }
}
