/*
 * The MIT License
 *
 * Copyright (c) 2016-2023 CloudBees, Inc.
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

import org.apache.http.HttpResponse;
import org.hamcrest.Description;
import org.hamcrest.Matchers;
import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.authorize_project.ProjectDefaultBuildAccessControl;
import org.jenkinsci.test.acceptance.plugins.credentials.AbstractCredentialsTest;
import org.jenkinsci.test.acceptance.plugins.credentials.BaseStandardCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsPage;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsRESTClient;
import org.jenkinsci.test.acceptance.plugins.credentials.FileCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.StringCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.UserPwdCredential;
import org.jenkinsci.test.acceptance.plugins.credentialsbinding.ManagedCredentialsBinding;
import org.jenkinsci.test.acceptance.plugins.credentialsbinding.SecretStringCredentialsBinding;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.ShellBuildStep;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.jenkinsci.test.acceptance.utils.PipelineTestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.jenkinsci.test.acceptance.utils.PipelineTestUtils.resolveScriptName;

@WithPlugins ({"credentials@2.0.7", "workflow-job", "workflow-cps", "workflow-basic-steps", "workflow-durable-task-step"})
public class CredentialsBindingTest extends AbstractCredentialsTest {

    private static final String USERNAME_CORRECT_MESSAGE = "username has the correct value";
    private static final String PASSWORD_CORRECT_MESSAGE = "password has the correct value";
    private static final String PRIVATE_KEY_CORRECT_MESSAGE = "ssh user private key has the correct value";
    private static final String COMPLETE_PASSWORD_CORRECT_MESSAGE = "complete password has the correct value";
    private static final String SECRET_TEXT_CORRECT_MESSAGE = "secret has the correct value";
    private static final String SECRET_FILE_CORRECT_MESSAGE = "text inside secret file has the correct value";
    private static final String SECRET_ZIP_FILE_CORRECT_MESSAGE = "text inside secret zip file has the correct value";
    private static final String SECRET_TEXT = "secret";
    private static final String SECRET_OUTPUT = " variable binded";


    @Test @WithPlugins({"plain-credentials", "credentials-binding"})
    public void testTextBinding() {
        CredentialsPage mc = new CredentialsPage(jenkins, ManagedCredentials.DEFAULT_DOMAIN);
        mc.open();
        StringCredentials cred = mc.add(StringCredentials.class);
        cred.scope.select("GLOBAL");
        cred.secret.set(SECRET_TEXT);
        mc.create();

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.check("Use secret text(s) or file(s)");
        ManagedCredentialsBinding mcb = new ManagedCredentialsBinding(job);
        SecretStringCredentialsBinding cb = mcb.addCredentialBinding(SecretStringCredentialsBinding.class);
        cb.variable.set("BINDED_SECRET");
        ShellBuildStep shell = job.addBuildStep(ShellBuildStep.class);
        shell.command("if [ \"$BINDED_SECRET\" = \"" + SECRET_TEXT + "\" ] \n then \n echo \"" + SECRET_OUTPUT + "\" \n fi");
        job.save();
        
        Build b = job.scheduleBuild();
        b.shouldSucceed();
        assertThat(b.getConsole(), containsString(SECRET_OUTPUT));
    }

    @Test
    @WithPlugins("credentials-binding@1.10")
    public void pipelineWithCredentialsTest() throws IOException {
        final HttpResponse resp = new CredentialsRESTClient(jenkins.url).createCredential(CRED_ID, CRED_USER, CRED_PWD, GLOBAL_SCOPE);
        Assert.assertThat(resp.getStatusLine().getStatusCode(), is(200));

        final String script = PipelineTestUtils.scriptForPipelineFromResourceWithParameters(this.getClass(), resolveScriptName("usernameSplitPasswordScript"), CRED_ID, CRED_USER, USERNAME_CORRECT_MESSAGE, CRED_PWD, PASSWORD_CORRECT_MESSAGE);
        final Build b = PipelineTestUtils.createPipelineJobWithScript(jenkins.jobs, script).startBuild();
        assertBuild(b, true, USERNAME_CORRECT_MESSAGE, PASSWORD_CORRECT_MESSAGE);
    }

    @Test
    @WithPlugins({"credentials-binding@1.17", "mock-security-realm", "authorize-project"})
    public void pipelineUsernamePersonalCredentials() throws URISyntaxException, IOException {
        final String script = PipelineTestUtils.scriptForPipelineFromResourceWithParameters(this.getClass(), resolveScriptName("usernameSplitPasswordScript"), CRED_ID, CRED_USER, USERNAME_CORRECT_MESSAGE, CRED_PWD, PASSWORD_CORRECT_MESSAGE);
        this.testPersonalCredentials(UserPwdCredential.class, null, script, USERNAME_CORRECT_MESSAGE, PASSWORD_CORRECT_MESSAGE);
    }


    @Test
    @WithPlugins({"credentials-binding@1.17", "mock-security-realm", "authorize-project"})
    public void pipelineUsernameTogetherPersonalCredentials() throws URISyntaxException, IOException {
        final String script = PipelineTestUtils.scriptForPipelineFromResourceWithParameters(this.getClass(), resolveScriptName("usernameTogetherPasswordScript"), CRED_ID, CRED_USER + ":" + CRED_PWD, COMPLETE_PASSWORD_CORRECT_MESSAGE);
        this.testPersonalCredentials(UserPwdCredential.class, null, script, COMPLETE_PASSWORD_CORRECT_MESSAGE);
    }

    @Test
    @WithPlugins({"credentials-binding@1.17", "mock-security-realm", "authorize-project"})
    public void pipelineSecretTextPersonalCredentials() throws URISyntaxException, IOException {
        final String script = PipelineTestUtils.scriptForPipelineFromResourceWithParameters(this.getClass(), resolveScriptName("secretTextScript"), CRED_ID, SECRET_TEXT, SECRET_TEXT_CORRECT_MESSAGE);
        this.testPersonalCredentials(StringCredentials.class, null, script, SECRET_TEXT_CORRECT_MESSAGE);
    }

    @Test
    @WithPlugins({"credentials-binding@1.17", "mock-security-realm", "authorize-project"})
    public void pipelineSecretFilePersonalCredentials() throws URISyntaxException, IOException {
        final String script = PipelineTestUtils.scriptForPipelineFromResourceWithParameters(this.getClass(), resolveScriptName("secretFileScript"), CRED_ID, SECRET_FILE_TEXT, SECRET_FILE_CORRECT_MESSAGE);
        this.testPersonalCredentials(FileCredentials.class, SECRET_FILE, script, SECRET_FILE_CORRECT_MESSAGE);
    }

    @Test
    @WithPlugins({"credentials-binding@1.17", "mock-security-realm", "authorize-project"})
    public void pipelineSecretZipPersonalCredentials() throws URISyntaxException, IOException {
        final String script = PipelineTestUtils.scriptForPipelineFromResourceWithParameters(this.getClass(), resolveScriptName("secretZipFileScript"), CRED_ID, SECRET_ZIP_FILE_TEXT, SECRET_ZIP_FILE_CORRECT_MESSAGE);
        this.testPersonalCredentials(FileCredentials.class, SECRET_ZIP_FILE, script, SECRET_ZIP_FILE_CORRECT_MESSAGE);
    }

    @Test
    @WithPlugins({"credentials-binding@1.17", "mock-security-realm", "authorize-project"})
    public void pipelineSSHUserPrivateKeyCredentials() throws URISyntaxException, IOException {
        final String script = PipelineTestUtils.scriptForPipelineFromResourceWithParameters(this.getClass(), resolveScriptName("sshUserPrivateKeyScript"), CRED_ID, CRED_USER, USERNAME_CORRECT_MESSAGE, "", PASSWORD_CORRECT_MESSAGE, CRED_PWD, PRIVATE_KEY_CORRECT_MESSAGE);
        this.testPersonalCredentials(SshPrivateKeyCredential.class, null, script, USERNAME_CORRECT_MESSAGE, PASSWORD_CORRECT_MESSAGE, PRIVATE_KEY_CORRECT_MESSAGE);
    }

    private void authorizeUserToLaunchProject() {
        final GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.open();

        final ProjectDefaultBuildAccessControl control = security.addBuildAccessControl(ProjectDefaultBuildAccessControl.class);
        control.runAsSpecificUser(CREATED_USER);
        security.save();
    }

    private void assertBuild(final Build b, final boolean expectSuccess, final String... messagesToCheck) {
        if (expectSuccess) {
            b.shouldSucceed();
        } else {
            b.shouldFail();
        }

        //Job ending does not imply that console log has been completely updated so we wait
        waitForLogToBeFullyLoaded(b, "Finished");

        final String consoleOutput = b.getConsole();
        for (final String message : messagesToCheck) {
            Assert.assertThat(consoleOutput, Matchers.containsString(message));
        }
    }

    private void testPersonalCredentials(final Class<? extends BaseStandardCredentials> credClazz, final String credSecretResource, final String jobScript, final String... messagesToCheck) throws URISyntaxException {
        createMockUserAndLogin();
        CredentialsPage cp = createCredentialsPage(true);
        createCredentials(credClazz, cp, null, credSecretResource);

        final WorkflowJob job = PipelineTestUtils.createPipelineJobWithScript(jenkins.jobs, jobScript);
        Build b = job.startBuild();
        assertBuild(b, false, String.format("Could not find credentials entry with ID \'%s\'", CRED_ID));

        this.authorizeUserToLaunchProject();

        b = job.startBuild();
        assertBuild(b, true, messagesToCheck);
    }

    private void waitForLogToBeFullyLoaded(final Build b, String text) {
        waitFor(driver, new Matcher<WebDriver>("Console log is not fully loaded") {
            @Override
            public boolean matchesSafely(WebDriver item) {
                String pageText = CapybaraPortingLayerImpl.getPageContent(visit(b.getConsoleUrl()));
                return pageText.contains(text);
            }

            @Override
            public void describeMismatchSafely(WebDriver item, Description mismatchDescription) {
                mismatchDescription.appendText("Timeout waiting for console log to be fully loaded");
            }
        }, 30);
    }
}
