package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;

/**
 * Various {@link ExpectedCondition} that can be reused.
 */
public final class Conditions {

    /**
     * ExpectedCondition that waits for any animation (size or opacity) of a
     * matched element to finish.
     *
     * @param by Selector to locate the element
     * @return the {@link WebElement} that matched the selector that has for 2
     *         consecutive calls not changed position or opacity.
     */
    public static ExpectedCondition<WebElement> waitForElementAnimationToFinish(final By by) {
        return new ExpectedCondition<>() {
            private Rectangle rect;
            private String opacity;

            @Override
            public WebElement apply(WebDriver driver) {
                WebElement element = driver.findElement(by);
                if (!element.isDisplayed()) {
                    return null;
                }
                String newOpacity = element.getCssValue("opacity");
                Rectangle newRect = element.getRect();
                if (newRect.equals(rect) && newOpacity.equals(opacity)) {
                    return element;
                }
                opacity = newOpacity;
                rect = newRect;
                return null;
            }

            @Override
            public String toString() {
                return "Animation complete for selector: " + by;
            }
        };
    }
}
