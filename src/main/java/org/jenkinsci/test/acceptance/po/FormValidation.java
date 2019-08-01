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
import org.openqa.selenium.WebElement;

import javax.annotation.Nonnull;
import java.util.List;

import static org.hamcrest.Matchers.*;

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
        OK, WARNING, ERROR, NONE
    }

    private final WebElement element;
    private final Kind kind;
    private final String message;

    public FormValidation(WebElement element) {
        List<WebElement> divs = element.findElements(CapybaraPortingLayer.by.tagName("div"));
        switch (divs.size()) {
            case 0:
                this.element = null;
                this.kind = Kind.NONE;
                this.message = null;
                break;
            case 1:
                this.element = divs.get(0);
                this.kind = extractKind();
                // Expand details are there any
                for (WebElement elem : this.element.findElements(By.linkText("(show details)"))) {
                    elem.click();
                }
                this.message = this.element.getText();
                break;
            default:
                throw new RuntimeException("Too many validation elements: " + divs);
        }
    }

    public @Nonnull Kind extractKind() {
        String kindClass = element.getAttribute("class");
        switch (kindClass) {
            case "error":
                return Kind.ERROR;
            case "warning":
                return Kind.WARNING;
            case "ok":
                return Kind.OK;
            default:
                throw new RuntimeException("Unknown kind class provided '" + kindClass + "' in " + element.getAttribute("outerHTML"));
        }
    }

    public Kind getKind() {
        return kind;
    }

    public String getMessage() {
        return message;
    }

    @Override public String toString() {
        return kind + ": " + message;
    }

    /**
     * When either there is no validation or empty OK was returned (there is no way to tell that apart).
     */
    public static final Matcher<FormValidation> silent() {
        return new Matcher<FormValidation>("No form validation result should be presented") {
            @Override public boolean matchesSafely(FormValidation item) {
                return item.getKind() == Kind.NONE && item.getMessage() == null;
            }

            @Override public void describeMismatchSafely(FormValidation item, Description mismatchDescription) {
                mismatchDescription.appendText("It is " + item.toString());
            }
        };
    }

    public static final Matcher<FormValidation> reports(final Kind kind, final String message) {
        return reports(kind, equalTo(message));
    }

    public static final Matcher<FormValidation> reports(final Kind kind, final org.hamcrest.Matcher<String> message) {
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
