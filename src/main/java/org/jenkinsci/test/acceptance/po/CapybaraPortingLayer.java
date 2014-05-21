package org.jenkinsci.test.acceptance.po;

import javax.inject.Inject;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;

import org.jenkinsci.test.acceptance.ByFactory;
import org.jenkinsci.test.acceptance.utils.ElasticTime;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.google.common.base.Joiner;
import com.google.inject.Injector;

import static java.util.Arrays.*;

/**
 * For assisting porting from Capybara.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("CdiManagedBeanInconsistencyInspection")
public class CapybaraPortingLayer extends Assert {
    /**
     * {@link WebDriver} that subtypes use to talk to the server.
     */
    @Inject
    protected WebDriver driver;

    /**
     * Access to the rest of the world.
     */
    @Inject
    public Injector injector;

    protected static final ElasticTime time = new ElasticTime();

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
     * Select radio button by its name, id, or label text.
     */
    public WebElement choose(String locator) {
        WebElement e = find(by.radioButton(locator));
        e.click();
        return e;
    }

    /**
     * Wait until the element that matches the given selector appears.
     */
    public WebElement waitFor(final By selector, final int timeoutSec) {
        return waitForCond(new Callable<WebElement>() {
            @Override public WebElement call() {
                try {
                    return find(selector);
                } catch (NoSuchElementException e) {
                    return null;
                }
            }
        }, timeoutSec);
    }

    public WebElement waitFor(final By selector) {
        return waitFor(selector, 30);
    }

    /**
     * Repeated evaluate the given predicate until it returns true.
     *
     * If it times out, an exception will be thrown.
     */
    public <T> T waitForCond(Callable<T> block, int timeoutSec) {
        try {
            long endTime = System.currentTimeMillis() + time.seconds(timeoutSec);
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
     *
     * @throws NoSuchElementException
     *      if the element is not found.
     * @see #getElement(By)         if you don't want to see an exception
     */
    public WebElement find(By selector) {
        try {
            long endTime = System.currentTimeMillis() + time.seconds(1);
            while (System.currentTimeMillis() <= endTime) {
                WebElement e = driver.findElement(selector);
                if (isDisplayed(e))
                    return e;

                for (WebElement f : driver.findElements(selector)) {
                    if (isDisplayed(f))
                        return f;
                }

                // give a bit more chance for the element to become visible
                sleep(100);
            }

            throw new NoSuchElementException("Unable to locate visible "+selector+" in "+driver.getCurrentUrl());
        } catch (NoSuchElementException x) {
            // this is often the best place to set a breakpoint
            throw new NoSuchElementException("Unable to locate "+selector+" in "+driver.getCurrentUrl(),x);
        }
    }

    /**
     * Consider stale elements not displayed.
     */
    private boolean isDisplayed(WebElement e) {
        try {
            return e.isDisplayed();
        } catch (StaleElementReferenceException _) {
            return false;
        }
    }

    /**
     * Works like {@link #find(By)} but instead of throwing an exception,
     * this method returns null.
     */
    public WebElement getElement(By selector) {
        // use all so that the breakpoint in find() method stays useful
        List<WebElement> all = all(selector);
        if (all.isEmpty())  return null;
        return all.get(0);
    }

    public void fillIn(String formFieldName, Object value) {
        WebElement e = find(By.name(formFieldName));
        e.clear();
        e.sendKeys(value.toString());
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

    /**
     * Thread.sleep that masks exception.
     */
    public static void sleep(long ms) {
        try {
            Thread.sleep(time.milliseconds(ms));
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }


    /**
     * Finds matching constructor and invoke it.
     *
     * This is often useful for binding {@link PageArea} by taking the concrete type as a parameter.
     */
    protected <T> T newInstance(Class<T> type, Object... args) {
        try {
            OUTER:
            for (Constructor<?> c : type.getConstructors()) {
                Class<?>[] pts = c.getParameterTypes();
                if (pts.length!=args.length)    continue;
                for (int i=0; i<pts.length; i++) {
                    if (args[i]!=null && !pts[i].isInstance(args[i]))
                        continue OUTER;
                }

                return type.cast(c.newInstance(args));
            }

            throw new AssertionError("No matching constructor found in "+type+": "+ asList(args));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to invoke a constructor of "+type, e);
        }
    }

    protected <T> T findCaption(Class<?> type, Finder<T> call) {
        String[] captions = type.getAnnotation(Describable.class).value();

        RuntimeException cause = new NoSuchElementException(
                "None of the captions exists: " + Joiner.on(", ").join(captions)
        );
        for (String caption: captions) {
            try {
                T out = call.find(caption);
                if (out != null) return out;
            } catch (RuntimeException ex) {
                cause = ex;
            }
        }

        throw cause;
    }

    protected abstract class Finder<R> {
        protected final CapybaraPortingLayer outer = CapybaraPortingLayer.this;
        protected abstract R find(String caption);
    }

    protected abstract class Resolver extends Finder<Object> {
        @Override protected final Object find(String caption) {
            resolve(caption);
            return this;
        }

        protected abstract void resolve(String caption);
    }
}
