/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees, Inc.
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

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.groovy_postbuild.GroovyPostBuildStep;
import org.jenkinsci.test.acceptance.plugins.matrix_auth.MatrixAuthorizationStrategy;
import org.jenkinsci.test.acceptance.plugins.script_security.ScriptApproval;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.JenkinsDatabaseSecurityRealm;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@WithPlugins({"script-security", "matrix-auth"})
public class ScriptSecurityPluginTest extends AbstractJUnitTest {
    /** Admin user. */
    private static final String ADMIN = "admin";
    /** Normal user. */
    private static final String USER = "user";

    private void login(String user) {
        jenkins.login().doLogin(user);
    }

    @Before
    public void setUp() {
        final GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.open();
        {
            JenkinsDatabaseSecurityRealm realm = security.useRealm(JenkinsDatabaseSecurityRealm.class);
            realm.allowUsersToSignUp(true);
            security.save();
            
            realm.signup(ADMIN);
            realm.signup(USER);
            
            security.open();
            MatrixAuthorizationStrategy mas = security.useAuthorizationStrategy(MatrixAuthorizationStrategy.class);
            mas.addUser(ADMIN).admin();
            mas.addUser(USER).developer();
        }
        security.save();
    }

    private FreeStyleJob createFailedJobWithGroovyPostBuild(String script, boolean sandbox) {
        final FreeStyleJob job;
        login(USER);
        {
            job = jenkins.jobs.create();
            job.configure();
            job.addPublisher(GroovyPostBuildStep.class).setScript(script).setSandbox(sandbox).setBehavior(GroovyPostBuildStep.FAILED);
            job.save();
            job.scheduleBuild().shouldFail(); // Script not approved
        }
        return job;
    }

    private WorkflowJob createFailedPipeline(String script, boolean sandbox) {
        final WorkflowJob job;
        login(USER);
        {
            job = jenkins.jobs.create(WorkflowJob.class);
            job.configure();
            job.script.set(script);
            job.sandbox.check(sandbox);
            job.save();
            job.scheduleBuild().shouldFail(); // Script not approved
        }
        return job;
    }

    private void shouldSucceed(Job job) {
        login(USER);
        job.scheduleBuild().shouldSucceed();
    }

    @Test
    @WithPlugins("groovy-postbuild")
    public void scriptNeedsApproval() throws Exception {
        final FreeStyleJob job = createFailedJobWithGroovyPostBuild("def a = 4", false);
        login(ADMIN);
        {
            ScriptApproval sa = new ScriptApproval(jenkins);
            sa.open();
            sa.find(job.name).approve();
        }
        shouldSucceed(job); // Script approved
    }

    @Test
    @WithPlugins("groovy-postbuild")
    public void signatureNeedsApproval() throws Exception {
        final FreeStyleJob job = createFailedJobWithGroovyPostBuild("def h = java.lang.System.getProperties()", true);
        login(ADMIN);
        {
            ScriptApproval sa = new ScriptApproval(jenkins);
            sa.open();
            sa.findSignature("getProperties").approve();
        }
        shouldSucceed(job); // Script approved
    }

    @Test
    @WithPlugins({"workflow-job", "workflow-cps"})
    public void pipelineNeedsApproval() throws Exception {
        final WorkflowJob job = createFailedPipeline("def a = 5", false);
        login(ADMIN);
        {
            ScriptApproval sa = new ScriptApproval(jenkins);
            sa.open();
            sa.find(job.name).approve();
        }
        shouldSucceed(job); // Script approved
    }

    @Test
    @WithPlugins({"workflow-job","workflow-cps"})
    @Ignore("https://github.com/jenkinsci/acceptance-test-harness/issues/1444")
    public void pipelineSignatureNeedsApproval() throws Exception {
        final WorkflowJob job = createFailedPipeline("def h = java.lang.System.getProperty('java.version')", true);
        login(ADMIN);
        {
            ScriptApproval sa = new ScriptApproval(jenkins);
            sa.open();
            sa.findSignature("getProperty").approve();
        }
        shouldSucceed(job); // Script approved
    }

}
