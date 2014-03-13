package org.jenkinsci.test.acceptance.slave;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.plexus.util.FileUtils;
import org.jenkinsci.test.acceptance.controller.Machine;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.po.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.po.SshSlave;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.*;

/**
 * @author Kohsuke Kawaguchi
 * @author Vivek Pandey
 */
public class SshSlaveController extends SlaveController {
    private final Machine machine;
    private final File privateKeyFile;

    @Inject
    public SshSlaveController(Machine machine,@Named("privateKeyFile")File privateKeyFile) {
        this.machine = machine;
        this.privateKeyFile = privateKeyFile;
    }

    @Override
    public Slave install(Jenkins j) {

        SshPrivateKeyCredential credential = new SshPrivateKeyCredential(j);

        try {
            credential.create("GLOBAL",machine.getUser(),FileUtils.fileRead(privateKeyFile,"UTF-8"));
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        return SshSlave.create(j, machine.getPublicIpAddress());
    }


    /**
     * Returns privateKey and publicKey files. Index 0 is privateKey and index 1 public key
     */
    private static KeyFiles createKeysIfNotExists(){
        File privKeyFile = new File(".jenkins_test/.ssh/id_dsa");
        File pubKeyFile = new File(".jenkins_test/.ssh/id_dsa.pub");
        if(!privKeyFile.exists() || !pubKeyFile.exists()){
            FileUtils.mkdir(privKeyFile.getParent());
            try {
                FileUtils.cleanDirectory(privKeyFile.getParent());
                privKeyFile.createNewFile();
                pubKeyFile.createNewFile();
            } catch (IOException e) {
                logger.error("Failed to clean ssh dir: "+privKeyFile.getParent());
            }

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

            try {
                FileUtils.fileWrite(privKeyFile,"UTF-8",priKey);
                FileUtils.fileWrite(pubKeyFile,"UTF-8",pubKey);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        }
        return new KeyFiles(privKeyFile,pubKeyFile);
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

    private static class KeyFiles{
        private final File privateKeyFile;
        private final File publicKeyFile;

        private KeyFiles(File privateKeyFile, File publicKeyFile) {
            this.privateKeyFile = privateKeyFile;
            this.publicKeyFile = publicKeyFile;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(SshSlaveController.class);
}
