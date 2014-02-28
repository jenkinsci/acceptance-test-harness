package org.jenkinsci.test.acceptance.po;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import javax.inject.Inject;
import java.net.URL;

/**
 * For assisting porting from Capybara.
 *
 * @author Kohsuke Kawaguchi
 */
public class CapybaraPortingLayer extends Assert {
    @Inject
    protected WebDriver driver;

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

    protected WebElement find(By selector) {
        return driver.findElement(selector);
    }

    protected void fillIn(String formFieldName, String value) {
        driver.findElement(By.name(formFieldName)).sendKeys(value);
    }
}
