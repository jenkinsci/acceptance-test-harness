package org.jenkinsci.test.acceptance.utils.mail;

import com.google.common.base.Joiner;

import org.jenkinsci.test.acceptance.guice.World;
import org.jenkinsci.test.acceptance.junit.Wait;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayer;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.junit.Assert;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static javax.mail.Message.RecipientType.*;
import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Encapsulated the mail server to be used for test and access to emails that were sent.
 *
 * <p>
 * This allows users/runners to use different service (for example a local mail server
 * or even a docker image) or a different account on mailtrap.
 *
 * @author Kohsuke Kawaguchi
 * @see Mailtrap
 * @see docs/EMAIL.md
 */
public abstract class MailService extends Assert {

    /**
     * Set up the Jenkins configuration to use the mail delivery service
     * encapsulated by this object.
     */
    public abstract void setup(Jenkins config);

    /**
     * All the emails sent in the test.
     */
    public abstract List<MimeMessage> getAllMails() throws IOException;

    /**
     * Picks up the first email whose subject matches the given pattern.
     *
     * @return null if nothing found.
     */
    public abstract MimeMessage getMail(Pattern subject) throws IOException;

    /**
     * Checks that the mail has arrived.
     */
    public void assertMail(final Pattern subject, String recipient, Pattern body) throws MessagingException, IOException {
        CapybaraPortingLayer hackish = new CapybaraPortingLayerImpl(World.get().getInjector());

        MimeMessage msg = hackish.waitFor().withMessage("Email whose subject matches: %s", subject).pollingEvery(5, TimeUnit.SECONDS)
                .until(new MailArrives(subject))
        ;

        String actualRecipients = Joiner.on(' ').join(msg.getRecipients(TO));
        assertThat("recipient", actualRecipients, is(recipient));
        Object c = msg.getContent();
        if (c instanceof MimeMultipart) {
            MimeMultipart content = (MimeMultipart) c;
            c = content.getBodyPart(0).getContent();
        }
        assertThat(c.toString(), containsRegexp(body));
    }

    /**
     * Checks that the mail has arrived.
     */
    public void assertMail(final Pattern subject, String recipient) throws MessagingException, IOException {
        assertMail(subject, recipient, Pattern.compile(".*"));
    }

    /**
     * Obtains the content of the email in plain text to assist asserting its content.
     *
     * This method tries to flatten multipart content as much as it can.
     */
    public String textMessage(MimeMessage msg) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            msg.writeTo(os);
        } catch (IOException | MessagingException ex) {
            throw new AssertionError(ex);
        }
        return os.toString();
    }

    private final class MailArrives extends Wait.Predicate<MimeMessage> {
        private final Pattern subject;

        private MailArrives(Pattern subject) {
            this.subject = subject;
        }

        @Override
        public MimeMessage apply() throws Exception {
            return getMail(subject);
        }

        @Override
        public String diagnose(Throwable lastException, String message) {
            try {
                List<MimeMessage> mails = getAllMails();
                StringBuilder sb = new StringBuilder("Received messages ").append(mails.size()).append(":\n");
                for (MimeMessage m: mails) {
                    sb.append('\t').append(m.getSubject()).append('\n');
                }
                return sb.toString();
            } catch (IOException|MessagingException ex) {
                return "Unable to diagnose " + ex.getMessage();
            }
        }
    }
}
