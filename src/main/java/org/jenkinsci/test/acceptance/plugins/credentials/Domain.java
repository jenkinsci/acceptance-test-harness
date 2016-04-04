package org.jenkinsci.test.acceptance.plugins.credentials;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageArea;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

@Describable("Credential Domain")
public class Domain extends PageAreaImpl {

    public Control name = control("/domain/name");
    public Control description = control("/domain/description");
    public final Control addCredentialButton = control("/hetero-list-add[credentials]");
    public final Control addSpecificationButton = control("/domain/hetero-list-add[specifications]");
    
    public Domain(PageObject context, String path) {
        super(context, path);
    }
    
    public Domain(PageArea area, String relativePath) {
        super(area, relativePath);
    }

    /**
     * Add a domain
     */
    public void add() {
        find(by.button("Add Domain")).click();
    }
    
    /**
     * Adds a new credential and bind it to the page ae object.
     */
    public <T extends Credential> T addCredential(Class<T> type) {
        addCredentialButton.selectDropdownMenu(type);

        String path = last(by.xpath("//div[@name='credentials']")).getAttribute("path");

        return newInstance(type, this, path);
    }
}
