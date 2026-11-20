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

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * PO for `/build` page that wait for user to specify parameters.
 *
 * @author ogondza.
 */
public class BuildWithParameters extends PageObject {
    public BuildWithParameters(Job job, URL url) {
        super(job, url);
    }

    public BuildWithParameters enter(List<Parameter> definitions, Map<String, ?> values) {
        waitFor(by.xpath("//form[@name='parameters']"), 2);
        for (Parameter def : definitions) {
            Object v = values.get(def.getName());
            if (v != null) {
                def.fillWith(v);
            }
        }
        return this;
    }

    public void start() {
        if (Boolean.getBoolean("new-job-page.flag.defaultValue")) {
            find(by.xpath("//dialog[@open]//div[@id='bottom-sticker']//button[contains(., 'Build')]"))
                    .click();
        } else {
            clickButton("Build");
        }
    }
}
