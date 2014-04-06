package org.jenkinsci.test.acceptance.plugins.mailer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static javax.mail.Message.RecipientType.TO;
import static org.hamcrest.CoreMatchers.is;

/**
 * Global config page for the mailer plugin.
 *
 * @author Kohsuke Kawaguchi
 */
public class MailerGlobalConfig extends PageArea {
    public final Control smtpServer = control("smtpServer");
    public final Control advancedButton = control("advanced-button");
    public final Control useSMTPAuth = control("useSMTPAuth");
    public final Control smtpAuthUserName = control("useSMTPAuth/smtpAuthUserName");
    public final Control smtpAuthPassword = control("useSMTPAuth/smtpAuthPassword");
    public final Control smtpPort = control("smtpPort");
    public final Control replyToAddress = control("replyToAddress");

    /**
     * Unique ID for this test run.
     */
    public final String fingerprint;

    @Inject
    public MailerGlobalConfig(Jenkins jenkins) {
        super(jenkins, "/hudson-tasks-Mailer");
        fingerprint = PageObject.createRandomName() + "@" + MAILBOX + ".com";
    }

    /**
     * Set up the configuration to use the shared mailtrap.io account.
     */
    public void setupDefaults() {
        smtpServer.set("mailtrap.io");
        advancedButton.click();
        useSMTPAuth.check();
        smtpAuthUserName.set(MAILBOX);
        smtpAuthPassword.set(PASSWORD);
        smtpPort.set("2525");

        // Fingerprint to identify message sent from this test run
        replyToAddress.set(fingerprint);

        // Set for email-ext plugin as well if available
        WebElement e = getElement(by.path("/hudson-plugins-emailext-ExtendedEmailPublisher/ext_mailer_default_replyto"));
        if (e!=null)
            e.sendKeys(fingerprint);
    }

    public void sendTestMail(String recipient) {
        control(by.path(path + '/')).check();
        control("/sendTestMailTo").set(recipient);
        control("/validate-button").click();
    }

    /**
     * @return null if nothing found.
     */
    public MimeMessage getMail(Pattern subject) throws IOException {
        List<MimeMessage> match = new ArrayList<>();

        for (JsonNode msg : fetchMessages()) {
            if (subject.matcher(msg.get("subject").asText()).find()) {
                MimeMessage m = fetchMessage(msg.get("id").asText());
                if (isOurs(m)) {
                    match.add(m);
                }
            }
        }

        switch (match.size()) {
            case 0: return null;
            case 1: return match.get(0);
            default: throw new AssertionError("More than one matching message found");
        }
    }

    public List<MimeMessage> getAllMails() throws IOException {
        List<MimeMessage> match = new ArrayList<>();

        for (JsonNode msg : fetchMessages()) {
            MimeMessage m = fetchMessage(msg.get("id").asText());
            if (isOurs(m))
                match.add(m);
        }

        return match;
    }

    /**
     * Does this email belong to our test case (as opposed to other tests that might be running elsewhere?)
     */
    private boolean isOurs(MimeMessage m) {
        try {
            Address[] r = m.getReplyTo();
            if (r==null)    return false;
            for (Address a : r) {
                if (a.toString().contains(fingerprint))
                    return true;
            }
            return false;
        } catch (MessagingException e) {
            throw new AssertionError(e);
        }
    }

    public JsonNode fetchJson(String fmt, Object... args) throws IOException {
        String s = IOUtils.toString(new URL(String.format(fmt, args)).openStream());
        return new ObjectMapper().readTree(s);
    }

    public JsonNode fetchMessages() throws IOException {
        return fetchJson("https://mailtrap.io/api/v1/inboxes/%s/messages?page=1&api_token=%s", INBOX_ID, TOKEN);
    }

    public MimeMessage fetchMessage(String id) throws IOException {
        URL raw = new URL(String.format("https://mailtrap.io/api/v1/inboxes/%s/messages/%s/body.eml?api_token=%s", INBOX_ID, id, TOKEN));

        try {
            return new MimeMessage(Session.getDefaultInstance(System.getProperties()), raw.openStream());
        } catch (MessagingException e) {
            throw new IOException(e);
        }
    }

    /**
     * Checks that the mail has arrived.
     */
    public void assertMail(final Pattern subject, String recipient, Pattern body) throws MessagingException, IOException {
        MimeMessage msg = waitForCond(new Callable<MimeMessage>() {
            @Override
            public MimeMessage call() throws Exception {
                return getMail(subject);
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

    public String textMessage(MimeMessage msg) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            msg.writeTo(os);
        } catch (IOException | MessagingException ex) {
            throw new AssertionError(ex);
        }
        return os.toString();
    }

    public static final String MAILBOX = "19251ad93afaab19b";
    public static final String PASSWORD = "c9039d1f090624";
    public static final String TOKEN = "2c04434bd66dfc37c130171f9d061af2";
    public static final String INBOX_ID = "23170";
}
