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
@DockerFixture(id="sshd",ports=22)
public class SshdContainer extends DockerContainer {
    File privateKey;

    public File getPrivateKey() throws IOException {
        if (privateKey==null) {
            privateKey = File.createTempFile("ssh", "key");
            privateKey.deleteOnExit();
            FileUtils.copyURLToFile(resource("unsafe").url,privateKey);
            Files.setPosixFilePermissions(privateKey.toPath(), EnumSet.of(OWNER_READ));
        }
        return privateKey;
    }

    /**
     * Gets the SSH command line.
     */
    public CommandBuilder ssh() throws IOException, InterruptedException {
        return new CommandBuilder("ssh")
                .add("-p",port(22),"-o","StrictHostKeyChecking=no","-i",getPrivateKey(),"test@localhost");
    }

    /**
     * Login with SSH public key and run some command.
     */
    public void sshWithPublicKey(CommandBuilder cmd) throws IOException, InterruptedException {
        if (ssh().add(cmd).system()!=0)
            throw new AssertionError("ssh failed: "+cmd);
    }

    public ProcessInputStream popen(CommandBuilder cmd) throws IOException, InterruptedException {
        return ssh().add(cmd).popen();
    }
}
