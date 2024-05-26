package org.jenkinsci.test.acceptance.plugins.credentials;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import java.net.URL;
import org.jenkinsci.test.acceptance.po.ConfigurablePageObject;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.openqa.selenium.NoSuchElementException;


public class DomainPage extends ConfigurablePageObject {

    private static final String SYSTEM_STORE_URL = "credentials/store/system";
    private static final String CONFIGURE_URL = "configure";

    private final String domainName;

    public DomainPage(Jenkins j) {
        super(j, j.url(SYSTEM_STORE_URL + "/newDomain"));
        this.domainName = null;
    }

    public DomainPage(Jenkins j, String domain) {
        super(j, j.url(String.format("%s/domain/%s/", SYSTEM_STORE_URL, domain)));
        this.domainName = domain;
    }

    public Domain addDomain() {
        String path = find(by.name("newDomain")).getAttribute("path");

        return newInstance(Domain.class, this, path);
    }

    @Override
    public URL getConfigUrl() {
        return url(CONFIGURE_URL);
    }

    @Override
    public void save() {
        if (this.onDomainConfigurationPage()) {
            clickButton("Save");
        } else {
            try {
                clickButton("Create");
            } catch (NoSuchElementException e) {
                // prior to credentials:1105.vb_4e24a_c78b_81 once it makes it to LTS remove fallback
                clickButton("OK");
            }
        }

        assertThat(driver, not(hasContent("This page expects a form submission")));
    }

    private boolean onDomainConfigurationPage() {
        return this.domainName != null && driver.getCurrentUrl().contains(String.format("%s/domain/%s/%s", SYSTEM_STORE_URL, this.domainName, CONFIGURE_URL));
    }

    public void delete() {
        visit(url("delete"));
        waitFor(by.button("Yes"));
        clickButton("Yes");
    }

}
