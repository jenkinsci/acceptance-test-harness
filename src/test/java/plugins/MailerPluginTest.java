package plugins;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.mailer.Mailer;
import org.jenkinsci.test.acceptance.plugins.mailer.MailerGlobalConfig;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.utils.mail.MailService;
import org.junit.Before;
import org.junit.Test;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.regex.Pattern;

@WithPlugins("mailer")
public class MailerPluginTest extends AbstractJUnitTest {
    @Inject
    MailerGlobalConfig mailer;

    @Inject
    MailService mail;

    @Before
    public void setup() {
        mail.setup(jenkins);
    }

    @Test
    public void send_test_mail() throws IOException, MessagingException {
        jenkins.configure();
        mailer.sendTestMail("admin@example.com");
        mail.assertMail(
                Pattern.compile("Test email #1"),
                "admin@example.com",
                Pattern.compile("This is test email #1 sent from Jenkins"));
    }

    @Test
    public void send_mail_for_failed_build() throws IOException, MessagingException {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.addShellStep("fail");
        Mailer m = job.addPublisher(Mailer.class);
        m.recipients.set("dev@example.com mngmnt@example.com");
        job.save();

        job.startBuild().shouldFail();
        mail.assertMail(
                Pattern.compile("Build failed in Jenkins: .* #1"),
                "dev@example.com mngmnt@example.com",
                Pattern.compile("failure"));
    }
}
