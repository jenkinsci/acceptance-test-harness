package org.jenkinsci.test.acceptance.plugins.credentials;

import java.net.URL;

import org.jenkinsci.test.acceptance.po.ConfigurablePageObject;
import org.jenkinsci.test.acceptance.po.Jenkins;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;


public class DomainPage extends ConfigurablePageObject {
    public DomainPage(Jenkins j) {
        super(j, j.url("credentials/store/system/newDomain"));
    }

    public DomainPage(Jenkins j, String domain) {
        super(j, j.url(String.format("credentials/store/system/domain/%s/", domain)));
    }

    public Domain addDomain() {
        String path = find(by.name("newDomain")).getAttribute("path");

        return newInstance(Domain.class, this, path);
    }

    @Override
    public URL getConfigUrl() {
        return url("configure");
    }

    @Override
    public void save() {
        if (driver.getCurrentUrl().contains("configure")) {
            clickButton("Save");
        } else {
            clickButton("OK");
        }

        assertThat(driver, not(hasContent("This page expects a form submission")));
    }

    public void delete() {
        visit(url("delete"));
        elasticSleep(1000);
        clickButton("Yes");
    }

}
