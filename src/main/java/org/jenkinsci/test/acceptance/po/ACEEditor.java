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

import org.openqa.selenium.WebDriver;

import javax.annotation.Nonnull;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ACEEditor extends JavaScriptWidget {

    private final String cssSelector;

    public ACEEditor(@Nonnull String cssSelector, @Nonnull WebDriver driver) {
        super(driver);
        this.cssSelector = cssSelector;
    }

    public void waitForRenderOf() {
        waitForRenderOf(cssSelector + " .ace_text-input");
    }

    public void set(@Nonnull String editorContent) {
        
        // Make sure the editor is fully rendered first i.e. wait for
        // the ace_text-input element to have been added.
        waitForRenderOf();

        // Now we can set the editor content.
        executeScript(
                "var targets = document.getElementsBySelector(arguments[0]);" +
                "if (!targets || targets.length === 0) {" +
                "    throw '**** Failed to find ACE Editor target object on page. Selector: ' + arguments[0];" +
                "}" +
                "if (!targets[0].aceEditor) {" +
                "    throw '**** Selected ACE Editor target object is not an active ACE Editor. Selector: ' + arguments[0];" +
                "}" +
                "targets[0].aceEditor.setValue(arguments[1]);", 
                cssSelector, editorContent);
    }
}
