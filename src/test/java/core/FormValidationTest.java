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
package core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.jenkinsci.test.acceptance.po.FormValidation.Kind;
import static org.jenkinsci.test.acceptance.po.FormValidation.reports;
import static org.jenkinsci.test.acceptance.po.FormValidation.silent;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.FormValidation;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.ListView;
import org.junit.Test;
import org.openqa.selenium.Keys;

public class FormValidationTest extends AbstractJUnitTest {

    @Test
    public void validate() {
        ajaxValidation();
        navigateAway();
        jsValidation();
        navigateAway();
    }

    private void ajaxValidation() {
        ListView lv = jenkins.views.create(ListView.class);
        lv.configure();

        lv.matchJobs(".*");
        assertThat(lv.includeRegex.getSilentFormValidation(), silent());

        lv.matchJobs("[");
        assertThat(lv.includeRegex.getFormValidation().getKind(), equalTo(Kind.ERROR));
    }

    private void jsValidation() {
        JenkinsConfig c = jenkins.getConfigPage();
        c.configure();
        c.setQuietPeriod(16);
        FormValidation formValidation = c.quietPeriod.getSilentFormValidation();
        assertThat(formValidation, silent());

        c.setQuietPeriod(-16);
        c.quietPeriod.resolve().sendKeys(Keys.TAB);
        c.quietPeriod.sleep(100L);
        formValidation = c.quietPeriod.getFormValidation();

        String errorMessage = "Not a non-negative integer";
        assertThat(formValidation, reports(Kind.ERROR, errorMessage));
    }

    private void navigateAway() {
        jenkins.runThenHandleUserPrompt(() -> jenkins.open());
        sleep(1000); // Needed for some reason
    }
}
