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
import org.jenkinsci.test.acceptance.plugins.mock_security_realm.MockSecurityRealm;
import org.jenkinsci.test.acceptance.plugins.script_security.ScriptApproval;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.junit.Test;

@WithPlugins({"script-security", "mock-security-realm", "matrix-auth", "groovy-postbuild"})
public class ScriptSecurityPluginTest extends AbstractJUnitTest {
    /** Admin user. */
    private static final String ADMIN = "admin";
    /** Normal user. */
    private static final String USER = "user";

    private void login(String user) {
        jenkins.login().doLogin(user);
    }

    @Test
    public void scriptNeedsApproval() throws Exception {
        final GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.open();
        {
            MockSecurityRealm realm = security.useRealm(MockSecurityRealm.class);
            realm.configure(ADMIN, USER);
            MatrixAuthorizationStrategy mas = security.useAuthorizationStrategy(MatrixAuthorizationStrategy.class);
            mas.addUser(ADMIN).admin();
            mas.addUser(USER).developer();
        }
        security.save();
        final FreeStyleJob job;
        login(USER);
        {
            job = jenkins.jobs.create();
            job.configure();
            job.addPublisher(GroovyPostBuildStep.class).setScript("def a = 4").setSandbox(false).setBehavior(GroovyPostBuildStep.FAILED);
            job.save();
            job.scheduleBuild().shouldFail(); // Script not approved
        }
        login(ADMIN);
        {
            ScriptApproval sa = new ScriptApproval(jenkins);
            sa.open();
            sa.find(job.name).approve();
        }
        login(USER);
        {
            job.scheduleBuild().shouldSucceed(); // Script approved
        }
    }

}
