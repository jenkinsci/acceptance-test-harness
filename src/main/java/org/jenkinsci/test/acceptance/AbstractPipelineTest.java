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

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Folder;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.jenkinsci.test.acceptance.utils.PipelineTestUtils;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchFrameException;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasAction;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

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

    public String scriptForPipelineFromResourceWithParameters(final String resourceName, final String... scriptParameters) throws IOException {
        final String script = PipelineTestUtils.scriptForPipelineFromResource(this.getClass(), resourceName);
        PipelineTestUtils.checkScript(script);

        return String.format(script, (Object[]) scriptParameters);
    }

    protected void assertJavadoc(final Job job) {
        assertThat(job, hasAction(JAVADOC_ACTION));

        find(by.link(JAVADOC_ACTION)).click();

        /*
        Same approach as https://github.com/jenkinsci/acceptance-test-harness/blob/b47dcb9a8abb771718a391372faf612a0734e76a/src/test/java/plugins/JavadocPluginTest.java#L92
        Why do we check in such way?
        The java version of the Jenkins under test could be different that the one in use by this code, the ATH code.
        See: https://github.com/jenkinsci/acceptance-test-harness/blob/39cbea43b73d32a0912d613a41e08ba9b54aad1d/src/main/java/org/jenkinsci/test/acceptance/controller/LocalController.java#L190
        In addition, the way javadoc is generated depends on the java version and the number of packages generated, one
        or more packages.
        See: https://issues.jenkins-ci.org/browse/JENKINS-32619?focusedCommentId=311819&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#comment-311819
        So even though it's not the most refined way to check the right generation of the javadoc, this way doesn't
        need to call a groovy script for checking the java version neither checking the number of packages generated.
        */

        try {
            // Former behavior, javadoc generating frames and plugin without redirection
            driver.switchTo().frame("classFrame");
        } catch (NoSuchFrameException e) {
            try {
                // With Java11 a link to the package-summary is shown, no frames
                find(by.partialLinkText("package-summary.html")).click();
            } catch (NoSuchElementException nse) {
            }
        }

        assertThat(driver, hasContent("io.jenkins.tools"));
    }
}
