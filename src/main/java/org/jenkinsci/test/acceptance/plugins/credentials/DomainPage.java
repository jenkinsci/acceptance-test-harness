package org.jenkinsci.test.acceptance.plugins.credentials;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import java.net.URL;
import org.jenkinsci.test.acceptance.po.ConfigurablePageObject;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.selenium.Scroller;

public class DomainPage extends ConfigurablePageObject {

    private static final String SYSTEM_STORE_URL = "credentials/store/system";

    public DomainPage(Jenkins j) {
        super(j, j.url(SYSTEM_STORE_URL));
    }

    public DomainPage(Jenkins j, String domain) {
        super(j, j.url(String.format("%s/domain/%s/", SYSTEM_STORE_URL, domain)));
    }

    public Domain addDomain() {
        clickButton("Add domain");

        new Scroller(driver).disableStickyElements();

        waitFor(by.xpath("//form[contains(@name, 'newDomain')]"), 10);

        String path = find(by.name("newDomain")).getAttribute("path");

        return newInstance(Domain.class, this, path);
    }

    @Override
    public URL getConfigUrl() {
        return url;
    }

    @Override
    public void configure() {
        visit(url);

        clickButton("Update domain");

        new Scroller(driver).disableStickyElements();

        waitFor(by.xpath("//form[contains(@name, '" + getFormName() + "')]"), 10);
        waitFor(SAVE_BUTTON, 5);
    }

    @Override
    public void save() {
        if (this.onDomainConfigurationPage()) {
            clickButton("Save");
        } else {
            clickButton("Create");
        }

        assertThat(driver, not(hasContent("This page expects a form submission")));
    }

    private boolean onDomainConfigurationPage() {
        return !driver.findElements(
                by.xpath(String.format("//form[contains(@name, '%s')]", getFormName()))
        ).isEmpty();
    }

    public void delete() {
        visit(url);

        clickButton("More actions");

        clickLink("Delete domain");
        clickButton("Yes");
    }
}
