package org.jenkinsci.test.acceptance.po;

import javax.inject.Inject;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.hamcrest.StringDescription;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.Wait;
import org.jenkinsci.test.acceptance.utils.ElasticTime;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
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
public class CapybaraPortingLayerImpl implements CapybaraPortingLayer {
    /**
     * {@link org.openqa.selenium.WebDriver} that subtypes use to talk to the server.
     */
    @Inject
    protected WebDriver driver;

    /**
     * Access to the rest of the world.
     */
    @Inject
    public Injector injector;

    protected static final ElasticTime time = new ElasticTime();

    /**
     * Some subtypes are constructed via Guice, in which case injection is done by outside this class.
     * The injector parameter should be null for that case.
     * <p/>
     * Some subtypes are constructed programmatically. In that case, non-null injector must be supplied.
     */
    public CapybaraPortingLayerImpl(Injector injector) {
        this.injector = injector;
        if (injector != null) {
            injector.injectMembers(this);
        }
    }

    /**
     * Navigates the browser to the page.
     *
     * @param url URL relative to the context path of Jenkins, such as "/about" or "/job/foo/configure".
     */
    protected final WebDriver visit(URL url) {
        driver.get(url.toExternalForm());
        return driver;
    }

    @Override
    public void clickButton(String text) {
        WebElement e = find(by.button(text));
        e.click();
    }

    /**
     * Select radio button by its name, id, or label text.
     */
    @Override
    public WebElement choose(String locator) {
        WebElement e = find(by.radioButton(locator));
        e.click();
        return e;
    }

    /**
     * Default waiting object configured with default timing.
     *
     * @see {@link Wait}
     */
    public <T> Wait<T> waitFor(T subject) {
        return new Wait<T>(subject)
                .pollingEvery(time.milliseconds(500), TimeUnit.MILLISECONDS)
                .withTimeout(time.seconds(120), TimeUnit.SECONDS)
        ;
    }

    public Wait<CapybaraPortingLayer> waitFor() {
        return waitFor((CapybaraPortingLayer) this);
    }

    /**
     * Wait until the element that matches the given selector appears.
     */
    @Override
    public WebElement waitFor(final By selector, final int timeoutSec) {
        return waitFor(this).withMessage("Element matching %s is present", selector)
                .withTimeout(timeoutSec, TimeUnit.SECONDS)
                .ignoring(NoSuchElementException.class)
                .until(new Callable<WebElement>() {
                    @Override public WebElement call() {
                        return find(selector);
                    }
        });
    }

    @Override
    public WebElement waitFor(final By selector) {
        return waitFor(this).withMessage("Element matching %s is present", selector)
                .ignoring(NoSuchElementException.class)
                .until(new Callable<WebElement>() {
                    @Override public WebElement call() {
                        return find(selector);
                    }
        });
    }

    /**
     * Repeated evaluate the given predicate until it returns true.
     * <p/>
     * If it times out, an exception will be thrown.
     */
    @Override @Deprecated
    public <T> T waitForCond(Callable<T> block, int timeoutSec) {
        return waitFor(this).withTimeout(timeoutSec, TimeUnit.SECONDS).until(block);
    }

    @Override @Deprecated
    public <T> T waitForCond(Callable<T> block) {
        return waitFor(this).until(block);
    }

    @Override public <T> void waitFor(final T item, final org.hamcrest.Matcher<T> matcher, int timeout) {
        StringDescription desc = new StringDescription();
        matcher.describeTo(desc);
        waitFor(driver).withMessage(desc.toString())
                .withTimeout(timeout, TimeUnit.SECONDS)
                .until(new Wait.Predicate<Boolean>() {
                    @Override public Boolean apply() {
                        return matcher.matches(item);
                    }

                    @Override public String diagnose(Throwable lastException, String message) {
                        StringDescription desc = new StringDescription();
                        matcher.describeMismatch(item, desc);
                        return desc.toString();
                    }
        });
    }

    /**
     * Returns the first visible element that matches the selector.
     *
     * @throws org.openqa.selenium.NoSuchElementException if the element is not found.
     * @see #getElement(org.openqa.selenium.By)         if you don't want to see an exception
     */
    @Override
    public WebElement find(By selector) {
        try {
            long endTime = System.currentTimeMillis() + time.seconds(1);
            while (System.currentTimeMillis() <= endTime) {
                WebElement e = driver.findElement(selector);
                if (isDisplayed(e)) {
                    return e;
                }

                for (WebElement f : driver.findElements(selector)) {
                    if (isDisplayed(f)) {
                        return f;
                    }
                }

                // give a bit more chance for the element to become visible
                elasticSleep(100);
            }

            throw new NoSuchElementException("Unable to locate visible " + selector + " in " + driver.getCurrentUrl());
        } catch (NoSuchElementException x) {
            // this is often the best place to set a breakpoint
            String msg = String.format("Unable to locate %s in %s\n\n%s", selector, driver.getCurrentUrl(), driver.getPageSource());
            throw new NoSuchElementException(msg, x);
        }
    }

