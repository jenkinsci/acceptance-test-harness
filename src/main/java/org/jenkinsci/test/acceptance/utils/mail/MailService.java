package org.jenkinsci.test.acceptance.utils.mail;

import com.google.common.base.Joiner;
import org.jenkinsci.test.acceptance.plugins.mailer.MailerGlobalConfig;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayer;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.junit.Assert;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static javax.mail.Message.RecipientType.*;
import static org.hamcrest.CoreMatchers.*;

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
    public abstract void setup(MailerGlobalConfig config);

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
        CapybaraPortingLayer hackish = new CapybaraPortingLayerImpl(null);

        MimeMessage msg = hackish.waitForCond(new Callable<MimeMessage>() {
            @Override
            public MimeMessage call() throws Exception {
                return getMail(subject);
            }

            @Override
            public String toString() {
                return "Email whose subject matches "+subject;
            }
        });

        String actualRecipients = Joiner.on(' ').join(msg.getRecipients(TO));
        assertThat("recipient", actualRecipients, is(recipient));
        Object c = msg.getContent();
        if (c instanceof MimeMultipart) {
            MimeMultipart content = (MimeMultipart) c;
            c = content.getBodyPart(0).getContent();
        }

        assertTrue("body metches", body.matcher(c.toString()).find());
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
}
