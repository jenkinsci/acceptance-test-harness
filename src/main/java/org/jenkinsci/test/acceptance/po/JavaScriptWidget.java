/*
 * The MIT License
 *
 * Copyright (c) 2015, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.test.acceptance.po;

import org.junit.Assert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JavaScriptWidget {

    public final JavascriptExecutor driver;

    public JavaScriptWidget(@Nonnull WebDriver driver) {
        assertIsJavaScriptExecutor(driver);
        this.driver = (JavascriptExecutor) driver;
    }

    public boolean isRendered(@Nonnull String cssSelector) {
        return isRendered(cssSelector, (WebDriver) driver);
    }

    public void waitForRenderOf(@Nonnull String cssSelector) {
        waitForRenderOf(cssSelector, (WebDriver) driver);
    }

    public static boolean isRendered(@Nonnull String cssSelector, @Nonnull WebDriver driver) {
        assertIsJavaScriptExecutor(driver);
        return (boolean) ((JavascriptExecutor)driver).executeScript(
                "var targets = document.getElementsBySelector(arguments[0]);" +
                        "if (!targets || targets.length === 0) {" +
                        "    return false;" +
                        "} else {" +
                        "    return true;" +
                        "}", cssSelector);
    }

    public static void waitForRenderOf(@Nonnull String cssSelector, @Nonnull WebDriver driver) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() < start + 10000) {
            if (isRendered(cssSelector, driver)) {
                return;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                break;
            }
        }
        Assert.fail("Timed out waiting on '" + cssSelector + "' to be rendered.");
    }

    protected Object executeScript(String script, Object... args) {
        return driver.executeScript(script, args);
    }


    protected Object executeAsyncScript(String script, Object... args) {
        return driver.executeAsyncScript(script, args);
    }

    private static void assertIsJavaScriptExecutor(WebDriver driver) {
        if (!(driver instanceof JavascriptExecutor)) {
            throw new IllegalArgumentException("WebDriver type " + driver.getClass().getName() + " does not implement JavascriptExecutor.");
        }
    }
}
