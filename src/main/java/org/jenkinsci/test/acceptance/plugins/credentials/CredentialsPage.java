package org.jenkinsci.test.acceptance.plugins.credentials;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jenkinsci.test.acceptance.po.*;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

public class CredentialsPage extends ConfigurablePageObject {
    public final Control addButton = control(by.xpath("//select[contains(@class, 'setting-input dropdownList')]"));
    private URL configUrl;
    private URL deleteUrl;

    /**
     * Create a new Credential
     * @param j
     * @param domainName
     */
    public CredentialsPage(Jenkins j, String domainName) {
        super(j, j.url("credentials/store/system/domain/"+domainName+"/newCredentials"));
    }

    public <T extends Credential> T add(final Class<T> type) {
        String path = createPageArea("credentials", new Runnable() {
            @Override public void run() {
                addButton.selectDropdownMenuAlt(type);
            }
        });
        return newInstance(type, this, path);
    }

    @Override
    public URL getConfigUrl() {
        return configUrl;
    }


    public void setConfigUrl(String url) throws MalformedURLException {
        configUrl = new URL(url+"/update");
        deleteUrl = new URL(url+"/delete");
    }

    public void create(){
        clickButton("OK");
        assertThat(driver, not(hasContent("This page expects a form submission")));
    }

    public void delete() {
        if (driver.getCurrentUrl().equals(getConfigUrl().toExternalForm())) {
            return;
        }
        visit(deleteUrl);
        elasticSleep(1000); // configure page requires some time to load
        clickButton("Yes");
    }
}
