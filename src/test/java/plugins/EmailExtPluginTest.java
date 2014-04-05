package plugins;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.email_ext.EmailExtPublisher;
import org.jenkinsci.test.acceptance.plugins.mailer.MailerGlobalConfig;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static javax.mail.Message.RecipientType.TO;
import static org.hamcrest.CoreMatchers.is;

/**
 Feature: Adds support for editable email configuration
   In order to be able to send customized mail notifications
   As a Jenkins user
   I want to install and configure email-ext plugin
 */
@WithPlugins("email-ext")
public class EmailExtPluginTest extends AbstractJUnitTest {
    @Inject
    private MailerGlobalConfig mailer;

    /**
     Scenario: Build
       Given I have installed the "email-ext" plugin
       And a default mailer setup
       And a job
       And I add always fail build step
       And I configure editable email "Modified $DEFAULT_SUBJECT" for "dev@example.com"
           """
               $DEFAULT_CONTENT
               with amendment
           """
       And I save the job
       And I build the job
       Then the build should fail
       And a mail message "^Modified " for "dev@example.com" should match
           """
           with amendment$
           """
     */
    @Test
    public void build() throws MessagingException, IOException {
        jenkins.configure();
        mailer.setupDefaults();
        jenkins.save();

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.addShellStep("false");
        EmailExtPublisher pub = job.addPublisher(EmailExtPublisher.class);
        pub.subject.set("Modified $DEFAULT_SUBJECT");
        pub.setRecipient("dev@example.com");
        pub.body.set("$DEFAULT_CONTENT\nwith amendment");
        job.save();

        Build b = job.queueBuild().shouldFail();

        MimeMessage msg = jenkins.waitForCond(new Callable<MimeMessage>() {
            @Override
            public MimeMessage call() throws Exception {
                return mailer.getMail(Pattern.compile("^Modified "));
            }
        });

        assertThat(msg.getRecipients(TO)[0].toString(), is("dev@example.com"));
        MimeMultipart content = (MimeMultipart)msg.getContent();
        assertTrue(content.getBodyPart(0).getContent().toString().endsWith("\nwith amendment"));
    }
}
