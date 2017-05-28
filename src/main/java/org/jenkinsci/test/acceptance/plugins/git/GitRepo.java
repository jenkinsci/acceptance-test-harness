package org.jenkinsci.test.acceptance.plugins.git;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.test.acceptance.docker.fixtures.GitContainer;
import org.zeroturnaround.zip.ZipUtil;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import static java.lang.ProcessBuilder.Redirect.*;
import static java.nio.file.attribute.PosixFilePermission.*;
import static java.util.Collections.*;
import static org.jenkinsci.test.acceptance.docker.fixtures.GitContainer.*;

/**
 * Manipulates git repository locally.
 *
 * @author Kohsuke Kawaguchi
 */
public class GitRepo implements Closeable {
    public final File dir;

    /**
     * Path to the script that acts like SSH.
     */
    private File ssh;

    /**
     * Private key file that contains /ssh_keys/unsafe.
     */
    private File privateKey;

    public GitRepo() {
        dir = initDir();
        git("init");
        setIdentity(dir);
    }

    /**
     * Creates a new repo by cloning the given URL.
     */
    public GitRepo(final String url) {
        dir = initDir();
        git("clone", url, ".");
        setIdentity(dir);
    }

    /**
     * Configures and identity for the repo, just in case global config is not set.
     */
    private void setIdentity(File dir) {
        gitDir(dir, "config", "user.name", "Jenkins-ATH");
        gitDir(dir, "config", "user.email", "jenkins-ath@example.org");
    }

    private File initDir() {
        try {
            // FIXME: perhaps this logic that makes it use a separate key should be moved elsewhere?
            privateKey = File.createTempFile("ssh", "key");
            FileUtils.copyURLToFile(GitContainer.class.getResource("GitContainer/unsafe"), privateKey);
            Files.setPosixFilePermissions(privateKey.toPath(), singleton(OWNER_READ));

            ssh = File.createTempFile("jenkins", "ssh");
            FileUtils.writeStringToFile(ssh,
                    "#!/bin/sh\n" +
                            "exec ssh -o StrictHostKeyChecking=no -i " + privateKey.getAbsolutePath() + " \"$@\"");
            Files.setPosixFilePermissions(ssh.toPath(), new HashSet<>(Arrays.asList(OWNER_READ, OWNER_EXECUTE)));

            return createTempDir("git");
        } catch (IOException e) {
            throw new AssertionError("Can't initialize git directory", e);
        }
    }

    public void git(Object... args) {
        gitDir(this.dir, args);
    }

    /**
     * Execute git command in specified directory.
     */
    public void gitDir(File dir, Object... args) {
        List<String> cmds = new ArrayList<>();
        cmds.add("git");
        for (Object a : args) {
            if (a != null) {
                cmds.add(a.toString());
            }
        }
        ProcessBuilder pb = new ProcessBuilder(cmds);
        pb.environment().put("GIT_SSH", ssh.getAbsolutePath());

        String errorMessage = cmds + " failed";
        try {
            int r = pb.directory(dir)
                    .redirectInput(INHERIT)
                    .redirectError(INHERIT)
                    .redirectOutput(INHERIT).start().waitFor();
            if (r != 0) {
                throw new AssertionError(errorMessage);
            }
        } catch (InterruptedException | IOException e) {
            throw new AssertionError(errorMessage, e);
        }
    }

    /**
     * Appends the string "more" to the file "foo", adds it to the repository and commits it.
     *
     * @param message commit message
     */
    public void changeAndCommitFoo(final String message) {
        try {
            String fileName = "foo";
            try (FileWriter o = new FileWriter(new File(dir, fileName), true)) {
                o.write("more");
            }
            git("add", fileName);
            commit(message);
        } catch (IOException e) {
            throw new AssertionError("Can't append line to file foo", e);
        }
    }

    /**
     * Records all changes to the repository.
     *
     * @param message commit message
     */
    public void commit(final String message) {
        git("commit", "-m", message);
    }

