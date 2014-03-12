package org.jenkinsci.test.acceptance.slave;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.plexus.util.FileUtils;
import org.jenkinsci.test.acceptance.controller.Machine;
import org.jenkinsci.test.acceptance.controller.Ssh;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.RemoteSshSlave;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.po.SshPrivateKeyCredential;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class SshSlaveController extends SlaveController {
    private final Machine machine;

    @Inject
    public SshSlaveController(Machine machine) {
        this.machine = machine;
    }

    @Override
    public Slave install(Jenkins j) {
        SecureRandom random;
        KeyPairGenerator keyGen;
        try {
            keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
            random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new AssertionError(e);
        }
        keyGen.initialize(1024, random);
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey priv = pair.getPrivate();
        PublicKey pub = pair.getPublic();

        String priKey = new String(Base64.encodeBase64(priv.getEncoded()));
        String pubKey = new String(Base64.encodeBase64(pub.getEncoded()));

        SshPrivateKeyCredential credential = new SshPrivateKeyCredential(j);

        try {
            File sshPrivKeyFile = FileUtils.createTempFile("id","dsa", new File(".jenkins_test"));
            FileUtils.fileWrite(sshPrivKeyFile,priKey);

            File sshPubKeyFile = FileUtils.createTempFile("id","dsa.pub", new File(".jenkins_test"));
            FileUtils.fileWrite(sshPubKeyFile,pubKey);
            Ssh ssh = machine.connect();

            ssh.copyTo(sshPrivKeyFile.getAbsolutePath(), "id_dsa", ".ssh");
            ssh.copyTo(sshPubKeyFile.getAbsolutePath(), "id_dsa.pub", ".ssh");
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        credential.create("GLOBAL",machine.getUser(),priKey);

        return RemoteSshSlave.create(j,priKey,machine.getPublicIpAddress());
    }

    @Override
    public void start() {
        // no-op
    }

    @Override
    public void stop() {
        // no-op
    }

    @Override
    public void close() throws IOException {

    }
}
