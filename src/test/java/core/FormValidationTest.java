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

import hudson.util.VersionNumber;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.FormValidation;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.po.FormValidation.*;

public class FormValidationTest extends AbstractJUnitTest {

    @Test
    public void validate() throws Exception {
        JenkinsConfig c = jenkins.getConfigPage();
        c.configure();
        c.numExecutors.set(16);
        FormValidation formValidation = c.numExecutors.getFormValidation();
        assertThat(formValidation, silent());

        c.numExecutors.set(-16);
        formValidation = c.numExecutors.getFormValidation();

        //support older jenkins versions
        String errorMessage = jenkins.getVersion().isNewerThan(new VersionNumber("2.104"))? "Not a non-negative number" : "Not an integer";
        assertThat(formValidation, reports(Kind.ERROR, errorMessage));
    }
}
