package org.jenkinsci.test.acceptance.plugins.credentialsbinding;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * Represents a credential binding in the job configuration page.
 */
public class CredentialsBinding extends PageAreaImpl {

    public Control credentialId = control("credentialId");
    public Control variable = control("variable");
    
    public CredentialsBinding(PageArea area, String path) {
        super(area, path);
    }
    
    public CredentialsBinding(ContainerPageObject po, String path) {
        super(po, path);
    }
    
    /**
     * Checks whether there are credentials in the credentials drop down
     * 
     * @return true if there are no credentials, false otherwise
     */
    public boolean noCredentials() {
        return getElement(by.tagName("options")) == null;
    }
}
