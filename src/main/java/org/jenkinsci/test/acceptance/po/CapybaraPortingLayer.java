package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;

/**
 * For assisting porting from Capybara.
 *
 * @author Kohsuke Kawaguchi
 */
public class CapybaraPortingLayer {
    @Inject
    protected WebDriver driver;

    /**
     * Navigates the browser to the page.
     *
     * @path url
     *      URL relative to the context path of Jenkins, such as "/about" or
     *      "/job/foo/configure".
     */
    protected final WebDriver visit(String url) {
        driver.get(url);
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
}
