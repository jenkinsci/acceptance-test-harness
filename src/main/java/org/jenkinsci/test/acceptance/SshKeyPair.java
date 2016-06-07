package org.jenkinsci.test.acceptance;

import com.google.inject.ProvidedBy;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.jvnet.hudson.crypto.RSAPublicKeyUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Security;

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
        return FileUtils.readFileToString(publicKey);
    }

    public String readPrivateKey() throws IOException {
        return FileUtils.readFileToString(privateKey);
    }

    /**
     * Computes EC2 fingerprint of this key.
     */
    public String getFingerprint() throws IOException, GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        Reader r = new BufferedReader(new StringReader(FileUtils.readFileToString(privateKey)));
        PEMParser pem = new PEMParser(r);

        KeyPair pair = (KeyPair) pem.readObject();
        return RSAPublicKeyUtil.getEC2FingerPrint(pair.getPublic());
    }
 }
