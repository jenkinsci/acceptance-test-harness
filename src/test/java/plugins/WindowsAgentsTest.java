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

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.windowsslaves.WindowsSlaveLauncher;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;
import org.openqa.selenium.TimeoutException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

@WithPlugins({ "windows-slaves" }) @Ignore("Enable when ATH testing on windows is available.")
public class WindowsAgentsTest extends AbstractJUnitTest {
    public static final String WINDOWS_SLAVES_HOST_PROPERTY = "plugins.windowsslaves.host";
    public static final String WINDOWS_SLAVES_USER_PROPERTY = "plugins.windowsslaves.user";
    public static final String WINDOWS_SLAVES_PASSWORD_PROPERTY = "plugins.windowsslaves.pass";
    public static final String WINDOWS_SLAVES_REMOTE_PATH_PROPERTY = "plugins.windowsslaves.path";
    public static final String LABEL = "windows";

    private DumbSlave agent;
    private WindowsSlaveLauncher launcher;
    private String user = getWindowsProperty(WINDOWS_SLAVES_USER_PROPERTY);
    private String pass = getWindowsProperty(WINDOWS_SLAVES_PASSWORD_PROPERTY);
    private String remoteFS = getWindowsRemoteFSPathBase();
    private String host = getWindowsProperty(WINDOWS_SLAVES_HOST_PROPERTY);

    @After
    public void tearDown() throws ExecutionException, InterruptedException {
        agent.disconnect("Finishing test");
        agent.delete();
    }

    @Test
    public void shouldConnect() {
        setUpAgent(remoteFS, user, pass, host);
        agent.waitUntilOnline();
        assertTiedJobSucceeds();
    }

    @Test
    public void shouldNoConnectOnWrongUser() {
        setUpAgent(remoteFS, user + "wrong", pass, host);
        assertAgentOffline();
        agent.configure();
        launcher.user(user);
        agent.save();
        agent.waitUntilOnline();
    }

    @Test
    public void shouldNoConnectOnWrongPass() {
        setUpAgent(remoteFS, user, pass + "wrong", host);
        assertAgentOffline();
        agent.configure();
        launcher.password(pass);
        agent.save();
        agent.waitUntilOnline();
    }

    private void setUpAgent(String remotePath, String user, String password, String host) {
        agent = jenkins.slaves.create(DumbSlave.class);
        agent.setExecutors(1);
        agent.remoteFS.set(remotePath + "\\" + agent.getName());
        agent.setLabels(LABEL);
        launcher = agent.setLauncher(WindowsSlaveLauncher.class);
        launcher.user(user).password(password).host(host).runAs("Use Administrator account given above");
        agent.save();
    }

    private void assertTiedJobSucceeds() {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.setLabelExpression(LABEL);
        job.save();
        job.scheduleBuild().shouldSucceed();
        assertBuiltOnAgent(job);

    }

    private void assertBuiltOnAgent(FreeStyleJob job) {
        agent.open();
        waitFor(by.link("Build History")).click();
        waitFor(by.href("/job/" + job.name + "/"));
    }

    private void assertAgentOffline() {
        try {
            waitFor().withTimeout(30, TimeUnit.SECONDS).until(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    return agent.isOnline();
                }
            });
            fail("Slave has become online when it should remain offline");
        } catch (TimeoutException timeout) {
            //empty by design this is the expected behaviour
        }
    }

    private String getWindowsProperty(String property) {
        String p = System.getProperty(property);
        if (p == null || p.isEmpty()) {
            throw new AssumptionViolatedException(String.format("No windows property (%s) found, ignoring windows slaves test", property));
        }
        return p;
    }

    private String getWindowsRemoteFSPathBase() {
        String path = getWindowsProperty(WINDOWS_SLAVES_REMOTE_PATH_PROPERTY);
        if (path.endsWith("\\")) { //sanity check to avoid problems with last \ characters
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
}
