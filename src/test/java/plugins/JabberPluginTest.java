package plugins;

import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.fixtures.JabberContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.jabber.JabberGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.jabber.JabberGlobalConfig.AdvancedConfig;
import org.jenkinsci.test.acceptance.plugins.jabber.JabberGlobalConfig.MUCConfig;
import org.jenkinsci.test.acceptance.plugins.jabber.JabberGlobalConfig.EnabledConfig;
import org.jenkinsci.test.acceptance.plugins.jabber.JabberPublisher;
import org.jenkinsci.test.acceptance.plugins.jabber.JabberPublisher.Publishers;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Feature: Tests for Jabber plugin
 * This test case is set to @Ignore because the Jabber Plugin is not able to connect to conference.localhost
 * because DNS resolution issues. These issues arise because of the used Smack API
 *      (http://www.igniterealtime.org/projects/smack/index.jsp)
 * that does rely on successful DNS resolution, which is totally valid in terms on conformity with the XMPP Standard.
 * 
 * This test is broken until a new version of the jabber plugin with a new Smack Library is provided that falls back to
 * using the hostname if the FQDN could not be resolved, *
 *
 * @author jenky-hm
 *
 */
@WithPlugins("jabber")
@Native("docker")
@Ignore
public class JabberPluginTest extends AbstractJUnitTest {
    @Inject
    private Docker docker;

    private final String jabberIdString = "jenkins-ci@localhost/master";
    private final String jabberPasswordString = "jenkins-pw";
    private final String mucNameString = "test";
    private final String pwMucNameString = "test-room";
    private final String pwMucPasswordString = "test-room-pw";
    private final String localhostString = "localhost";
    //private final String confRoom = "*test@conference.localhost";
    //private final String confRoom = "*test@127.0.0.1";
    private final String confRoom = "test@conference.localhost";

    /**
     @native(docker)
     Scenario: Configure a job with jabber notification
     Given I have installed the "jabber" plugin
     And a docker fixture "jabber"
     And a job
     When I configure docker fixture as Jabber server
     And I configure the jenkins to use a jid and password
     And I configure the jenkins to use a the test chatroom
     And I configure the job to use a shell step
     And I save the job
     And I build the job
     Then the build should succeed
     And Jabber plugin should have notified the users in test chatroom on docker fixture
     */
    @Test
    public void jabber_notification_success_publishing() throws IOException, InterruptedException {
        JabberContainer jabber = docker.start(JabberContainer.class);
        //Resource cp_file = resource(resourceFilePath);
        //File sshFile = sshd.getPrivateKey();

        FreeStyleJob j = jenkins.jobs.create();
        jenkins.configure();
        EnabledConfig ec = new JabberGlobalConfig(jenkins).enableConfig(); {
            ec.jabberid.set(jabberIdString);
            //sleep(10000);
            ec.jabberPassword.set(jabberPasswordString);
            //sleep(10000);
        };

        MUCConfig mc = ec.addMUCConfig();{
            //sleep(10000);
            mc.mucName.set(mucNameString);
            //mc.mucName.set(pwMucNameString);
            //mc.mucPassword.set(pwMucPasswordString);
        }

        AdvancedConfig ac = ec.addAdvancedConfig();{
            //sleep(10000);
            ac.hostname.set(jabber.ipBound(5222));
            //sleep(10000);
            ac.port.set(jabber.port(5222));
            ac.enableSASL.check(false);

        }
        jenkins.save();

        j.configure(); {
            j.addShellStep("echo 'Hello, Jenkins CI was here!'");
            JabberPublisher jp = j.addPublisher(JabberPublisher.class);
            Publishers publisher = jp.setPublisher();
            sleep(10000);
            publisher.targets.set(confRoom);

        }
        j.save();
        j.startBuild().shouldSucceed();
        sleep(20000);
        File logfile = jabber.getLogbotLogFile();
        assertThat(FileUtils.readFileToString(logfile), CoreMatchers.containsString("SUCCESS"));
    }

}
