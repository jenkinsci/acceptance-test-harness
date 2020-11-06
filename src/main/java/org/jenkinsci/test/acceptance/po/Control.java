package org.jenkinsci.test.acceptance.po;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.selenium.Scroller;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import com.google.inject.Injector;

import org.jenkinsci.test.acceptance.junit.Resource;

/**
 * Wraps a specific form element in {@link PageAreaImpl} to provide operations.
 * <p/>
 * {@link Control} is like a {@link WebElement}, but with the following key differences:
 * <p/>
 * <ul> <li>{@link Control} is late binding, and the underlying {@link WebElement} is resolved only when an interaction
 * with control happens. This allows {@link Control}s to be instantiated earlier (typically when a {@link PageObject}
 * subtype is instantiated.) <li>{@link Control} offers richer methods to interact with a form element, making the right
 * code easier to write. </ul>
 * <p/>
 * See {@link PageAreaImpl} subtypes for typical usage.
 *
 * @author Kohsuke Kawaguchi
 * @see PageAreaImpl#control(String...)
 */
public class Control extends CapybaraPortingLayerImpl {
    private final Owner parent;
    private final String[] relativePaths;

    public Control(PageAreaImpl parent, String... relativePaths) {
        super(parent.injector);
        this.parent = parent;
        this.relativePaths = relativePaths;
    }

    /**
     * Creates a control by giving their full path in the page
     */
    public Control(PageObject parent, String... paths) {
        super(parent.injector);
        this.parent = new Owner() {
            @Override
            public By path(String rel) {
                return by.path(rel);
            }
        };
        this.relativePaths = paths;
    }

    public Control(Injector injector, final By selector) {
        super(injector);
        this.relativePaths = new String[1];
        this.parent = new Owner() {
            @Override
            public By path(String rel) {
                return selector;
            }
        };
    }

    public WebElement resolve() {
        NoSuchElementException problem = new NoSuchElementException("No relative path specified!");
        for (String p : relativePaths) {
            try {
                return find(parent.path(p));
            }
            catch (NoSuchElementException e) {
                problem = e;
            }
        }
        throw problem;
    }

    public void sendKeys(String t) {
        resolve().sendKeys(t);
    }

    public void uncheck() {
        check(resolve(), false);
    }

    public void check() {
        check(resolve(), true);
    }

    public void check(boolean state) {
        check(resolve(), state);
    }

    /**
     * sends a click on the underlying element.
     * You should not use this on any page where the click will cause another page to be loaded as it will not
     * gaurantee that the new page has been loaded.
     * @see #clickAndWaitToBecomeStale()
     * @see #clickAndWaitToBecomeStale(Duration)
     */
    public void click() {
        WebElement we = resolve();
        // button may be obscured by say the "Save Apply" screen so we wait as Selenium will do a scroll but the CSS 
        // can take a while to update the layout \o/
        waitFor(we).
               withTimeout(time.seconds(1), TimeUnit.MILLISECONDS).
               pollingEvery(time.milliseconds(100), TimeUnit.MILLISECONDS).
               ignoring(ElementClickInterceptedException.class).
               until(() -> {we.click(); return true;});
    }


    /**
     * like click but will block for up to 30 seconds until the underlying web element has become stale.
     * see https://blog.codeship.com/get-selenium-to-wait-for-page-load/
     */
    /*package*/ void clickAndWaitToBecomeStale() {
        clickAndWaitToBecomeStale(Duration.ofSeconds(30));
    }

    /**
     * like click but will block until the underlying web element has become stale.
     * see https://blog.codeship.com/get-selenium-to-wait-for-page-load/
     * @param timeout the amount of time to wait
     */
    /*package*/ void clickAndWaitToBecomeStale(Duration timeout) {
        WebElement webElement = resolve();
        // webElement.submit() despite advertising it does exactly this just blows up :(
        webElement.click();
        waitFor(webElement).withTimeout(timeout).until(Control::isStale);
    }


    /**
     * The existing {@link org.jenkinsci.test.acceptance.po.Control#set(String)}
     * method has shortcomings regarding large strings because it utilizes
     * the sendKeys mechanism to enter the string which takes a significant amount
     * of time, i.e. the browser may consider the script to be unresponsive.
     *
     * This method method shall provide a high throughput mechanism which
     * puts the whole string at once into the text field instead of char by char.
     *
     * This is a solution / workaround published for Selenium Issue 4496:
     * https://code.google.com/p/selenium/issues/detail?id=4469
     *
     * @param text the large string to be entered
     */

    public void setAtOnce(String text){
        WebElement e = resolve();
        e.clear();
        ((JavascriptExecutor)driver).executeScript("arguments[0].value = arguments[1];", e, text);
    }

    /**
     * Returns the value of the input field.
     *
     * @return the value of the input field.
     */
    public String get() {
        return resolve().getAttribute("value");
    }

    /**
     * Sets the value of the input field to the specified text.
     *
     * Any existing value gets cleared.
     */
    public void set(@Nullable String text) {
        //if the text is longer than 255 characters, use the high throughput variant
        if (text!=null && text.length() > 255)
            setAtOnce(text);
        else {
            WebElement e = resolve();
            e.clear();
            e.sendKeys(StringUtils.defaultString(text));
        }
    }

    public void set(Object text) {
        set(text.toString());
    }

