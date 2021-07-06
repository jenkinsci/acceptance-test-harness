/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
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
package org.jenkinsci.test.acceptance.selenium;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;

/**
 * Make sure there are no exceptions shown after user interaction.
 *
 * @author ogondza
 */
public class SanityChecker extends AbstractWebDriverEventListener {
    private final static By SPECIFIER = By.xpath(
            "//h1/span[contains(., 'Oops!')]/../following-sibling::div/h2[text()='Stack trace']/following-sibling::pre"
    );

    @Override
    public void afterNavigateTo(String url, WebDriver driver) {
        checkSanity(driver);
    }

    @Override
    public void beforeNavigateTo(String url, WebDriver driver) {
        checkSanity(driver);
    }

    @Override
    public void beforeClickOn(WebElement element, WebDriver driver) {
        checkSanity(driver);
    }

    private void checkSanity(WebDriver driver) {
        if (isFastPath(driver)) return;

        // Exception
        List<WebElement> elements = driver.findElements(SPECIFIER);
        if (!elements.isEmpty()) {
            String trace = elements.get(0).getText();
            throw new Failure("Jenkins error detected at " + driver.getCurrentUrl() + ":\n" + trace);
        }

        // POST required
        WebElement postForm = driver.findElement(By.cssSelector("form > input[value='Try POSTing']"));
        if (postForm != null) throw new Failure("Post required at " + driver.getCurrentUrl());
    }

    /**
     * Quickly determine if the driver is definitely in the safe state.
     *
     * <p>
     * The expectation is that the most of the time this would return true,
     * and reduces the overhead of {@link SanityChecker}.
     */
    private boolean isFastPath(WebDriver driver) {
        try {
            final String pageSource = driver.getPageSource();
            return !(pageSource.contains("Oops!") || pageSource.contains("Try POSTing"));
        } catch (UnhandledAlertException ex) {
            // If alert is expected we can not check sanity and should leave it alone for test to handle. If it is not
            // expected, the code is likely going to fail anyway but it is better to do on less surprising place
            return true;
        }
    }

    public static final class Failure extends RuntimeException {

        private static final long serialVersionUID = 4077465978093533078L;

        public Failure(String msg) {
            super(msg);
        }
    }
}
