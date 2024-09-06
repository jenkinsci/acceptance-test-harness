package org.jenkinsci.test.acceptance.po;

import java.util.List;
import java.util.concurrent.Callable;
import org.jenkinsci.test.acceptance.ByFactory;
import org.jenkinsci.test.acceptance.junit.Wait;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Interface for assisting porting from Capybara.
 *
 * @author christian.fritz
 */
public interface CapybaraPortingLayer {
    ByFactory by = new ByFactory();

    void clickButton(String text);

    /**
     * Select radio button by its name, id, or label text.
     */
    WebElement choose(String locator);

    <T> Wait<T> waitFor(T subject);

    Wait<CapybaraPortingLayer> waitFor();

    /**
     * Wait until the element that matches the given selector appears.
     */
    WebElement waitFor(By selector, int timeoutSec);

    /**
     * Wait until the element that matches the given selector appears.
     */
    WebElement waitFor(By selector);

    /**
     * Repeated evaluate the given predicate until it returns true.
     * <p>
     * If it times out, an exception will be thrown.
     *
     * @param timeoutSec 0 if left to the default value
     */
    @Deprecated
    <T> T waitForCond(Callable<T> block, int timeoutSec);

    @Deprecated
    <T> T waitForCond(Callable<T> block);

    /** Wait until a matcher matches. */
    <MatcherT, SubjectT extends MatcherT> void waitFor(
            SubjectT item, org.hamcrest.Matcher<MatcherT> matcher, int timeoutSec);

    /**
     * Returns the first visible element that matches the selector.
     *
     * @throws org.openqa.selenium.NoSuchElementException if the element is not found.
     * @see #getElement(org.openqa.selenium.By)         if you don't want to see an exception
     */
    WebElement find(By selector);

    /**
     * Returns the first element that matches the selector even if not visible.
     *
     * @throws org.openqa.selenium.NoSuchElementException if the element is not found.
     * @see #getElement(org.openqa.selenium.By)         if you don't want to see an exception
     */
    WebElement findIfNotVisible(By selector);

    /**
     * Works like {@link #find(org.openqa.selenium.By)} but instead of throwing an exception,
     * this method returns null.
     */
    WebElement getElement(By selector);

    void fillIn(String formFieldName, Object value);

    /**
     * Checks the checkbox.
     */
    void check(WebElement e);

    /**
     * Sets the state of the checkbox to the specified value.
     */
    void check(WebElement e, boolean state);

    /**
     * Sends a blur event to the provided element
     */
    void blur(WebElement e);

    /**
     * Finds all the elements that match the selector.
     * <p>
     * Note that this method inherits the same restriction of the {@link org.openqa.selenium.WebDriver#findElements(org.openqa.selenium.By)},
     * in that its execution is not synchronized with the JavaScript execution of the browser.
     * <p>
     * For example, if you click something that's expected to populate additional DOM elements,
     * and then call {@code all()} to find them, then all() can execute before those additional DOM elements
     * are populated, thereby failing to find the elements you are looking for.
     * <p>
     * In contrast, {@link #find(org.openqa.selenium.By)} do not have this problem, because it waits until the element
     * that matches the criteria appears.
     * <p>
     * So if you are using this method, think carefully. Perhaps you can use {@link #find(org.openqa.selenium.By)} to
     * achieve what you are looking for (by making the query more specific), or perhaps you can combine
     * this with {@link #waitForCond(java.util.concurrent.Callable)} so that if you don't find the elements you are looking for
     * in the list, you'll retry.
     */
    List<WebElement> all(By selector);

    /**
     * Picks up the last visible element that matches given selector.
     */
    WebElement last(By selector);

    /**
     * Picks up the last visible element that matches given selector.
     */
    WebElement lastIfNotVisible(By selector);

    /**
     * Executes JavaScript.
     */
    Object executeScript(String javaScript, Object... args);

    /**
     * @param locator Text, ID, or link.
     */
    void clickLink(String locator);

    /**
     * Checks the specified checkbox.
     */
    void check(String locator);

    /**
     * Confirms an alert giving it some time to appear
     *
     * @param timeout Maximum time to wait for the alert to appear, in seconds
     * @deprecated Use {@link CapybaraPortingLayer#runThenConfirmAlert(Runnable, int)} and provide the runnable that triggers the alert.
     */
    @Deprecated
    void confirmAlert(int timeout);

    /**
     * Do something that triggers an alert then giving it some time to appear
     *
     * @param runnable Something that will trigger the alert
     * @param timeout Maximum time to wait for the alert to appear, in seconds
     */
    void runThenConfirmAlert(Runnable runnable, int timeout);

    /**
     * Get all text of the page including markup.
     */
    String getPageSource();
}
