package org.jenkinsci.test.acceptance.plugins.git_client.ssh_host_key_verification;

import org.jenkinsci.test.acceptance.po.Describable;

@Describable("org.jenkinsci.plugins.gitclient.verifier.KnownHostsFileVerificationStrategy")
public class KnownHostsFileStrategy extends SshHostKeyVerificationStrategy {
    @Override
    public String id() {
        return "1";
    }
}
