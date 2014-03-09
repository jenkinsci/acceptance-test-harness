package plugins.audit_trail;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.JenkinsLogger;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class AuditTrailPluginTest extends AbstractJUnitTest {
    @Inject
    Jenkins jenkins;

    JenkinsLogger auditTrail;

    @Before
    public void setUp() {
        jenkins.getPluginManager().installPlugin("audit-trail");
        /*
            It takes a couple of seconds for the plugin to start recording and displaying events.
            It is supposed to be ready after this step.
         */
        auditTrail = jenkins.getLogger("Audit Trail");
    }


    /**
     * Scenario: Trail should be empty after installation
     *   Given I have set up the Audit Trail plugin
     *   Then the audit trail should be empty
     *
     *   When I create a job named "job"
     *   And  I create dumb slave named "slave"
     *   Then the audit trail should contain event "/createItem"
     *   And  the audit trail should contain event "/computer/createItem"
     */
    @Test
    public void trail_should_be_empty_after_login() {
        assertThat(auditTrail.isEmpty(), is(true));

        jenkins.createJob(FreeStyleJob.class, "job");
        jenkins.createDumbSlave("slave");

        List<String> events = auditTrail.getEvents();
        assertThat(events, hasItem("/createItem"));
        assertThat(events, hasItem("/computer/createItem"));
    }
}
