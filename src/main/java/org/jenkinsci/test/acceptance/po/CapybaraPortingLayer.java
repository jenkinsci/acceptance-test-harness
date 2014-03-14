package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;
import org.jenkinsci.test.acceptance.ByFactory;
import org.junit.Assert;
import org.openqa.selenium.*;

import javax.inject.Inject;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * For assisting porting from Capybara.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class CapybaraPortingLayer extends Assert {
    @Inject
    protected WebDriver driver;

    /**
     * Access to the rest of the world.
     */
    @Inject
    protected Injector injector;

    public static final ByFactory by = new ByFactory();

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
        WebElement e = find(by.button(text));
        e.click();
    }

    /**
     * Wait until the element that matches the given selector appears.
     */
    public WebElement waitFor(final By selector) {
        return waitForCond(new Callable<WebElement>() {
            public WebElement call() {
                try {
                    return find(selector);
                } catch (NoSuchElementException e) {
                    return null;
                }
            }
        });
    }

    /**
     * Repeated evaluate the given predicate until it returns true.
     *
     * If it times out, an exception will be thrown.
     */
    public <T> T waitForCond(Callable<T> block, int timeoutSec) {
        try {
            long endTime = System.currentTimeMillis()+ TimeUnit.SECONDS.toMillis(timeoutSec);
            while (System.currentTimeMillis()<endTime) {
                T v = block.call();
                if (isTrueish(v))
                    return v;
                sleep(1000);
            }
            throw new TimeoutException("Failed to wait for condition "+block);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new Error("Failed to wait for condition "+block,e);
        }
    }

    private boolean isTrueish(Object v) {
        if (v instanceof Boolean)   return (Boolean)v;
        return v!=null;
    }

    public <T> T waitForCond(Callable<T> block) {
        return waitForCond(block,30);
    }


    /**
     * Returns the first visible element that matches the selector.
     */
    public WebElement find(By selector) {
        try {
            WebElement e = driver.findElement(selector);
            if (e.isDisplayed())
                return e;

            for (WebElement f : driver.findElements(selector)) {
                if (f.isDisplayed())
                    return f;
            }
            return e;   // hmm, not sure what to return here!
        } catch (NoSuchElementException x) {
            throw new NoSuchElementException("Unable to locate "+selector+" in "+driver.getCurrentUrl(),x);
        }
    }

    /**
     * Works like {@link #find(By)} but instead of throwing an exception,
     * this method returns null.
     */
    public WebElement getElement(By selector) {
        try {
            return find(selector);
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public void fillIn(String formFieldName, String value) {
        find(By.name(formFieldName)).sendKeys(value);
    }

    /**
     * Checks the checkbox.
     */
    public void check(WebElement e) {
        if (!e.isSelected())
            e.click();
    }

    /**
     * Sets the state of the checkbox to the specified value.
     */
    public void check(WebElement e, boolean state) {
        if (e.isSelected()!=state)
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
    public void selectDropdownMenu(String displayName, WebElement menuButton) {
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
        sleep(1000);
    }

    /**
     * @param locator
     *      Text, ID, or link.
     */
    public void clickLink(String locator) {
        find(by.link(locator)).click();
    }

    /**
     * Checks the specified checkbox.
     */
    public void check(String locator) {
        check(find(by.checkbox(locator)));
    }

    public void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }
}
