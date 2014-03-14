package org.jenkinsci.test.acceptance.controller;

import com.google.inject.ProvidedBy;
import org.codehaus.plexus.util.FileUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;

/**
 * Configuration injected from outside that holds the SSH key pair that can be used for tests.
 *
 * This is used for example for test harness to remotely logging in to EC2 instances, or for
 * masters to login to slaves.
 *
 * <p>
 * Configuration script can either inject 'publicKeyFile' and 'privateKeyFile' parameter
 * separately, it can inject {@link SshKeyPairGenerator} as a provider.
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
@ProvidedBy(SshKeyPairGenerator.class)
public class SshKeyPair {
    public final File publicKey;
    public final File privateKey;

    @Inject
    public SshKeyPair(File publicKeyFile, File privateKeyFile) {
        this.publicKey = publicKeyFile;
        this.privateKey = privateKeyFile;
    }

    public String readPublicKey() throws IOException {
        return FileUtils.fileRead(publicKey,"UTF-8");
    }

    public String readPrivateKey() throws IOException {
        return FileUtils.fileRead(privateKey,"UTF-8");
    }
}
