package org.jenkinsci.test.acceptance.plugins.email_ext;

import org.jenkinsci.test.acceptance.po.AbstractStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Editable Email Notification")
public class EmailExtPublisher extends AbstractStep implements PostBuildStep {
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
        control("project_triggers/hetero-list-add[recipientProviders]").selectDropdownMenu("Recipient List");
    }

    public void ensureAdvancedOpened() {
        if (!advacedOpened) {
            control("advanced-button").click();
            advacedOpened = true;
        }
    }
}
