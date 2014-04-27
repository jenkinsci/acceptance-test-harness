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
 * Represents a server with SSHD.
 *
 * @author Kohsuke Kawaguchi
 */
@DockerFixture(id="ftpd",ports={21,7050,7051,7052,7053,7054,7055},bindIp="127.0.0.2")
public class FtpdContainer extends DockerContainer {

}
