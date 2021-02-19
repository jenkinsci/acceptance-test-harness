/*
 * The MIT License
 *
 * Copyright (c) Red Hat, Inc.
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

import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.jenkinsci.test.acceptance.Matcher;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import javax.annotation.Nonnull;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.jenkinsci.test.acceptance.po.CapybaraPortingLayer.by;

/**
 * Result of form field validation.
 *
 * @see Control#getFormValidation()
 * @see FormValidation#silent()
 * @see FormValidation#reports(Kind, String)
 *
 * @author ogondza.
 */
public class FormValidation {
    public enum Kind {
        OK, WARNING, ERROR;

        public static Kind get(String cls) {
            if (cls == null || cls.isEmpty()) { // class can be empty as it is handled differently than other attributes by selenium
                cls = "ok"; // No class means ok without message
            }
            return valueOf(cls.toUpperCase());
        }
    }

    private final @Nonnull Kind kind;
    private final @Nonnull String message;

    public static FormValidation await(Control control) {
        WebElement element = control.resolve();

        WebElement validationArea;

        // Special handling for validation buttons and their markup
        if (element.getTagName().equals("button")) {
            WebElement spinner = element.findElement(by.xpath("./../../../following-sibling::div[1]"));
            // Wait as long as there is some spinner shown on the page
            control.waitFor().until(() -> !spinner.isDisplayed());
            // Expand details (are there any) before we get the area element as doing so afterwards would stale the element reference
            for (WebElement elem : element.findElements(By.linkText("(show details)"))) {
                elem.click();
            }
            validationArea = element.findElement(by.xpath("./../../../following-sibling::div[2]"));
        } else {
            // Fire validation if it was not already
            element.sendKeys(Keys.TAB);

            // Wait for validation area to stop being <div></div>
            validationArea = control.waitFor().until(() -> {
                // path to validation area is the parents sibling with class `validation-error-area`
                WebElement va = element.findElement(by.xpath("../../div[contains(@class,'validation-error-area')]"));
                try {
                    va.findElement(by.xpath("./div[2]"));
                    return va;
                } catch (NoSuchElementException noDiv) {
                    // https://issues.jenkins-ci.org/browse/JENKINS-59605?focusedCommentId=377474&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-377474
                    // There are known false-negatives in ATH so let's presume this is done and successful until the core is fixed.
                    return va;
                }
            });
        }

        return new FormValidation(validationArea);
    }

    public FormValidation(WebElement element) {
        List<WebElement> divs = element.findElements(by.xpath("div[2]/div"));
        switch (divs.size()) {
            case 0: // TODO remove after https://issues.jenkins-ci.org/browse/JENKINS-59605
                this.kind = Kind.OK;
                this.message = "";
                break;
            case 1:
                WebElement outcome = divs.get(0);
                this.kind = extractKind(outcome);
                this.message = outcome.getText();
                break;
            default:
                throw new RuntimeException("Too many validation elements: " + divs);
        }
    }

    private @Nonnull Kind extractKind(WebElement element) {
        String kindClass = element.getAttribute("class");
        try {
            return Kind.get(kindClass);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(
                    "Unknown kind class provided '" + kindClass + "' in " + element.getAttribute("outerHTML"),
                    ex
            );
        }
    }

    public @Nonnull Kind getKind() {
        return kind;
    }

    public @Nonnull String getMessage() {
        return message;
    }

    @Override public String toString() {
        return kind + "(" + message + ")";
    }

    /**
     * When either there is no validation or empty OK was returned (there is no way to tell that apart).
     */
    public static Matcher<FormValidation> silent() {
        return new Matcher<FormValidation>("No form validation result should be presented") {
            @Override public boolean matchesSafely(FormValidation item) {
                return item.getKind() == Kind.OK && item.getMessage().isEmpty();
            }

            @Override public void describeMismatchSafely(FormValidation item, Description mismatchDescription) {
                mismatchDescription.appendText("It is " + item.toString());
            }
        };
    }

    public static Matcher<FormValidation> reports(final Kind kind, final String message) {
        return reports(kind, equalTo(message));
    }

    public static Matcher<FormValidation> reports(final Kind kind, final org.hamcrest.Matcher<String> message) {
        StringDescription sd = new StringDescription();
        message.describeTo(sd);
        return new Matcher<FormValidation>("Validation reporting " + kind + " with message: " + sd.toString()) {
            @Override public boolean matchesSafely(FormValidation item) {
                return item.getKind() == kind && message.matches(item.getMessage());
            }

            @Override public void describeMismatchSafely(FormValidation item, Description mismatchDescription) {
                if (item.getKind() != kind) {
                    mismatchDescription.appendText("It is " + item.toString());
                } else {
                    message.describeMismatch(item.getMessage(), mismatchDescription);
                }
            }
        };
    }
}
