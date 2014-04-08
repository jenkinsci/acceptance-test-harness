package org.jenkinsci.test.acceptance.plugins.credentials;

import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Jenkins;

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
        String sut_type = type.getAnnotation(Describable.class).value();

        selectDropdownMenu(sut_type,
            find(by.path("/domainCredentials/hetero-list-add[credentials]")));

        String path = last(by.xpath("//div[@name='credentials']")).getAttribute("path");

        return newInstance(type, this, path);
    }
}
