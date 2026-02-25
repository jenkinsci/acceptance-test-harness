package org.jenkinsci.test.acceptance.plugins.credentials;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.openqa.selenium.By;

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
     * @return Control
     */
    public Control checkSystemPage(String name) {
        return control(by.link(name));
    }

    /**
     * Check if the given credential is part of the domain.
     */
    public Control checkIfCredentialsExist(String name) {
        By xpath = by.xpath("//span[contains(text(), '" + name + "')]/parent::div/parent::div");
        return control(xpath);
    }

    /**
     * Find the href of the associted
     */
    public String credentialById(String name) {
        By xpath = by.xpath("//td[contains(text(),'" + name + "')]/parent::tr//a | //span[contains(text(),'" + name
                + "')]/parent::div/parent::div//a");
        return control(xpath).resolve().getAttribute("href");
    }
}
