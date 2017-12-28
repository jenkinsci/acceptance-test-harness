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

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.FormValidation;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.po.FormValidation.*;

public class ConfigureNumExecutorsTest extends AbstractJUnitTest {

    FormValidation formValidation;
    String strExecutors;

    private void check4CorrectNumExecutors(JenkinsConfig config, String numExecutors) {

        config.configure();
        config.numExecutors.set(numExecutors);
        formValidation = config.numExecutors.getFormValidation();
        assertThat(formValidation, silent());
        jenkins.save();

        config.configure();
        strExecutors = config.numExecutors.get();
        assertThat("Incorrect numExecutors has been saved",
                strExecutors, equalTo(numExecutors));
    }

    private void check4IncorrectNumExecutors(JenkinsConfig config, String numExecutors) {

        config.configure();
        String oldNumExecutors = config.numExecutors.get();
        config.numExecutors.set(numExecutors);
        formValidation = config.numExecutors.getFormValidation();
        assertThat(formValidation, reports(Kind.ERROR, "Not a non-negative number"));
        jenkins.save();

        config.configure();
        strExecutors = config.numExecutors.get();
        assertThat("Incorrect numExecutors has been saved",
                strExecutors, equalTo(oldNumExecutors));
    }

    @Test
    public void validate_numExecutors_for_master() throws Exception {

        JenkinsConfig c = jenkins.getConfigPage();

        check4CorrectNumExecutors(c, "16");
        check4CorrectNumExecutors(c, "0");
        check4IncorrectNumExecutors(c, "-16");
        check4IncorrectNumExecutors(c, "sixteen");

    }
}
