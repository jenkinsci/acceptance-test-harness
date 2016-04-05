package org.jenkinsci.test.acceptance.plugins.credentials;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

@Describable("Credential Domain")
public class Domain extends PageAreaImpl {

    public final Control name = control("/domain/name");
    public final Control description = control("/domain/description");
    public final Control addCredentialButton = control("/hetero-list-add[credentials]");
    public final Control addSpecificationButton = control("/domain/hetero-list-add[specifications]");
    
    public Domain(ManagedCredentials context, String path) {
        super(context, path);
    }
    
    /**
     * Adds a new credential under the scope of this domain
     */
    public <T extends Credential> T addCredential(Class<T> type) {
        addCredentialButton.selectDropdownMenu(type);

        String path = last(by.name("credentials")).getAttribute("path");

        return newInstance(type, this, path);
    }
}
