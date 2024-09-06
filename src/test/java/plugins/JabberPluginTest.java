package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.JabberContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.jabber.JabberGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.jabber.JabberGlobalConfig.AdvancedConfig;
import org.jenkinsci.test.acceptance.plugins.jabber.JabberGlobalConfig.EnabledConfig;
import org.jenkinsci.test.acceptance.plugins.jabber.JabberGlobalConfig.MUCConfig;
import org.jenkinsci.test.acceptance.plugins.jabber.JabberPublisher;
import org.jenkinsci.test.acceptance.plugins.jabber.JabberPublisher.Publishers;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * This test case is set to @Ignore because the Jabber Plugin is not able to connect to conference.localhost
 * because DNS resolution issues. These issues arise because of the used Smack API
 *      (<a href="https://www.igniterealtime.org/projects/smack/index.jsp">Smack Documentation</a>)
 * that does rely on successful DNS resolution, which is totally valid in terms on conformity with the XMPP Standard.
 * <p>
 * This test is broken until a new version of the jabber plugin with a new Smack Library is provided that falls back to
 * using the hostname if the FQDN could not be resolved, *
 *
 * @author jenky-hm
 */
@WithPlugins("jabber")
@Category(DockerTest.class)
@WithDocker
@Ignore("TODO: Bring this back to life or remove from testsuite")
public class JabberPluginTest extends AbstractJUnitTest {
    @Inject
    private DockerContainerHolder<JabberContainer> docker;

    private final String jabberIdString = "jenkins-ci@localhost/master";
    private final String jabberPasswordString = "jenkins-pw";
    private final String mucNameString = "test";
    private final String confRoom = "test@conference.localhost";

    @Test
    public void jabber_notification_success_publishing() throws IOException, InterruptedException {
        JabberContainer jabber = docker.get();
        // Resource cp_file = resource(resourceFilePath);
        // File sshFile = sshd.getPrivateKey();

        FreeStyleJob j = jenkins.jobs.create();
        jenkins.configure();
        EnabledConfig ec = new JabberGlobalConfig(jenkins).enableConfig();
        {
            ec.jabberid.set(jabberIdString);
            // sleep(10000);
            ec.jabberPassword.set(jabberPasswordString);
            // sleep(10000);
        }
        ;

        MUCConfig mc = ec.addMUCConfig();
        {
            // sleep(10000);
            mc.mucName.set(mucNameString);
            // mc.mucName.set(pwMucNameString);
            // mc.mucPassword.set(pwMucPasswordString);
        }

        AdvancedConfig ac = ec.addAdvancedConfig();
        {
            // sleep(10000);
            ac.hostname.set(jabber.ipBound(5222));
            // sleep(10000);
            ac.port.set(jabber.port(5222));
            ac.enableSASL.check(false);
        }
        jenkins.save();

        j.configure();
        {
            j.addShellStep("echo 'Hello, Jenkins CI was here!'");
            JabberPublisher jp = j.addPublisher(JabberPublisher.class);
            Publishers publisher = jp.setPublisher();
            elasticSleep(10000);
            publisher.targets.set(confRoom);
        }
        j.save();
        j.startBuild().shouldSucceed();
        sleep(20000);
        File logfile = jabber.getLogbotLogFile();
        assertThat(FileUtils.readFileToString(logfile, StandardCharsets.UTF_8), containsString("SUCCESS"));
    }
}