    public void touch(final String fileName) {
        try {
            FileUtils.writeStringToFile(file(fileName), "");
        } catch (IOException e) {
            throw new AssertionError("Can't change file " + fileName, e);
        }
    }

    /**
     * Refers to a file relative to the workspace directory.
     */
    private File file(String name) {
        return new File(dir, name);
    }

    @Override
    public void close() throws IOException {
        FileUtils.deleteDirectory(dir);
        ssh.delete();
        privateKey.delete();
    }

    /**
     * Add a submodule to the main repository.
     *
     * @param submoduleName name of the submodule
     */
    public GitRepo addSubmodule(String submoduleName) {
        try {
            File submoduleDir = new File(createTempDir(submoduleName).getAbsolutePath() + "/" + submoduleName);
            submoduleDir.delete();
            submoduleDir.mkdir();

            gitDir(submoduleDir, "init");
            setIdentity(submoduleDir);
            try (FileWriter o = new FileWriter(new File(submoduleDir, "foo"), true)) {
                o.write("more");
            }

            gitDir(submoduleDir, "add", "foo");
            gitDir(submoduleDir, "commit", "-m", "Initial commit");

            git("submodule", "add", submoduleDir.getAbsolutePath());
            git("commit", "-am", "Added submodule");

            return this;
        } catch (IOException e) {
            throw new AssertionError("Can't create submodule " + submoduleName, e);
        }
    }

    private File createTempDir(String name) throws IOException {
        File tmp = File.createTempFile("jenkins", name);
        tmp.delete();
        tmp.mkdir();
        return tmp;
    }

    /**
     * Zip bare repository, copy to Docker container using sftp, then unzip.
     * The repo is now accessible over "ssh://git@ip:port/home/git/gitRepo.git"
     *
     * @param host IP of Docker container
     * @param port SSH port of Docker container
     */
    public void transferToDockerContainer(String host, int port) {
        try {
            Path zipPath = Files.createTempFile("git", "zip");
            File zippedRepo = zipPath.toFile();
            String zippedFilename = zipPath.getFileName().toString();
            ZipUtil.pack(new File(dir.getPath()), zippedRepo);

            Properties props = new Properties();
            props.put("StrictHostKeyChecking", "no");

            JSch jSch = new JSch();
            jSch.addIdentity(privateKey.getAbsolutePath());

            Session session = jSch.getSession("git", host, port);
            session.setConfig(props);
            session.connect();

            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            channel.cd("/home/git");
            channel.put(new FileInputStream(zippedRepo), zippedFilename);

            ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
            InputStream in = channelExec.getInputStream();
            channelExec.setCommand("unzip " + zippedFilename + " -d " + REPO_NAME);
            channelExec.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            int index = 0;
            while ((line = reader.readLine()) != null) {
                System.out.println(++index + " : " + line);
            }

            channelExec.disconnect();
            channel.disconnect();
            session.disconnect();
            Files.delete(zipPath);
        } catch (IOException | JSchException | SftpException e) {
            throw new AssertionError("Can't transfer git repository to docker container", e);
        }
    }

    private Path path(Path path) {
        return dir.toPath().resolve(path);
    }

    /**
     * Adds all files of the specified directory to this git repository.
     *
     * @param directory the files to add
     */
    public void addFilesIn(final URL directory) {
        try {
            Path source = Paths.get(directory.toURI());

            try (DirectoryStream<Path> paths = Files.newDirectoryStream(source)) {
                for (Path path : paths) {
                    Files.copy(path, path(path.getFileName()));
                }
            }
            git("add", "*");
        } catch (URISyntaxException | IOException e) {
            throw new AssertionError(String.format("Can't copy files from %s", directory), e);
        }
    }

    /**
     * Creates the specified branch in this repository.
     *
     * @param name the name of the branch
     */
    public void createBranch(final String name) {
        git("branch", name);
    }
}
