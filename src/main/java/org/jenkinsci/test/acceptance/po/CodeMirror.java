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
package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;

/**
 * Encapsulate CodeMirror wizardry.
 *
 * @author ogondza
 */
public class CodeMirror extends PageAreaImpl {
    public CodeMirror(PageObject context, String path) {
        super(context, path);
    }

    public CodeMirror(PageAreaImpl area, String relativePath) {
        super(area, relativePath);
    }

    public void set(String content) {
        if (!(driver instanceof JavascriptExecutor)) {
            throw new AssertionError("JavaScript execution not supported");
        }

        // can't use find() because it wants a visible element
        // wait until the element in question appears in DOM as it is added by JavaScript
        waitFor()
                .ignoring(NoSuchElementException.class)
                .until(() -> driver.findElement(by.xpath("//*[@path='%s']", getPath())));

        executeScript(scriptSet, String.format("//*[@path='%s']/following-sibling::div", getPath()), content);
    }

    /**
     * Returns the value of the codemirror.
     *
     * @return value of the codemirror.
     */
    public String get() {
        return executeScript(scriptGet, String.format("//*[@path='%s']/following-sibling::div", getPath()))
                .toString();
    }

    private static final String scriptPrefix = "cmElem = document.evaluate("
            + "        arguments[0], document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null"
            + ").singleNodeValue;"
            + "codemirror = cmElem.CodeMirror;"
            + "if (codemirror == null) {"
            + "    console.log('CodeMirror object not found!');"
            + "}";

    private static final String scriptSet = scriptPrefix + "codemirror.setValue(arguments[1]);" + "codemirror.save();";

    private static final String scriptGet = scriptPrefix + "return codemirror.getValue();";
}
