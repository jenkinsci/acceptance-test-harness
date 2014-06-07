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

import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;

/**
 * Represents a server with SMB.
 *
 * @author Tobias Meyer
 */
@DockerFixture(id="sshd",ports={445,139},bindIp="127.0.0.2")
public class SMBContainer extends DockerContainer {
    private final String username = "test";

    private final String password = "test";

    /**
     * Gets the ftp password of the ftp user on the docker server
     *
     * @return ftp password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the username of the ftp user on the docker server
     *
     * @return ftp username
     */
    public String getUsername() {
        return username;
    }

}
