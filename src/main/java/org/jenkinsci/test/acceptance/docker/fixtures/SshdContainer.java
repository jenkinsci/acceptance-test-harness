package org.jenkinsci.test.acceptance.docker.fixtures;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;
import org.jenkinsci.utils.process.CommandBuilder;
import org.jenkinsci.utils.process.ProcessInputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.EnumSet;

import static java.nio.file.attribute.PosixFilePermission.*;

/**
 * Represents a server with SSHD.
 *
 * @author Kohsuke Kawaguchi
 */
@DockerFixture(id = "sshd", ports = 22)
public class SshdContainer extends DockerContainer {
    private File privateKey;
    private File privateKeyEnc;

    /**
     * Get plaintext Private Key File
     */
    public File getPrivateKey() {
        if (privateKey == null) {
            try {
                privateKey = File.createTempFile("ssh", "key");
                privateKey.deleteOnExit();
                FileUtils.copyURLToFile(resource("unsafe").url, privateKey);
                Files.setPosixFilePermissions(privateKey.toPath(), EnumSet.of(OWNER_READ));
            } catch (IOException e) {
                throw new RuntimeException("Not able to get the plaintext SSH key file. Missing file, wrong file permissions?!");
            }
        }
        return privateKey;
    }

    /**
     * Get encrypted Private Key File
     */
    public File getEncryptedPrivateKey() {
        if (privateKeyEnc == null) {
            try {
                privateKeyEnc = File.createTempFile("ssh_enc", "key");
                privateKeyEnc.deleteOnExit();
                FileUtils.copyURLToFile(resource("unsafe_enc_key").url, privateKeyEnc);
                Files.setPosixFilePermissions(privateKeyEnc.toPath(), EnumSet.of(OWNER_READ));
            } catch (IOException e) {
                throw new RuntimeException("Not able to get the encrypted SSH key file. Missing file, wrong file permissions?!");
            }
        }
        return privateKeyEnc;
    }

    public String getPrivateKeyString() {
        try {
            return new String(Files.readAllBytes(getPrivateKey().toPath()));
        } catch (IOException ex) {
            throw new AssertionError(ex);
        }
    }

    /**
     * Gets the SSH command line via <b>unencrypted</b> key.
     */
    public CommandBuilder ssh() {
        return new CommandBuilder("ssh")
                .add("-p", port(22), "-o", "StrictHostKeyChecking=no", "-i", getPrivateKey(), "test@" + ipBound(22));
    }


//    /**
//     * Gets the SSH command line via <b>encrypted</b> key.
//     * FIXME additonal script or tool needed like sshpass ("sshpass -fpassword.txt ssh -p 22 -o ... -i ... test@localhost")
//     * ssh does not allow passing a password as a parameter!
//     */
//    public CommandBuilder ssh_enc() throws IOException, InterruptedException {
//        return new CommandBuilder("ssh")
//                .add("-p",port(22),"-o","StrictHostKeyChecking=no","-i",getPrivateKey(),"test@localhost");
//    }

    /**
     * Login with SSH public key and run some command.
     */
    public void sshWithPublicKey(CommandBuilder cmd) throws IOException, InterruptedException {
        if (ssh().add(cmd).system() != 0)
            throw new AssertionError("ssh failed: " + cmd);
    }

    public ProcessInputStream popen(CommandBuilder cmd) throws IOException, InterruptedException {
        return ssh().add(cmd).popen();
    }
}
