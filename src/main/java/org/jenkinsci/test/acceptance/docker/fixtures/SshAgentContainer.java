/*
 * The MIT License
 *
 * Copyright 2017 CloudBees, Inc.
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.jenkinsci.test.acceptance.plugins.ssh_slaves.SshSlaveLauncher;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

/**
 * Jenkins agent with various login methods.
 */
public class SshAgentContainer extends GenericContainer<SshAgentContainer> {

    public SshAgentContainer() {
        super(new ImageFromDockerfile("localhost/testcontainers/ath-ssh-agent", false)
                .withFileFromClasspath(".", SshAgentContainer.class.getName().replace('.', '/')));
        withExposedPorts(22);
    }

    public String getEncryptedEd25519PrivateKey() throws IOException {
        return load("ed25519.priv");
    }

    public String getEncryptedEd25519PrivateKeyPassphrase() throws IOException {
        return load("ed25519.pass");
    }

    public String getPrivateKeyString() throws IOException {
        return load("unsafe");
    }

    private String load(String resourceFile) throws IOException {
        try (var is = SshAgentContainer.class.getResourceAsStream("SshAgentContainer/" + resourceFile)) {
            return new String(is.readAllBytes(), StandardCharsets.US_ASCII);
        }
    }

    public SshSlaveLauncher configureSSHSlaveLauncher(DumbSlave agent) {
        SshSlaveLauncher launcher = agent.setLauncher(SshSlaveLauncher.class);
        launcher.host.set(getHost());
        launcher.port(getMappedPort(22));
        launcher.setSshHostKeyVerificationStrategy(SshSlaveLauncher.NonVerifyingKeyVerificationStrategy.class);
        return launcher;
    }
}
