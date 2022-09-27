package org.jenkinsci.test.acceptance.plugins.mailer;

import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

import javax.inject.Inject;

/**
 * Global config page for the mailer plugin.
 *
 * @author Kohsuke Kawaguchi
 */
public class MailerGlobalConfig extends PageAreaImpl {
    public final Control smtpServer = control("smtpHost", "smtpServer");
    public final Control advancedButton = control("advanced-button");
    public final Control useSMTPAuth = control("authentication", "useSMTPAuth");
    public final Control smtpAuthUserName = control("authentication/username", "useSMTPAuth/smtpAuthUserName");
    public final Control smtpAuthPassword = control("authentication/password", "useSMTPAuth/smtpAuthPasswordSecret", "useSMTPAuth/smtpAuthPassword");
    public final Control smtpPort = control("smtpPort");
    public final Control replyToAddress = control("replyToAddress");

    @Inject
    public MailerGlobalConfig(Jenkins jenkins) {
        super(jenkins, "/hudson-tasks-Mailer");
    }

    public void sendTestMail(String recipient) {
        control(by.path(getPath() + '/')).check();

        // these two controls have weird paths that don't fit well with relative path expression
        new Control(getPage(),"/hudson-tasks-Mailer//sendTestMailTo").set(recipient);
        new Control(getPage(),"/hudson-tasks-Mailer//validate-button").click();

        waitFor(driver, hasContent("Email was successfully sent"), 30);
    }
}
