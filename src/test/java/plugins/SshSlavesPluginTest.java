/*
 * The MIT License
 *
 * Copyright (c) 2015 Red Hat, Inc.
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

import static org.junit.Assert.assertTrue;

import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.credentials.UserPwdCredential;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshCredentialDialog;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.plugins.ssh_slaves.SshSlaveLauncher;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Inject;

@WithPlugins("ssh-slaves")
@Native("docker")
public class SshSlavesPluginTest extends AbstractJUnitTest {
    @Inject private Docker docker;

    private SshdContainer sshd;
    private DumbSlave slave;

    @Before public void setUp() {
        // Take advantage of preinstalled java
        sshd = docker.start(JavaContainer.class);

        slave = jenkins.slaves.create(DumbSlave.class);
        slave.setExecutors(1);
    }

    @Test public void connectWithPassword() {
        SshSlaveLauncher launcher = slave.setLauncher(SshSlaveLauncher.class);
        launcher.host.set(sshd.ipBound(22));
        launcher.port(sshd.port(22));

        SshCredentialDialog dia = launcher.addCredential();
        UserPwdCredential cred = dia.select(UserPwdCredential.class);
        cred.username.set("test");
        cred.password.set("test");
        cred.add();
        slave.save();

        verify();
    }

    @Test public void connectWithKey() {
        SshSlaveLauncher launcher = slave.setLauncher(SshSlaveLauncher.class);
        launcher.host.set(sshd.ipBound(22));
        launcher.port(sshd.port(22));

        SshCredentialDialog dia = launcher.addCredential();
        SshPrivateKeyCredential cred = dia.select(SshPrivateKeyCredential.class);
        cred.username.set("test");
        cred.enterDirectly(sshd.getPrivateKeyString());
        cred.add();
        slave.save();

        verify();
    }

    private void verify() {
        slave.waitUntilOnline();
        assertTrue(slave.isOnline());

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.setLabelExpression(slave.getName());
        job.addShellStep("test $NODE_NAME = '" + slave.getName() + "'");
        job.addShellStep("test $USER = test");
        job.save();
        job.startBuild().shouldSucceed();
    }
}
