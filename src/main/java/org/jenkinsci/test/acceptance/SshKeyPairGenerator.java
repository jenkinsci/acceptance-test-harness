package org.jenkinsci.test.acceptance;

import org.apache.xerces.impl.dv.util.Base64;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.jvnet.hudson.crypto.RSAPublicKeyUtil;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.EnumSet;

import static java.nio.file.attribute.PosixFilePermission.*;

/**
 * {@link Provider} that generates key (and once and keep it in <tt>~/.ssh/jenkins-selenium-tests4j.pub</tt>
 *
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class SshKeyPairGenerator implements Provider<SshKeyPair>, com.google.inject.Provider<SshKeyPair> {
    @Override
    public SshKeyPair get() {
        File home = new File(System.getProperty("user.home"));
        File publicKey = new File(home,".ssh/jenkins-selenium-tests.pub");
        File privateKey = new File(home,".ssh/jenkins-selenium-tests.key");

        if (!publicKey.exists() || !privateKey.exists()) {
            try {
                generateKey(publicKey, privateKey);
            } catch (IOException|GeneralSecurityException e) {
                throw new AssertionError("Failed to generate a key", e);
            }
        }

        return new SshKeyPair(publicKey, privateKey);
    }

    private void generateKey(File publicKey, File privateKey) throws GeneralSecurityException, IOException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048,new SecureRandom()); // going beyond 2048 requires crypto extension
        KeyPair kp = gen.generateKeyPair();

        // write out the private key
        try (JcaPEMWriter w = new JcaPEMWriter(new FileWriter(privateKey))) {
            w.writeObject(kp);
        }
        // private key needs to be access restricted
        Files.setPosixFilePermissions( privateKey.toPath(), EnumSet.of(OWNER_READ));

        // and public key
        try (FileWriter w = new FileWriter(publicKey)) {
            w.write("ssh-rsa "+ Base64.encode(RSAPublicKeyUtil.encode(kp.getPublic()))+" generated-used-by-jenkins");
        }
    }
}
