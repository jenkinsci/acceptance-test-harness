package org.jenkinsci.test.acceptance.plugins.email_ext;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Editable Email Notification")
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

        // since 2.38 refactored to hetero-list, recepients are preselected
        if (page.getJenkins().getPlugin("email-ext").isOlderThan("2.38")) {
            ensureAdvancedOpened();
            control("project_triggers/sendToList").check();
        }
    }

    public void ensureAdvancedOpened() {
        if (!advacedOpened) {
            control("advanced-button").click();
            advacedOpened = true;
        }
    }
}