    /**
     * Returns the first element that matches the selector even if not visible.
     *
     * @throws org.openqa.selenium.NoSuchElementException if the element is not found.
     * @see #getElement(org.openqa.selenium.By)         if you don't want to see an exception
     */
    @Override
    public WebElement findIfNotVisible(By selector) {
        try {
            long endTime = System.currentTimeMillis() + time.seconds(1);
            WebElement e = null;
            while (System.currentTimeMillis() <= endTime) {
                e = driver.findElement(selector);

                // give a bit more chance for the element to become visible
                elasticSleep(100);

            }
            if (e == null) {
                throw new NoSuchElementException("Unable to locate visible " + selector + " in " + driver.getCurrentUrl());
            }
            return e;

        } catch (NoSuchElementException x) {
            // this is often the best place to set a breakpoint
            String msg = String.format("Unable to locate %s in %s\n\n%s", selector, driver.getCurrentUrl(), driver.getPageSource());
            throw new NoSuchElementException(msg, x);
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
     * Works like {@link #find(org.openqa.selenium.By)} but instead of throwing an exception,
     * this method returns null.
     */
    @Override
    public WebElement getElement(By selector) {
        // use all so that the breakpoint in find() method stays useful
        List<WebElement> all = all(selector);
        if (all.isEmpty()) {
            return null;
        }
        return all.get(0);
    }

    @Override
    public void fillIn(String formFieldName, Object value) {
        WebElement e = find(By.name(formFieldName));
        e.clear();
        e.sendKeys(value.toString());
    }

    /**
     * Checks the checkbox.
     */
    @Override
    public void check(WebElement e) {
        if (!e.isSelected()) {
            e.click();
        }
    }

    /**
     * Sets the state of the checkbox to the specified value.
     */
    @Override
    public void check(WebElement e, boolean state) {
        if (e.isSelected() != state) {
            e.click();
        }
    }

    /**
     * Finds all the elements that match the selector.
     * <p/>
     * <p/>
     * Note that this method inherits the same restriction of the {@link org.openqa.selenium.WebDriver#findElements(org.openqa.selenium.By)},
     * in that its execution is not synchronized with the JavaScript execution of the browser.
     * <p/>
     * <p/>
     * For example, if you click something that's expected to populate additional DOM elements,
     * and then call {@code all()} to find them, then all() can execute before those additional DOM elements
     * are populated, thereby failing to find the elements you are looking for.
     * <p/>
     * <p/>
     * In contrast, {@link #find(org.openqa.selenium.By)} do not have this problem, because it waits until the element
     * that matches the criteria appears.
     * <p/>
     * <p/>
     * So if you are using this method, think carefully. Perhaps you can use {@link #find(org.openqa.selenium.By)} to
     * achieve what you are looking for (by making the query more specific), or perhaps you can combine
     * this with {@link #waitForCond(java.util.concurrent.Callable)} so that if you don't find the elements you are looking for
     * in the list, you'll retry.
     */
    @Override
    public List<WebElement> all(By selector) {
        return driver.findElements(selector);
    }

    /**
     * Picks up the last visible element that matches given selector.
     */
    @Override
    public WebElement last(By selector) {
        find(selector); // wait until at least one is found

        // but what we want is the last one
        List<WebElement> l = driver.findElements(selector);
        return l.get(l.size() - 1);
    }

    /**
     * Picks up the last visible element that matches given selector.
     */
    @Override
    public WebElement lastIfNotVisible(By selector) {
        findIfNotVisible(selector); // wait until at least one is found

        // but what we want is the last one
        List<WebElement> l = driver.findElements(selector);
        return l.get(l.size() - 1);
    }

    /**
     * Executes JavaScript.
     */
    @Override
    public Object executeScript(String javaScript, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(javaScript, args);
    }

    /**
     * @param locator Text, ID, or link.
     */
    @Override
    public void clickLink(String locator) {
        find(by.link(locator)).click();
    }

    /**
     * Checks the specified checkbox.
     */
    @Override
    public void check(String locator) {
        check(find(by.checkbox(locator)));
    }

    /**
     * Thread.sleep that masks exception.
     */
    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    public static void elasticSleep(long ms) {
        sleep(time.milliseconds(ms));
    }

    /**
     * Finds matching constructor and invoke it.
     * <p/>
     * This is often useful for binding {@link org.jenkinsci.test.acceptance.po.PageArea} by taking the concrete type as a parameter.
     */
    protected <T> T newInstance(Class<T> type, Object... args) {
        try {
            OUTER:
            for (Constructor<?> c : type.getConstructors()) {
                Class<?>[] pts = c.getParameterTypes();
                if (pts.length != args.length) {
                    continue;
                }
                for (int i = 0; i < pts.length; i++) {
                    if (args[i] != null && !pts[i].isInstance(args[i])) {
                        continue OUTER;
                    }
                }

                return type.cast(c.newInstance(args));
            }

            throw new AssertionError("No matching constructor found in " + type + ": " + asList(args));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Failed to invoke a constructor of " + type, e);
        }
    }

    protected <T> T findCaption(Class<?> type, Finder<T> call) {
        String[] captions = type.getAnnotation(Describable.class).value();

        RuntimeException cause = new NoSuchElementException(
                "None of the captions exists: " + Joiner.on(", ").join(captions)
        );
        for (String caption : captions) {
            try {
                T out = call.find(caption);
                if (out != null) {
                    return out;
                }
            } catch (RuntimeException ex) {
                cause = ex;
            }
        }

        throw cause;
    }

    /**
     * Obtains a resource in a wrapper.
     */
    public Resource resource(String path) {
        final URL resource = getClass().getResource(path);
        if (resource == null) {
            throw new AssertionError("No such resource " + path + " for " + getClass().getName());
        }
        return new Resource(resource);
    }

    protected abstract class Finder<R> {
        protected final CapybaraPortingLayer outer = CapybaraPortingLayerImpl.this;

        protected abstract R find(String caption);
    }

    protected abstract class Resolver extends Finder<Object> {
        @Override
        protected final Object find(String caption) {
            resolve(caption);
            return this;
        }

        protected abstract void resolve(String caption);
    }
}
