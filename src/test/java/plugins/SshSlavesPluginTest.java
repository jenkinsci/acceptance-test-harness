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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.ssh_slaves.SshSlaveLauncher;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Inject;
import org.junit.experimental.categories.Category;

@WithPlugins({"ssh-slaves@1.11", "credentials@2.1.10", "ssh-credentials@1.12"})
@Category(DockerTest.class)
@WithDocker
public class SshSlavesPluginTest extends AbstractJUnitTest {

    public static final String REMOTE_FS = "/tmp";

    @Inject private DockerContainerHolder<JavaContainer> docker;

    private SshdContainer sshd;
    private DumbSlave slave;

    @Before public void setUp() {
        sshd = docker.get();

        slave = jenkins.slaves.create(DumbSlave.class);
        slave.setExecutors(1);
        slave.remoteFS.set(REMOTE_FS);
    }

    @Test public void connectWithPassword() {
        configureDefaultSSHSlaveLauncher()
            .pwdCredentials("test", "test");
        slave.save();

        verify();
    }

    @Test public void connectWithKey() {
        configureDefaultSSHSlaveLauncher()
            .keyCredentials("test", sshd.getPrivateKeyString());
        slave.save();

        verify();
    }

    @Test public void unableToConnectWrongPort() {
        configureSSHSlaveLauncher(sshd.ipBound(22), 1234).pwdCredentials("test", "test");
        slave.save();
        
        // Wait for connection attempt to fail
        waitForLogMessage("Connection refused");
    }
    
    @Test public void unableToConnectWrongCredentials() {
        configureDefaultSSHSlaveLauncher().pwdCredentials("unexsisting", "unexsisting");
        slave.save();
        
        // Wait for connection attempt to fail
        waitForLogMessage("Authentication failed");
    }
    
    @Test public void customJavaPath() {
        SshSlaveLauncher launcher = configureDefaultSSHSlaveLauncher().pwdCredentials("test", "test");
        
        launcher.javaPath.set("/usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java");
        slave.save();
    
        verify();
        verifyLog("java-8-openjdk-amd64");
    }
    
    @Test public void jvmOptions() {
        String option = "-XX:-PrintGC";
        
        SshSlaveLauncher launcher = configureDefaultSSHSlaveLauncher().pwdCredentials("test", "test");
        
        launcher.jvmOptions.set(option);
        slave.save();
        
        verify();
        verifyLog(option);
    }
    
    @Test public void customStartup() {
        SshSlaveLauncher launcher = configureDefaultSSHSlaveLauncher().pwdCredentials("test", "test");
        
        launcher.prefixCmd.set("sh -c \"");
        launcher.suffixCmd.set("\"");
        slave.save();
        
        
        verify();
        verifyLog("sh -c \"cd \"" + REMOTE_FS + "\" && java  -jar slave.jar\"");
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
        assertTrue(slave.getLog().contains(message));
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
