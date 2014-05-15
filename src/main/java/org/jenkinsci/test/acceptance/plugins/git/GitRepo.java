package org.jenkinsci.test.acceptance.plugins.git;

import org.apache.commons.io.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static java.lang.ProcessBuilder.Redirect.*;
import static java.nio.file.attribute.PosixFilePermission.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;

/**
 * Manipulates git repository locally.
 *
 * TODO: this needs to define a mechanism to transfer git repo to the place accessible by Jenkins under test.
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
     * Private key file that contains /ssh_keys/unsafe
     */
    private File privateKey;

    public GitRepo() throws IOException, InterruptedException {
        dir = initDir();
        git("init");
    }

    /**
     * Creates a new repo by cloning the given URL
     */
    public GitRepo(String url) throws IOException, InterruptedException {
        dir = initDir();
        git("clone",url,".");
    }

    private File initDir() throws IOException {
        // FIXME: perhaps this logic that makes it use a separate key should be moved elsewhere?
        privateKey = File.createTempFile("ssh", "key");
        FileUtils.copyURLToFile(GitRepo.class.getResource("/ssh_keys/unsafe"), privateKey);
        Files.setPosixFilePermissions(privateKey.toPath(), singleton(OWNER_READ));

        ssh = File.createTempFile("jenkins", "ssh");
        FileUtils.writeStringToFile(ssh,
                "#!/bin/sh\n"+
                "exec ssh -o StrictHostKeyChecking=no -i "+privateKey.getAbsolutePath()+" \"$@\"");
        Files.setPosixFilePermissions(ssh.toPath(), new HashSet<>(Arrays.asList(OWNER_READ, OWNER_EXECUTE)));

        File dir = File.createTempFile("jenkins", "git");
        dir.delete();
        dir.mkdir();
        return dir;
    }

    public void git(String... args) throws IOException, InterruptedException {
        List<String> cmds = new ArrayList<>();
        cmds.add("git");
        cmds.addAll(asList(args));
        ProcessBuilder pb = new ProcessBuilder(cmds);
        pb.environment().put("GIT_SSH",ssh.getAbsolutePath());

        int r = pb.directory(dir)
                .redirectInput(INHERIT)
                .redirectError(INHERIT)
                .redirectOutput(INHERIT).start().waitFor();
        if (r!=0)
            throw new Error(cmds+" failed");
    }

    /**
     * Creates a new commit.
     */
    public void commit(String msg) throws IOException, InterruptedException {
        try (FileWriter o = new FileWriter(new File(dir, "foo"), true)) {
            o.write("more");
        }
        git("add","foo");
        git("commit","-m",msg);
    }

    @Override
    public void close() throws IOException {
        FileUtils.deleteDirectory(dir);
        ssh.delete();
        privateKey.delete();
    }
}
