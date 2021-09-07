package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.audit_trail.AuditTrailLogger;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.LocalSlaveController;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@WithPlugins("audit-trail")
public class AuditTrailPluginTest extends AbstractJUnitTest {
    AuditTrailLogger auditTrail;

    @Before
    public void setUp() {
        auditTrail = AuditTrailLogger.create(jenkins);
    }

    @Test
    public void trail_should_be_empty_after_login() {
        assertThat(auditTrail.isEmpty(), is(true));
    }

    @Test
    public void trail_should_contain_logged_events() throws Exception {
        FreeStyleJob freeStyleJob = jenkins.jobs.create(FreeStyleJob.class);

        // purpose of this is to just go through the motion of creating a new slave,
        // so this one can bypass SlaveController.
        Slave slave = new LocalSlaveController().install(jenkins).get();

        List<String> events = auditTrail.getEvents();
        assertThat(events, hasItem("/createItem (" + freeStyleJob.name + ")"));
        assertThat(events, hasItem("/computer/createItem (" + slave.getName() + ")"));
    }
}
