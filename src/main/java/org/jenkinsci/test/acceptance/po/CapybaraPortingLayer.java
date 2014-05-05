package org.jenkinsci.test.acceptance.po;

import java.util.List;
import java.util.concurrent.Callable;

import org.jenkinsci.test.acceptance.ByFactory;
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

    /**
     * Wait until the element that matches the given selector appears.
     */
    WebElement waitFor(By selector);

    /**
     * Repeated evaluate the given predicate until it returns true.
     * <p/>
     * If it times out, an exception will be thrown.
     */
    <T> T waitForCond(Callable<T> block, int timeoutSec);

    <T> T waitForCond(Callable<T> block);

    /**
     * Returns the first visible element that matches the selector.
     *
     * @throws org.openqa.selenium.NoSuchElementException if the element is not found.
     * @see #getElement(org.openqa.selenium.By)         if you don't want to see an exception
     */
    WebElement find(By selector);

    /**
     * Works like {@link #find(org.openqa.selenium.By)} but instead of throwing an exception, this method returns null.
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

    List<WebElement> all(By selector);

    /**
     * Picks up the last element that matches given selector.
     */
    WebElement last(By selector);

    /**
     * Executes JavaScript.
     */
    Object executeScript(String javaScript, Object... args);

    /**
     * Given a menu button that shows a list of build steps, select the right item from the menu to insert the said
     * build step.
     */
    void selectDropdownMenu(String displayName, WebElement menuButton);

    /**
     * @param locator Text, ID, or link.
     */
    void clickLink(String locator);

    /**
     * Checks the specified checkbox.
     */
    void check(String locator);

    /**
     * Thread.sleep that masks exception.
     */
    void sleep(int ms);
}
