package org.jenkinsci.test.acceptance.po

import com.google.inject.Injector
import geb.Browser
import org.openqa.selenium.WebDriver

/**
 * The Page class combines the geb page object {@link geb.Page} with the
 * {@link org.jenkinsci.test.acceptance.po.PageObject} of the jekins ci test harness.
 *
 * Page Objects of groovy tests inherit from this class to get the full power of geb framework.
 *
 * @author christian.fritz
 */
class Page extends geb.Page {

    Page(Injector injector) {
        super()
        init(new Browser(driver: injector.getInstance(WebDriver.class)))
    }
}
