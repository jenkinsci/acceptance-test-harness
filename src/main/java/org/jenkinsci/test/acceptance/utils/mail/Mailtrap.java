package org.jenkinsci.test.acceptance.utils.mail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.io.IOUtils;
import org.jenkinsci.test.acceptance.ByFactory;
import org.jenkinsci.test.acceptance.guice.TestScope;
import org.jenkinsci.test.acceptance.plugins.email_ext.GlobalConfig;
import org.jenkinsci.test.acceptance.plugins.mailer.MailerGlobalConfig;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageObject;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * {@link MailService} that uses Mailtrap.io
 *
 * This class comes with the default account shared by the project, but
 * you can also specify a separate account from wiring script like this:
 *
 * <pre>
 * bind MailService toInstance new Mailtrap(...);
 * </pre>
 *
 * @author Kohsuke Kawaguchi
 */
@TestScope
public class Mailtrap extends MailService {
    // these default values is the account that the project "owns".
    private static final int PORT = 2525;
    private static final String HOST = "smtp.mailtrap.io";
    private String MAILBOX = "selenium-tests-69507a9ef0aa7fa5";
    private String PASSWORD = "72a80a49ae5ab81d";
    private String TOKEN = "28314ab4b06951db3531370d82d9232d";
    private String INBOX_ID = "16868";

    /**
     * Unique ID for this test run.
     */
    public final String fingerprint;

    public Mailtrap() {
        fingerprint = PageObject.createRandomName() + "@" + MAILBOX + ".com";
    }

    /**
     * This constructor allow you to override values from wiring script.
     */
    public Mailtrap(String MAILBOX, String PASSWORD, String TOKEN, String INBOX_ID) {
        this();
        this.MAILBOX = MAILBOX;
        this.PASSWORD = PASSWORD;
        this.TOKEN = TOKEN;
        this.INBOX_ID = INBOX_ID;
    }

    /**
     * Set up the configuration to use the shared mailtrap.io account.
     */
    @Override
    public void setup(Jenkins jenkins) {
        jenkins.configure();
        MailerGlobalConfig config = new MailerGlobalConfig(jenkins);
        config.smtpServer.set(HOST);
        config.advancedButton.click();
        config.useSMTPAuth.check();
        config.smtpAuthUserName.set(MAILBOX);
        config.smtpAuthPassword.set(PASSWORD);
        config.smtpPort.set(PORT);

        // Fingerprint to identify message sent from this test run
        config.replyToAddress.set(fingerprint);

        jenkins.save();

        // Set for email-ext plugin as well if available
        if (jenkins.getPluginManager().isInstalled("email-ext")) {
            // For whatever reason this needs new config page opened
            jenkins.configure();
            GlobalConfig ext = new GlobalConfig(jenkins.getConfigPage());
            ext.smtpServer(HOST);
            ext.auth(MAILBOX, PASSWORD);
            ext.smtpPort(PORT);
            ext.replyTo(fingerprint);
            jenkins.save();
        }
    }

    /**
     * @return null if nothing found.
     */
    @Override
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

    @Override
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

    private static final ByFactory by = new ByFactory();
}
