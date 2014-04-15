package org.jenkinsci.test.acceptance.plugins.subversion;

import org.jenkinsci.test.acceptance.po.*;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Kohsuke Kawaguchi
 */
@Describable("Subversion")
public class SubversionScm extends Scm {
    public static final String ALWAYS_FRESH_COPY = "Always check out a fresh copy";

    public final Control url = control("locations/remote");
    public final Control local = control("locations/local");
    public final Control checkoutStrategy = control(by.xpath("//td[@class='setting-name' and text()='%s']/../td[@class='setting-main']/select", "Check-out Strategy"));

    public <T extends PageObject> T getCredentialPage(Class<T> type) throws MalformedURLException {
        //click into a different field to trigger the Url-Check
        this.local.click();
        //
        final WebElement linkToCredentialPage = this.find(by.link("enter credential"));
        URL urlOfCredentialPage = new URL(linkToCredentialPage.getAttribute("href"));
        linkToCredentialPage.click();
        return this.newInstance(type, this.injector, urlOfCredentialPage, driver.getWindowHandle());
    }


    public SubversionScm(Job job, String path) {
        super(job, path);
    }



}
