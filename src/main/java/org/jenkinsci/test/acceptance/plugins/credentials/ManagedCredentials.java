package org.jenkinsci.test.acceptance.plugins.credentials;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.openqa.selenium.WebElement;

/**
 * "Manage Credentials" page.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public class ManagedCredentials extends ContainerPageObject {
    public ManagedCredentials(Jenkins j) {
        super(j, j.url("credentials/"));
    }

    /**
     * Adds a new credential and bind it to the page ae object.
     */
    public <T extends Credential> T add(Class<T> type) {
        final WebElement dropDown = find(by.path("/domainCredentials/hetero-list-add[credentials]"));

        findCaption(type, new Resolver() {
            @Override protected void resolve(String caption) {
                selectDropdownMenu(caption, dropDown);
            }
        });

        String path = last(by.xpath("//div[@name='credentials']")).getAttribute("path");

        return newInstance(type, this, path);
    }
}
