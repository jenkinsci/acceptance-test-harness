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
package plugins;

import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.StringParameter;
import org.junit.Test;

@WithPlugins("job-parameter-summary")
public class JobParameterSummaryPluginTest extends AbstractJUnitTest {

    @Test
    public void show_summary() {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.addParameter(StringParameter.class).setName("PARAM_1").setDefault("DEFAULT_1");
        job.addParameter(StringParameter.class).setName("PARAM_2").setDescription("DESC_2");
        job.save();

        assertThat(driver, Matchers.hasContent("PARAM_1"));
        assertThat(driver, Matchers.hasContent("DEFAULT_1"));
        assertThat(driver, Matchers.hasContent("PARAM_2"));
        assertThat(driver, Matchers.hasContent("DESC_2"));
    }
}
