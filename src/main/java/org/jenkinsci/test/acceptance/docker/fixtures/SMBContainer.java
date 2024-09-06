package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

/**
 * Represents a server with SMB.
 *
 * @author Tobias Meyer
 */
@DockerFixture(
        id = "smb",
        ports = {445, 139, 135})
public class SMBContainer extends DockerContainer implements IPasswordDockerContainer {
    private final String username = "test";

    private final String password = "test";

    /**
     * Gets the samba password of the samba user on the docker server
     *
     * @return Samba password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Gets the username of the samba user on the docker server
     *
     * @return Samba username
     */
    @Override
    public String getUsername() {
        return username;
    }
}
