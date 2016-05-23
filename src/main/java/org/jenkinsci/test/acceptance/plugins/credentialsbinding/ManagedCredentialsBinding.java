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
     * @param type
     * @return
     */
    public <T extends CredentialsBinding> T addCredentialBinding(Class<T> type) {
        control(by.path("/org-jenkinsci-plugins-credentialsbinding-impl-SecretBuildWrapper/hetero-list-add[bindings]")).selectDropdownMenu(type);
        elasticSleep(1000); // it takes some time until the element is visible
        WebElement last = last(by.xpath("//div[@name='bindings']"));
        String path = last.getAttribute("path");

        return newInstance(type, this, path);
    }
}
