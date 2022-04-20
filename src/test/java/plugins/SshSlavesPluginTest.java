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

import com.google.inject.Inject;
import hudson.util.VersionNumber;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.SshAgentContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsPage;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshCredentialDialog;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.plugins.ssh_slaves.SshSlaveLauncher;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.jvnet.hudson.test.Issue;
import org.openqa.selenium.NoSuchElementException;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

@WithPlugins({"ssh-slaves@1.11", "credentials@2.1.10", "ssh-credentials@1.12"})
@Category(DockerTest.class)
@WithDocker
public class SshSlavesPluginTest extends AbstractJUnitTest {

    public static final String REMOTE_FS = "/tmp";

    @Inject private DockerContainerHolder<SshAgentContainer> docker;

    private SshAgentContainer sshd;
    private DumbSlave slave;

    private void setUp() {
        sshd = docker.get();

        slave = jenkins.slaves.create(DumbSlave.class);
        slave.setExecutors(1);
        slave.remoteFS.set(REMOTE_FS);
    }

    @Test
    @Since("1.560")
    public void newAgent() {
        // Just to make sure the permanent agent is set up properly, we should seed it
        // with a FS root and executors
        final DumbSlave s = jenkins.slaves.create(DumbSlave.class);
        {
            SshSlaveLauncher l = s.setLauncher(SshSlaveLauncher.class);

            String username = "user1";
            String privateKey = "1212122112";
            String description = "Ssh key";

            l.host.set("127.0.0.1");
            l.credentialsId.resolve();  // make sure this exists

            try {
                l.credentialsId.select(String.format("%s (%s)", username, description));
                fail();
            } catch (NoSuchElementException e) {
                //ignore
            }

            SshCredentialDialog f = l.addCredential();
            {
                SshPrivateKeyCredential sc = f.select(SshPrivateKeyCredential.class);
                sc.description.set(description);
                sc.username.set(username);
                sc.selectEnterDirectly().privateKey.set(privateKey);
            }
            f.add();

            l.credentialsId.select(String.format("%s (%s)", username, description));
        }
        s.save();
    }

    @Test
    public void newSlaveWithExistingCredential() throws Exception {
        String username = "xyz";
        String description = "ssh_creds";
        String privateKey = "1212121122121212";

        CredentialsPage c = new CredentialsPage(jenkins, "_");
        c.open();

        SshPrivateKeyCredential sc = c.add(SshPrivateKeyCredential.class);
        sc.username.set(username);
        sc.description.set(description);
        sc.selectEnterDirectly().privateKey.set(privateKey);

        c.create();

        //now verify
        ManagedCredentials mc = new ManagedCredentials(jenkins);
        String href = mc.credentialById("ssh_creds");
        c.setConfigUrl(href);
        verifyValueForCredential(c, sc.username, username);

        // See https://jenkins.io/doc/developer/security/secrets/#secrets-and-configuration-forms, available from Jenkins 2.171
        if (jenkins.getVersion().isNewerThan(new VersionNumber("2.170"))) {
            verifyUnexpectedValueForCredential("Credentials in plain text should not be accessible from Web UI",
                    c, sc.selectEnterDirectly().privateKey, privateKey);
        }

        // Just to make sure the dumb slave is set up properly, we should seed it
        // with a FS root and executors
        final DumbSlave s = jenkins.slaves.create(DumbSlave.class);
        SshSlaveLauncher l = s.setLauncher(SshSlaveLauncher.class);
        l.host.set("127.0.0.1");

        l.credentialsId.select(String.format("%s (%s)", username, description));
    }

    private void verifyValueForCredential(CredentialsPage cp, Control element, String expected) {
        cp.configure();
        assert(element.exists());
        assertThat(element.resolve().getAttribute("value"), containsString(expected));
    }

    private void verifyUnexpectedValueForCredential(String message, CredentialsPage cp, Control element, String notExpected) {
        cp.configure();
        assert(element.exists());
        assertThat(message, element.resolve().getAttribute("value"), not(containsString(notExpected)));
    }

    @Test public void connectWithPassword() {
        setUp();
        configureDefaultSSHSlaveLauncher()
            .pwdCredentials("test", "test");
        slave.save();

        verify();
    }

    @Test public void connectWithKey() {
        setUp();
        configureDefaultSSHSlaveLauncher()
            .keyCredentials("test", sshd.getPrivateKeyString(), null);
        slave.save();

        verify();
    }

    @Issue("JENKINS-46754")
    @Test public void connectWithEd25519EncKey() {
        setUp();
        configureDefaultSSHSlaveLauncher()
            .keyCredentials("test", sshd.getEncryptedEd25519PrivateKey(), sshd.getEncryptedEd25519PrivateKeyPassphrase());
        slave.save();
        verify();
    }

    @Test public void unableToConnectWrongPort() {
        setUp();
        configureSSHSlaveLauncher(sshd.ipBound(22), 1234).pwdCredentials("test", "test");
        slave.save();
        
        // Wait for connection attempt to fail
        waitForLogMessage("Connection refused");
    }
    
    @Test public void unableToConnectWrongCredentials() {
        setUp();
        configureDefaultSSHSlaveLauncher().pwdCredentials("unexsisting", "unexsisting");
        slave.save();
        
        // Wait for connection attempt to fail
        waitForLogMessage("Authentication failed");
    }
    
    @Test public void customJavaPath() {
        setUp();
        SshSlaveLauncher launcher = configureDefaultSSHSlaveLauncher().pwdCredentials("test", "test");

        String javaPath = "/usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java";
        if (System.getProperty("os.arch").equals("aarch64")) {
            javaPath = "/usr/lib/jvm/java-8-openjdk-arm64/jre/bin/java";
        }
        launcher.javaPath.set(javaPath);
        slave.save();
    
        verify();
        verifyLog("java-8-openjdk");
    }
    
    @Test public void jvmOptions() {
        String option = "-XX:-PrintGC";

        setUp();
        SshSlaveLauncher launcher = configureDefaultSSHSlaveLauncher().pwdCredentials("test", "test");
        
        launcher.jvmOptions.set(option);
        slave.save();
        
        verify();
        verifyLog(option);
    }
    
    @Test public void customStartup() {
        setUp();
        SshSlaveLauncher launcher = configureDefaultSSHSlaveLauncher().pwdCredentials("test", "test");
        
        launcher.prefixCmd.set("sh -c \"");
        launcher.suffixCmd.set("\"");
        slave.save();
        
        
        verify();
        verifyLog("sh -c \"");
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
    
    private void waitForLogMessage(final String message) {
        waitFor().withTimeout(5, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return slave.getLog().contains(message);
            }

            @Override public String toString() {
                return "slave log to contain '" + message + "':\n" + slave.getLog();
            }
        });
    }
    private void verifyLog(String message) {
        assertThat(slave.getLog(), containsString(message));
    }
    
    private SshSlaveLauncher configureDefaultSSHSlaveLauncher() {
        return configureSSHSlaveLauncher(sshd.ipBound(22), sshd.port(22));
    }
    
    private SshSlaveLauncher configureSSHSlaveLauncher(String host, int port) {
        SshSlaveLauncher launcher = slave.setLauncher(SshSlaveLauncher.class);
        launcher.host.set(host);
        launcher.port(port);
        launcher.setSshHostKeyVerificationStrategy(SshSlaveLauncher.NonVerifyingKeyVerificationStrategy.class);
        return launcher;
    }
}
