package plugins;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.plugins.mailer.Mailer;
import org.jenkinsci.test.acceptance.plugins.mailer.MailerGlobalConfig;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Before;
import org.junit.Test;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 Feature: Notify users via email
   In order to have all contributors informed
   As a Jenkins project manager
   I want to send and configure mail notifications
 */
public class MailerPluginTest extends AbstractJUnitTest {
    @Inject
    MailerGlobalConfig mailer;

    @Before
    public void setup() {
        jenkins.configure();
        mailer.setupDefaults();
        jenkins.save();
    }

    /**
     Scenario: Send test email
       Given a default mailer setup
       When I send test mail to "admin@example.com"
       Then a mail message "Test email #1" for "admin@example.com" should match
           """
           This is test email #1 sent from Jenkins
           """
     */
    @Test
    public void send_test_mail() throws IOException, MessagingException {
        jenkins.configure();
        mailer.sendTestMail("admin@example.com");
        mailer.assertMail(
                Pattern.compile("Test email #1"),
                "admin@example.com",
                Pattern.compile("This is test email #1 sent from Jenkins"));
    }

    /**
     Scenario: Send mail for failed build
       Given a default mailer setup
       And a job
       When I configure the job
       And I add always fail build step
       And I configure mail notification for "dev@example.com mngmnt@example.com"
       And I save the job
       And I build the job
       Then a mail message "Build failed in Jenkins: .* #1" for "dev@example.com mngmnt@example.com" should match
           """
           failure
           """
     */
    @Test
    public void send_mail_for_failed_build() throws IOException, MessagingException {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.addShellStep("fail");
        Mailer m = job.addPublisher(Mailer.class);
        m.recipients.set("dev@example.com mngmnt@example.com");
        job.save();

        job.queueBuild().shouldFail();
        mailer.assertMail(
                Pattern.compile("Build failed in Jenkins: .* #1"),
                "dev@example.com mngmnt@example.com",
                Pattern.compile("failure"));
    }
}
