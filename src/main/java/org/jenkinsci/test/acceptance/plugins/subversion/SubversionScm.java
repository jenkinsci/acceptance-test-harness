package org.jenkinsci.test.acceptance.plugins.subversion;

import org.jenkinsci.test.acceptance.po.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Encapsulates the PageArea of the Subversion SCM
 *
 * @author Kohsuke Kawaguchi
 * @author Matthias Karl
 */
@Describable("Subversion")
public class SubversionScm extends Scm {
    public static final String ALWAYS_FRESH_COPY = "Always check out a fresh copy";
    public static final String CLEAN_CHECKOUT = "Emulate clean checkout by first deleting unversioned/ignored files, then 'svn update'";
    private static final String CHECK_OUT_STRATEGY = "Check-out Strategy";
    private static final String REPOSITORY_BROWSER = "Repository browser";

    public final Control url = control("locations/remote");
    public final Control btAdvanced = control(by.xpath("//td[table/tbody/tr/td[@class='setting-main']/input[@name='_.ignoreDirPropChanges']]/div[@class='advancedLink']//button"));
    public final Control local = control("locations/local");
    public final Control checkoutStrategy = control(by.xpath("//td[@class='setting-name' and text()='%s']/../td[@class='setting-main']/select | " +
            "//div[contains(@class, 'setting-name') and normalize-space(text())='%s']/../div[@class='setting-main']/select | " +
            "//div[contains(@class, 'jenkins-form-label') and normalize-space(text())='%s']/../div[@class='jenkins-select']/select",
            CHECK_OUT_STRATEGY, CHECK_OUT_STRATEGY, CHECK_OUT_STRATEGY));
    public final Control credentials = control("locations/credentialsId");
    public final Control repositoryBrowser = control(by.xpath("//td[@class='setting-name' and text()='%s']/../td[@class='setting-main']/select | " +
            "//div[contains(@class, 'setting-name') and normalize-space(text())='%s']/../div[@class='setting-main']/select | " +
            "//div[contains(@class, 'jenkins-form-label') and normalize-space(text())='%s']/../div[@class='jenkins-select']/select",

            REPOSITORY_BROWSER, REPOSITORY_BROWSER, REPOSITORY_BROWSER));

    /**
     * Opens the SVNPlugin credential page for protected repositories.
     * Only for plugin version 1.54 and older.
     *
     * @param type child of SubversionCredential.class
     * @param <T>  child of SubversionCredential.class
     * @return PageObject of the CredentialPage
     * @throws SubversionPluginTestException if Url to credential page is not found or malformed.
     */
    @Deprecated
    public <T extends PageObject> T getCredentialPage(Class<T> type) throws SubversionPluginTestException {
        //click into a different field to trigger the Url-Check
        this.local.click();
        URL urlOfCredentialPage = null;
        WebElement linkToCredentialPage;
        String urlString = null;
        try {
            elasticSleep(1000);
            linkToCredentialPage = this.find(by.link("enter credential"));
            urlString = linkToCredentialPage.getAttribute("href");
            urlOfCredentialPage = new URL(urlString);
            linkToCredentialPage.click();
        } catch (NoSuchElementException e) {
            SubversionPluginTestException.throwRepoMayNotBeProtected(e);
        } catch (MalformedURLException e) {
            SubversionPluginTestException.throwMalformedURL(e, urlString);
        }
        return this.newInstance(type, this.injector, urlOfCredentialPage, driver.getWindowHandle());
    }

    public <T extends SvnRepositoryBrowser> T useRepositoryBrowser(final Class<T> type) {
        repositoryBrowser.selectDropdownMenuAlt(type);
        return this.newInstance(type, this, this.getPage().url(getPath("[1]")));
    }

    public SubversionSvmAdvanced advanced() {
        btAdvanced.click();
        return this.newInstance(SubversionSvmAdvanced.class, this.getPage(), this.getPage().url);
    }

    public SubversionScm(Job job, String path) {
        super(job, path);
    }
}
