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
 *
 */

package plugins;

import hudson.util.VersionNumber;
import org.jenkinsci.test.acceptance.AbstractPipelineTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsPage;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.credentials.UserPwdCredential;
import org.jenkinsci.test.acceptance.plugins.workflow_multibranch.GithubBranchSource;
import org.jenkinsci.test.acceptance.plugins.workflow_shared_library.WorkflowGithubSharedLibrary;
import org.jenkinsci.test.acceptance.plugins.workflow_shared_library.WorkflowSharedLibraryGlobalConfig;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests the use of a pipeline shared groovy library stored in GitHub
 */
@WithPlugins({"git@3.0.1", "workflow-multibranch", "github-branch-source", "pipeline-groovy-lib"})
public class SharedLibraryGithubTest extends AbstractPipelineTest {

    private static final String SHARED_LIBRARY_NAME = "Greeting";
    private static final String NAME = "myname";
    private static final String EXPECTED_OUTPUT_FROM_LIBRARY_SRC = "Hello my friend " + NAME;
    private static final String EXPECTED_OUTPUT_FROM_LIBRARY_VARS = "Hello from vars my friend " + NAME;
    private static final String GH_CRED_ID = "ghCred";
    private static VersionNumber GHBS_VERSION_INSTALLED = null;
    private boolean USE_NEW_UI_CONTROLS;

    @Before
    public void setup() {
        final CredentialsPage mc = new CredentialsPage(jenkins, ManagedCredentials.DEFAULT_DOMAIN);
        mc.open();
        final UserPwdCredential cred = mc.add(UserPwdCredential.class);
        cred.username.set("varyvoltest");
        cred.password.set("2bf9975e8904c46f51edceefe172156ba0b65731");
        cred.setId(GH_CRED_ID);
        mc.create();

        // Figure out what version of GHBS we're running with. This allows for
        // compatibility with both versions of the UI.
        GHBS_VERSION_INSTALLED = jenkins.getPlugin("github-branch-source").getVersion();
        VersionNumber usesNewUIControls = new VersionNumber("2.5.5");
        if (GHBS_VERSION_INSTALLED.isNewerThan(usesNewUIControls)) {
            USE_NEW_UI_CONTROLS = true;
        } else {
            USE_NEW_UI_CONTROLS = false;
        }


        jenkins.configure();
        final WorkflowGithubSharedLibrary sharedLibrary = new WorkflowSharedLibraryGlobalConfig(jenkins).addSharedLibrary(WorkflowGithubSharedLibrary.class);
        configureSharedLibrary(sharedLibrary);
        jenkins.save();
    }

    @Test
    public void testSharedLibraryFromGithub() {
        WorkflowJob job = createPipelineJobWithScript(scriptForPipeline("master"));
        Build b = job.startBuild().shouldSucceed();

        String consoleOutput = b.getConsole();
        assertThat(consoleOutput, containsString(EXPECTED_OUTPUT_FROM_LIBRARY_SRC));
        assertThat(consoleOutput, containsString(EXPECTED_OUTPUT_FROM_LIBRARY_VARS));

        job = createPipelineJobWithScript(scriptForPipeline("newTestBranch"));
        b = job.startBuild().shouldSucceed();

        consoleOutput = b.getConsole();
        assertThat(consoleOutput, containsString(EXPECTED_OUTPUT_FROM_LIBRARY_SRC));
        assertThat(consoleOutput, containsString(EXPECTED_OUTPUT_FROM_LIBRARY_VARS));
    }

    @Test
    @WithPlugins({"git@3.6.4", "github-branch-source@2.2.6"})
    public void testSharedLibraryFromGithub_tags() {
        final WorkflowJob job = createPipelineJobWithScript(scriptForPipeline("v1.0"));
        final Build b = job.startBuild().shouldSucceed();

        final String consoleOutput = b.getConsole();
        assertThat(consoleOutput, containsString(EXPECTED_OUTPUT_FROM_LIBRARY_SRC));
        assertThat(consoleOutput, containsString(EXPECTED_OUTPUT_FROM_LIBRARY_VARS));
    }

    public String scriptForPipeline(final String branch) {
        return "@Library('" + SHARED_LIBRARY_NAME + "@" + branch + "') _\n" +
                "\n" +
                "def greet = new varyvol.foo.Greeting()\n" +
                "greet.printGreeting('" + NAME + "')\n" +
                "\n" +
                "otherGreeting('" + NAME + "')";
    }

    private void configureSharedLibrary(final WorkflowGithubSharedLibrary sharedLibrary) {
        sharedLibrary.name.set(SHARED_LIBRARY_NAME);
        final GithubBranchSource source = sharedLibrary.selectSCM();
        source.credential(GH_CRED_ID);
        if (USE_NEW_UI_CONTROLS) {
            source.repoUrl("https://github.com/varyvoltest/pipeline-basic-shared-library.git");
        } else {
            source.owner("varyvoltest");
            source.selectRepository("pipeline-basic-shared-library");
        }
    }
}
