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

    public final Control url = control("locations/remote");
    public final Control local = control("locations/local");
    public final Control checkoutStrategy = control(by.xpath("//td[@class='setting-name' and text()='%s']/../td[@class='setting-main']/select", "Check-out Strategy"));
    public final Control credentials = control("locations/credentialsId");

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
            sleep(1000);
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


    public SubversionScm(Job job, String path) {
        super(job, path);
    }


}
