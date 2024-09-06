/*
 * The MIT License
 *
 * Copyright (c) 2023 CloudBees, Inc.
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
package org.jenkinsci.test.acceptance;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasAction;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import java.io.IOException;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Folder;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.jenkinsci.test.acceptance.utils.PipelineTestUtils;

@WithPlugins({"workflow-job", "workflow-cps", "workflow-basic-steps", "workflow-durable-task-step"})
public class AbstractPipelineTest extends AbstractJUnitTest {
    private static final String JAVADOC_ACTION = "Javadoc";

    public WorkflowJob createPipelineJobWithScript(final String script) {
        return PipelineTestUtils.createPipelineJobWithScript(jenkins.getJobs(), script);
    }

    public WorkflowJob createPipelineJobInFolderWithScript(final Folder f, final String script) {
        return PipelineTestUtils.createPipelineJobWithScript(f.getJobs(), script);
    }

    public String scriptForPipeline() {
        return null;
    }

    public String scriptForPipelineWithParameters(final String... scriptParameters) {
        final String script = this.scriptForPipeline();
        PipelineTestUtils.checkScript(script);

        return String.format(script, (Object[]) scriptParameters);
    }

    public String scriptForPipelineFromResourceWithParameters(
            final String resourceName, final String... scriptParameters) throws IOException {
        final String script = PipelineTestUtils.scriptForPipelineFromResource(this.getClass(), resourceName);
        PipelineTestUtils.checkScript(script);

        return String.format(script, (Object[]) scriptParameters);
    }

    protected void assertJavadoc(final Job job) {
        assertThat(job, hasAction(JAVADOC_ACTION));

        find(by.link(JAVADOC_ACTION)).click();

        find(by.href("index-all.html")).click();
        find(by.href("io/jenkins/tools/package-summary.html")).click();

        assertThat(driver, hasContent("io.jenkins.tools"));
    }
}
