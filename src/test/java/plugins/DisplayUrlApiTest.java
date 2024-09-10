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
package plugins;

import static org.junit.Assert.assertEquals;

import com.google.inject.Inject;
import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.regex.Pattern;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.jenkinsci.test.acceptance.AbstractPipelineTest;
import org.jenkinsci.test.acceptance.docker.fixtures.MailhogContainer;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.PluginManager;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.jenkinsci.test.acceptance.update_center.PluginSpec;
import org.jenkinsci.test.acceptance.utils.mail.MailhogProvider;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests the display-url-api plugin.
 */
@WithDocker
@Category(DockerTest.class)
@WithPlugins({"mailer", "workflow-job", "workflow-cps", "workflow-basic-steps", "workflow-durable-task-step"})
public class DisplayUrlApiTest extends AbstractPipelineTest {

    @Inject
    private MailhogProvider mailhogProvider;

    private HttpClient client = HttpClientBuilder.create()
            .setRedirectStrategy(new LaxRedirectStrategy())
            .build();

    @Test
    public void testUrlInEmail() throws IOException, MessagingException {
        final MailhogContainer mailhog = mailhogProvider.get();
        final WorkflowJob job = createPipelineJobWithScript(scriptForMailerPipeline(true));
        job.startBuild().shouldFail();
        String expectedUrlGeneratedByDisplayUrlApiPlugin = getURLS(job.name)[0];

        mailhog.assertMail(
                Pattern.compile("Build failed in Jenkins: .* #1"),
                "dev@example.com",
                Pattern.compile(expectedUrlGeneratedByDisplayUrlApiPlugin));

        makeJobSucceed(job);
        job.startBuild().shouldSucceed();

        mailhog.assertMail(
                Pattern.compile("Jenkins build is back to normal .* #2"),
                "dev@example.com",
                Pattern.compile(expectedUrlGeneratedByDisplayUrlApiPlugin.replace("/1/", "/2/")));
    }

    @Test
    @WithPlugins({"blueocean"})
    public void testRedirectsToBlueOcean() throws IOException {
        doTestRedirects();
    }

    @Test
    public void testRedirects() throws IOException {
        PluginManager pm = jenkins.getPluginManager();
        if (pm.installationStatus(new PluginSpec("blueocean")) == PluginManager.InstallationStatus.NOT_INSTALLED) {
            doTestRedirects();
        } else {
            throw new AssumptionViolatedException(
                    "Test redirect with Blue Ocean has already be tested by other test on this suite");
        }
    }

    private String scriptForMailerPipeline(boolean makeFail) {
        String status = makeFail ? "FAILURE" : "SUCCESS";
        return String.format(
                "node {\n" + "    try {\n"
                        + "      error \" Failure\""
                        + "    } catch (any) {\n"
                        + "        currentBuild.result = '%s'\n"
                        + "        if (currentBuild.result == 'FAILURE')\n"
                        + "            throw any\n"
                        + "    } finally {\n"
                        + "        step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: 'dev@example.com', sendToIndividuals: true])\n"
                        + "    }\n"
                        + "}",
                status);
    }

    private void makeJobSucceed(WorkflowJob job) {
        job.configure();
        job.script.set(scriptForMailerPipeline(false));
        job.save();
    }

    private void doTestRedirects() throws IOException {
        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class);
        String[] urls = getURLS(job.name);

        job.startBuild().shouldSucceed();

        for (String s : urls) {
            assertRedirect(s);
        }
    }

    private String[] getURLS(String jobName) {
        return jenkins.runScript(
                        "import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider\n"
                                + "import org.jenkinsci.plugins.workflow.job.*\n"
                                + "\n"
                                + "WorkflowJob p = new WorkflowJob(Jenkins.getInstance(),\"%s\");\n"
                                + "WorkflowRun build = new WorkflowRun(p);\n"
                                + "\n"
                                + "print DisplayURLProvider.get().getRunURL(build)\n"
                                + "print \";\" + DisplayURLProvider.get().getJobURL(p)\n"
                                + "print \";\" + DisplayURLProvider.get().getChangesURL(build)",
                        jobName)
                .split(";");
    }

    private void assertRedirect(String s) throws IOException {
        HttpGet get = new HttpGet(s.trim());
        try (CloseableHttpResponse response = (CloseableHttpResponse) client.execute(get)) {
            int responseCode = response.getStatusLine().getStatusCode();
            assertEquals(String.format("URL %s is not reachable", s.trim()), 200, responseCode);
        }
    }
}
