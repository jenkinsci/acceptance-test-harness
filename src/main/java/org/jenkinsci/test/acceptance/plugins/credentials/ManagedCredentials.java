package org.jenkinsci.test.acceptance.plugins.credentials;

import java.util.List;
import java.util.concurrent.Callable;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.openqa.selenium.WebElement;

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
     * Adds a new credential and bind it to the page object.
     */
    public <T extends Credential> T add(Class<T> type) {
        addButton.selectDropdownMenu(type);

        List<WebElement> globalCredentials = find(by.path("/domainCredentials")).findElements(by.name("credentials"));
        String path = globalCredentials.get(globalCredentials.size() - 1).getAttribute("path");

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
}
