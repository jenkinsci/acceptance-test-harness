package org.jenkinsci.test.acceptance.plugins.mailer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
        smtpAuthPassword.set("72a80a49ae5ab81d");
        smtpPort.set("2525");

        // Fingerprint to identify message sent from this test run
        replyToAddress.set(fingerprint);

        // Set for email-ext plugin as well if available
        WebElement e = getElement(by.path("/hudson-plugins-emailext-ExtendedEmailPublisher/ext_mailer_default_replyto"));
        if (e!=null)
            e.sendKeys(fingerprint);
    }

    public void sendTestMail(String recipient) {
        control("").check();
        control("/sendTestMailTo").set(recipient);
        control("/validate-button").click();
    }

    public MimeMessage getMail(Pattern subject) throws IOException {
        List<MimeMessage> match = new ArrayList<>();

        for (JsonNode msg : fetchMessages()) {
            if (subject.matcher(msg.get("message").get("title").asText()).matches()) {
                MimeMessage m = fetchMessage(msg.get("message").get("id").asText());
                if (isOurs(m)) {
                    match.add(m);
                }
            }
        }

        assertEquals(1, match.size());  // More than one matching message
        return match.get(0);
    }

    public List<MimeMessage> getAllMails() throws IOException {
        List<MimeMessage> match = new ArrayList<>();

        for (JsonNode msg : fetchMessages()) {
            MimeMessage m = fetchMessage(msg.get("message").get("id").asText());
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
        return new ObjectMapper().readTree(new URL(String.format(fmt,args)));
    }

    public JsonNode fetchMessages() throws IOException {
        return fetchJson("http://mailtrap.io/api/v1/inboxes/%s/messages?page=1&token=%s", MAILBOX, TOKEN);
    }

    public MimeMessage fetchMessage(String id) throws IOException {
        JsonNode s = fetchJson("http://mailtrap.io/api/v1/inboxes/%s/messages/%s?token=%s", MAILBOX, id, TOKEN)
                .get("message").get("source");

        try {
            return new MimeMessage(Session.getDefaultInstance(System.getProperties()), new ByteArrayInputStream(s.asText().getBytes("UTF-8")));
        } catch (MessagingException e) {
            throw new IOException(e);
        }
    }

    public static final String MAILBOX = "selenium-tests-69507a9ef0aa7fa5";
    public static final String TOKEN = "wvpGO0F4gT8DTZJIHzkpmQ";
}
