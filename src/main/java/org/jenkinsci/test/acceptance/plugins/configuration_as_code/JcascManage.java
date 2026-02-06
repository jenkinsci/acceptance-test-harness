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
package org.jenkinsci.test.acceptance.plugins.configuration_as_code;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.time.Duration;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.FormValidation;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageObject;

public class JcascManage extends PageObject {
    public JcascManage(Jenkins jenkins) {
        super(jenkins, jenkins.url("configuration-as-code"));
    }

    /**
     * Configure and apply the new source
     */
    public void configure(String path) {
        visit("#new");

        Control control = control("/newSource");
        control.set(path);
        waitFor()
                .withTimeout(Duration.ofSeconds(5))
                .ignoring(AssertionError.class)
                .until(() -> {
                    assertThat(
                            control.getFormValidation(),
                            FormValidation.reports(FormValidation.Kind.OK, "The configuration can be applied"));
                    return true;
                });

        clickButton("Apply configuration");
        verifySuccessfulApplication();
    }

    public void reload() {
        find(by.name("reload")).click();
        verifySuccessfulApplication();
    }

    private void verifySuccessfulApplication() {
        String pageSource = getPageSource();
        assertThat(pageSource, not(containsString("Error")));
        assertThat(pageSource, not(containsString("Exception")));
    }
}
