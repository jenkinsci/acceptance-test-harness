package org.jenkinsci.test.acceptance.plugins.credentialsbinding;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.openqa.selenium.WebElement;

public class ManagedCredentialsBinding extends ContainerPageObject {

    public ManagedCredentialsBinding(ContainerPageObject po) {
        super(po, po.url("configure/"));
    }
    
    /**
     * Adds a credential binding of the type passed as parameter
     *
     */
    public <T extends CredentialsBinding> T addCredentialBinding(final Class<T> type) {
        String path = createPageArea("/org-jenkinsci-plugins-credentialsbinding-impl-SecretBuildWrapper/bindings", new Runnable() {
            @Override public void run() {
                control(by.path("/org-jenkinsci-plugins-credentialsbinding-impl-SecretBuildWrapper/hetero-list-add[bindings]")).selectDropdownMenu(type);
            }
        });
        return newInstance(type, this, path);
    }
}
