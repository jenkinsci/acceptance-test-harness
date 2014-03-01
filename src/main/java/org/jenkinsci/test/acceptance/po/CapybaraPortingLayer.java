package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.cucumber.By2;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;

import static org.jenkinsci.test.acceptance.cucumber.By2.*;

/**
 * For assisting porting from Capybara.
 *
 * @author Kohsuke Kawaguchi
 */
public class CapybaraPortingLayer extends Assert {
    @Inject
    protected WebDriver driver;

    /**
     * Access to the rest of the world.
     */
    @Inject
    protected Injector injector;

    /**
     * Some subtypes are constructed via Guice, in which case injection is done by outside this class.
     * The injector parameter should be null for that case.
     *
     * Some subtypes are constructed programmatically. In that case, non-null injector must be supplied.
     */
    public CapybaraPortingLayer(Injector injector) {
        this.injector = injector;
        if (injector!=null)
            injector.injectMembers(this);
    }

    /**
     * Navigates the browser to the page.
     *
     * @path url
     *      URL relative to the context path of Jenkins, such as "/about" or
     *      "/job/foo/configure".
     */
    protected final WebDriver visit(URL url) {
        driver.get(url.toExternalForm());
        return driver;
    }

    public void clickButton(String text) {
        driver.findElement(By.xpath("//button[text()='" + text + "']")).click();
    }

    /**
     * @deprecated
     *      {@link WebDriver#findElement(By)} already does waiting, so this isn't needed.
     */
    public WebElement waitFor(By selector) {
        return driver.findElement(selector);
    }

    public WebElement find(By selector) {
        return driver.findElement(selector);
    }

    public void fillIn(String formFieldName, String value) {
        driver.findElement(By.name(formFieldName)).sendKeys(value);
    }

    /**
     * Checks the checkbox.
     */
    public void check(WebElement e) {
        if (!e.isSelected())
            e.click();
    }

    public List<WebElement> all(By selector) {
        return driver.findElements(selector);
    }

    /**
     * Picks up the last element that matches given selector.
     */
    public WebElement last(By selector) {
        List<WebElement> l = driver.findElements(selector);
        return l.get(l.size()-1);
    }

    /**
     * Executes JavaScript.
     */
    public Object executeScript(String javaScript, Object... args) {
        return ((JavascriptExecutor)driver).executeScript(javaScript,args);
    }

    /**
     * Given a menu button that shows a list of build steps, select the right item from the menu
     * to insert the said build step.
     */
    public void selectDropdownMenu(String displayName, WebElement menuButton) throws Exception {
        menuButton.click();

        // With enough implementations registered the one we are looking for might
        // require scrolling in menu to become visible. This dirty hack stretch
        // yui menu so that all the items are visible.
        executeScript(""+
            "YAHOO.util.Dom.batch("+
            "    document.querySelector('.yui-menu-body-scrolled'),"+
            "    function (el) {"+
            "        el.style.height = 'auto';"+
            "        YAHOO.util.Dom.removeClass(el, 'yui-menu-body-scrolled');"+
            "    }"+
            ");"
        );

        clickLink(displayName);
        Thread.sleep(1000);
    }

    /**
     * @param locator
     *      Text, ID, or link.
     */
    public void clickLink(String locator) {
        find(link(locator)).click();
    }

    public void check(String locator) {
        check(find(By2.checkbox(locator)));
    }

}
