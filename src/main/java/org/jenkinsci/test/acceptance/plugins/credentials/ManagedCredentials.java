package org.jenkinsci.test.acceptance.plugins.credentials;

import java.util.concurrent.Callable;

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

    public ManagedCredentials(Jenkins j) {
        super(j, j.url("credentials/"));
    }

    /**
     * Adds a new credential and bind it to the page ae object.
     */
    public <T extends Credential> T add(Class<T> type) {
        addButton.selectDropdownMenu(type);

        String path = last(by.xpath("//div[@name='credentials']")).getAttribute("path");

        return newInstance(type, this, path);
    }

    /**
     * Adds a new domain
     */
    public Domain addDomain() {
        addDomainButton.click();
       
        // Wait for element present to avoid errors
        waitFor().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return last(by.xpath("//div[@name='domainCredentials']")).getAttribute("path") != null;
            }
        });
        String path = last(by.xpath("//div[@name='domainCredentials']")).getAttribute("path");
        
        return newInstance(Domain.class, this, path);
    }
    
    /**
     * Gets a credential by the credential id
     */
    public <T extends Credential> T get(Class<T> type, String id) {
        String path = findIfNotVisible(by.input(id)).findElement(by.ancestor("div")).getAttribute("path");
        return newInstance(type, this, path);
    }

    public <T extends Credential> T get(Class<T> type, String domainName, String id) {
        // Find domain div
        String path = findIfNotVisible(by.input(domainName))
                .findElement(by.ancestor("div"))
                // Find credential div inside domain div
               .findElement(by.input(id))
               .findElement(by.ancestor("div"))
               .getAttribute("path");
        
        return newInstance(type, this, path);
    }
}
