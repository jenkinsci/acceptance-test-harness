package org.jenkinsci.test.acceptance.plugins.credentials;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import java.net.MalformedURLException;
import java.net.URL;
import org.jenkinsci.test.acceptance.po.ConfigurablePageObject;
import org.jenkinsci.test.acceptance.po.Folder;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.selenium.Scroller;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class CredentialsPage extends ConfigurablePageObject {
    private URL configUrl;

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
        waitFor(by.id("cr-dialog-submit"));
        new Scroller(driver).disableStickyElements();

        // If the credential has a scope field, wait for the options to load.
        // Otherwise, sometimes the JS doesn't run fast enough to fill in the options
        WebElement scope = getElement(by.name("_.scope"));
        if (scope != null) {
            waitFor(scope).until(el -> !el.findElements(by.tagName("option")).isEmpty());
        }

        return newInstance(type, this, path);
    }

    @Override
    public URL getConfigUrl() {
        return configUrl;
    }

    public void setConfigUrl(String url) throws MalformedURLException {
        configUrl = new URL(url);
    }

    public void create() {
        find(by.id("cr-dialog-submit")).click();
        try {
            assertThat(driver, not(hasContent("This page expects a form submission")));
        } catch (UnhandledAlertException e) {
            // TODO seems to occur on at least up to 2.541.1 but doesn't occur on 2.547
            // can't see what the alert is, selenium goes too fast and if you pause it there's no issue
            sleep(100);
            assertThat(driver, not(hasContent("This page expects a form submission")));
        }
    }

    public void delete() {
        if (driver.getCurrentUrl().equals(getConfigUrl().toExternalForm())) {
            return;
        }
        visit(configUrl);
        find(by.id("more-credential-actions")).click();

        clickLink("Delete credential");
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
    public void configure() {
        if (!driver.getCurrentUrl().equals(getConfigUrl().toExternalForm())) {
            visit(getConfigUrl());
        }

        clickButton("Update credential");

        new Scroller(driver).disableStickyElements();

        waitFor(by.xpath("//form[contains(@name, '" + getFormName() + "')]"), 10);
        waitFor(SAVE_BUTTON, 5);
    }

    @Override
    public String getFormName() {
        return "update";
    }
}
