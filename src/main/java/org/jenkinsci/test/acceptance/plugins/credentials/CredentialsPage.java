package org.jenkinsci.test.acceptance.plugins.credentials;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import java.net.MalformedURLException;
import java.net.URL;
import org.jenkinsci.test.acceptance.po.ConfigurablePageObject;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Folder;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CredentialsPage extends ConfigurablePageObject {
    public final Control addButton = control(by.xpath("//select[contains(@class, 'setting-input dropdownList')] | "
            + "//select[contains(@class, 'jenkins-select__input dropdownList')]"));
    private URL configUrl;
    private URL deleteUrl;

    /**
     * Create a new Credential
     */
    public CredentialsPage(Jenkins j, String domainName) {
        super(j, j.url("credentials/store/system/domain/" + domainName));
    }

    /**
     * Create a new Credential scoped to a Folder
     */
    public CredentialsPage(Folder f, String domainName) {
        super(f, f.url("credentials/store/folder/domain/" + domainName));
    }

    /**
     * Create a new personal Credential
     */
    public CredentialsPage(Jenkins j, String domainName, String userName) {
        super(j, j.url(String.format("user/%s/credentials/store/user/domain/%s", userName, domainName)));
    }

    public <T extends Credential> T add(Class<T> type) {
        WebElement radio = findCaption(type, caption -> {
            for (WebElement webElement : all(by.css(".jenkins-choice-list__item__label"))) {
                if (webElement.getText().equals(caption)) {
                    webElement.click();
                    return webElement.findElement(by.xpath("./../input"));
                }
            }
            return null;
        });

        String path = radio.getAttribute("path");

        WebElement nextButton = find(by.id("cr-dialog-next"));
        nextButton.click();

        return newInstance(type, this, path);
    }

    @Override
    public URL getConfigUrl() {
        return configUrl;
    }

    public void setConfigUrl(String url) throws MalformedURLException {
        configUrl = new URL(url + "/update");
        deleteUrl = new URL(url + "/delete");
    }

    public void create() {
        find(by.id("cr-dialog-submit")).click();
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

    @Override
    public WebDriver open() {
        WebDriver wd = super.open();

        clickButton("Add Credentials");
        // Selenium will execute the next step before the options have loaded if we don't wait for them
        waitFor(by.css(".jenkins-choice-list__item__label"));
        return wd;
    }

    @Override
    public String getFormName() {
        return "update";
    }
}