    /**
     * Clicks a menu button, and selects the matching item from the drop down.
     * TODO using a class name as the {@link Describable#value} does not seem to work.
     * @param type
     *      Class with {@link Describable} annotation.
     */
    public void selectDropdownMenu(Class type) {
        click();
        WebElement we = findCaption(type,findDropDownMenuItem);
        // the element may not yet be visible so wait for it to become shown after the click above
        waitFor(we).pollingEvery(100L, TimeUnit.MILLISECONDS).withTimeout(time.milliseconds(1000), TimeUnit.MILLISECONDS).until(() -> we.isDisplayed());
        we.click();
        // wait until the menu is hidden
        waitFor(we).pollingEvery(100L, TimeUnit.MILLISECONDS).withTimeout(time.milliseconds(1000), TimeUnit.MILLISECONDS).until(() -> !we.isDisplayed());
    }

    public void selectDropdownMenu(String displayName) {
        click();
        WebElement we = findDropDownMenuItem.find(displayName);
        // the element may not yet be visible so wait for it to become shown after the click above
        waitFor(we).pollingEvery(100L, TimeUnit.MILLISECONDS).withTimeout(time.milliseconds(1000), TimeUnit.MILLISECONDS).until(() -> we.isDisplayed());
        we.click();
        // wait until the menu is hidden
        waitFor(we).pollingEvery(100L, TimeUnit.MILLISECONDS).withTimeout(time.milliseconds(1000), TimeUnit.MILLISECONDS).until(() -> !we.isDisplayed());
    }

    /**
     * Given a menu button that shows a list of build steps, select the right item from the menu
     * to insert the said build step.
     */
    private final Finder<WebElement> findDropDownMenuItem = new Finder<WebElement>() {
        @Override
        protected WebElement find(String caption) {
            WebElement menuButton = resolve();

            // With enough implementations registered the one we are looking for might
            // require scrolling in menu to become visible. This dirty hack stretch
            // yui menu so that all the items are visible.
            executeScript("" +
                            "YAHOO.util.Dom.batch(" +
                            "    document.querySelector('.yui-menu-body-scrolled')," +
                            "    function (el) {" +
                            "        el.style.height = 'auto';" +
                            "        YAHOO.util.Dom.removeClass(el, 'yui-menu-body-scrolled');" +
                            "    }" +
                            ");"
            );
            // we can not use `Select` as these are YUI menus and we need to wait for it to be visible
            WebElement menu = findElement(menuButton, by.xpath("ancestor::*[contains(@class,'yui-menu-button')]/.."));
            WebElement e = findElement(menu, by.link(caption));
            return e;
        }
    };

    /**
     * For alternative use when the 'yui-menu-button' doesn't exist.
     * @param type
     */
    public void selectDropdownMenuAlt(Class type) {
        findCaption(type,findDropDownMenuItemBySelector);
        elasticSleep(1000);
    }

    private final Finder<WebElement> findDropDownMenuItemBySelector = new Finder<WebElement>() {
        @Override
        protected WebElement find(String caption) {
            WebElement menuButton = resolve();

            // With enough implementations registered the one we are looking for might
            // require scrolling in menu to become visible. This dirty hack stretch
            // yui menu so that all the items are visible.
            executeScript("" +
                    "YAHOO.util.Dom.batch(" +
                    "    document.querySelector('.yui-menu-body-scrolled')," +
                    "    function (el) {" +
                    "        el.style.height = 'auto';" +
                    "        YAHOO.util.Dom.removeClass(el, 'yui-menu-body-scrolled');" +
                    "    }" +
                    ");"
            );

            Select context = new Select(findElement(menuButton, by.xpath("ancestor-or-self::*[contains(@class,'setting-input dropdownList')]")));
            context.selectByVisibleText(caption);
            WebElement e = context.getFirstSelectedOption();
            return e;

        }
    };

    /**
     * Select an option.
     */
    public void select(String option) {
        WebElement e = resolve();
        // Make sure the select is scrolled into view before interacting with its options that has got special handling by scroller.
        new Scroller().scrollIntoView(e, driver);
        findElement(e, by.option(option)).click();
    }

    private WebElement findElement(WebElement context, By selector) {
        try {
            return context.findElement(selector);
        } catch (NoSuchElementException x) {
            // this is often the best place to set a breakpoint
            String msg = String.format("Unable to locate %s in %s", selector, driver.getCurrentUrl());
            throw new NoSuchElementException(msg, x);
        }
    }

    public void select(Class<?> describable) {
        String element = findCaption(describable, new Finder<String>() {
            @Override
            protected String find(String caption) {
                return Control.this.getElement(by.option(caption)) != null
                        ? caption : null
                ;
            }
        });

        select(element);
    }

    public void choose(Class<?> describable) {
        String element = findCaption(describable, new Finder<String>() {
            @Override
            protected String find(String caption) {
                final By xpath = by.xpath("//input[@type = 'radio' and @value = '%s']", caption);
                return Control.this.getElement(xpath) != null ? caption : null;
            }
        });

        choose(element);
    }

    public void upload(Resource res) {
        resolve().sendKeys(res.asFile().getAbsolutePath());
    }

    public String text() {
        return resolve().getText();
    }

    public FormValidation getFormValidation() {
        return FormValidation.await(this);
    }

    /**
     * Determines whether an object is existing on the current page
     * @return TRUE if it exists
     */
    public boolean exists(){
        try{
            this.resolve();
            return true;
        }
        catch (NoSuchElementException e)
        {
            return false;
        }
    }

    public interface Owner {
        /**
         * Resolves relative path into a selector.
         */
        By path(String rel);
    }

}
