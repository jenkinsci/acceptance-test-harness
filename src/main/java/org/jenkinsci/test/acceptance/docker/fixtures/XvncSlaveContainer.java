/*
 * The MIT License
 *
 * Copyright 2015 Jesse Glick.
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

package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.test.acceptance.plugins.credentials.UserPwdCredential;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshCredentialDialog;
import org.jenkinsci.test.acceptance.plugins.ssh_slaves.SshSlaveLauncher;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Slave;

/**
 * A fixture consisting of a Jenkins slave which can run XVNC.
 */
@DockerFixture(id="xvnc-slave", ports=22)
public class XvncSlaveContainer extends DockerContainer {

    /**
     * Attaches the slave to Jenkins.
     * @param j the server
     * @return return a configured slave; call {@link Slave#save} after any other customizations to complete; use {@link Slave#waitUntilOnline} if desired
     */
    public Slave connect(Jenkins j) {
        // Some code from SshSlaveController could be applicable here, but looks too tricky to reuse.
        DumbSlave s = j.slaves.create(DumbSlave.class);
        SshSlaveLauncher launcher = s.setLauncher(SshSlaveLauncher.class);
        launcher.host.set(ipBound(22));
        launcher.port(port(22));
        launcher.pwdCredentials("jenkins", "jenkins");
        launcher.setSshHostKeyVerificationStrategy(SshSlaveLauncher.NonVerifyingKeyVerificationStrategy.class);
        s.remoteFS.set("/home/jenkins");
        s.setExecutors(1);
        return s;
    }
}
