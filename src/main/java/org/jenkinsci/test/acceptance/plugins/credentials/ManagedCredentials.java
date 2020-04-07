package org.jenkinsci.test.acceptance.plugins.credentials;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;

/**
 * "Manage Credentials" page.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public class ManagedCredentials extends ContainerPageObject {
    public final Control addButton = control("/domainCredentials/hetero-list-add[credentials]");
    public final Control addDomainButton = control("/repeatable-add");

    public static final String DEFAULT_DOMAIN = "_";

    public ManagedCredentials(Jenkins j) {
        super(j, j.url("credentials/store/system/"));
    }

    public ManagedCredentials(Jenkins j, String domain) {
        super(j, j.url("credentials/store/system/domain/" + domain + "/"));
    }

    public ManagedCredentials(Jenkins j, String domain, String user) {
        super(j, j.url(String.format("user/%s/credentials/store/user/domain/%s/", user, domain)));
    }

    /**
     * Find if the given control exists on the main credentials page.
     * @param name
     * @return Control
     */
    public Control checkSystemPage(String name) {
        return control(by.link(name));
    }

    /**
     * Check if the given credential is part of the domain.
     * @param name
     * @return
     */
    public Control checkIfCredentialsExist(String name) {
        Control control = control(by.xpath("//a[@title='" + name + "']"));
        // post credentials-2.3.2
        if (!control.exists()) {
            control = control(by.xpath("//td[contains(text(),'" + name + "')]"));
        }
        return control;
    }

    /**
     * Find the href of the associted
     * @param name
     * @return
     */
    public String credentialById(String name) {
        Control control = control(by.xpath("//a[@title='" + name + "']"));
        // post credentials-2.3.2
        if (!control.exists()) {
            control = control(by.xpath("//td[contains(text(),'" + name + "')]/parent::tr//a"));
        }
        return control.resolve().getAttribute("href");
    }
}
