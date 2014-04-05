package org.jenkinsci.test.acceptance.plugins.email_ext;

import org.jenkinsci.test.acceptance.po.BuildStepPageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * @author Kohsuke Kawaguchi
 */
@BuildStepPageObject("Editable Email Notification")
public class EmailExtPublisher extends PostBuildStep {
    public final Control subject = control("project_default_subject");
    private final Control recipient = control("project_recipient_list", "recipientlist_recipients");
    public final Control body = control("project_default_content");

    private boolean advacedOpened;

    public EmailExtPublisher(Job parent, String path) {
        super(parent, path);
    }

    public void setRecipient(String r) {
        recipient.set(r);
        ensureAdvancedOpened();
        control("project_triggers/sendToList").check();
    }

    public void ensureAdvancedOpened() {
        if (!advacedOpened) {
            control("advanced-button").click();
            advacedOpened = true;
        }
    }
}
