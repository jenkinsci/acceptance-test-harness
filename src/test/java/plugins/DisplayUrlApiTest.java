package plugins;

import com.google.inject.Inject;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.internal.AssumptionViolatedException;

import jakarta.mail.MessagingException;
import java.io.IOException;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * Tests the display-url-api plugin.
 */
@WithDocker
@Category(DockerTest.class)
@WithPlugins({"mailer@1.18", "workflow-job", "workflow-cps", "workflow-basic-steps", "workflow-durable-task-step"})
public class DisplayUrlApiTest extends AbstractPipelineTest {

    @Inject
    private MailhogProvider mailhogProvider;

    private HttpClient client = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();

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
            throw new AssumptionViolatedException("Test redirect with Blue Ocean has already be tested by other test on this suite");
        }
    }


    private String scriptForMailerPipeline(boolean makeFail) {
        String status = makeFail ? "FAILURE" : "SUCCESS";
        return String.format("node {\n" +
                "    try {\n" +
                "        sh 'exit 1'\n" +
                "    } catch (any) {\n" +
                "        currentBuild.result = '%s'\n" +
                "        if (currentBuild.result == 'FAILURE')\n" +
                "            throw any\n" +
                "    } finally {\n" +
                "        step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: 'dev@example.com', sendToIndividuals: true])\n" +
                "    }\n" +
                "}", status);
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
        return jenkins.runScript("" +
                "import org.jenkinsci.plugins.displayurlapi.DisplayURLProvider\n" +
                "import org.jenkinsci.plugins.workflow.job.*\n" +
                "\n" +
                "WorkflowJob p = new WorkflowJob(Jenkins.getInstance(),\"%s\");\n" +
                "WorkflowRun build = new WorkflowRun(p);\n" +
                "\n" +
                "print DisplayURLProvider.get().getRunURL(build)\n" +
                "print \";\" + DisplayURLProvider.get().getJobURL(p)\n" +
                "print \";\" + DisplayURLProvider.get().getChangesURL(build)", jobName).split(";");
    }

    private void assertRedirect(String s) throws IOException {
        CloseableHttpResponse response = null;
        HttpGet get = new HttpGet(s.trim());
        try {
            response = (CloseableHttpResponse) client.execute(get);
            int responseCode = response.getStatusLine().getStatusCode();
            assertTrue(String.format("URL %s is not reachable", s.trim()), responseCode == 200);
        } finally {
            response.close();
        }
    }

}