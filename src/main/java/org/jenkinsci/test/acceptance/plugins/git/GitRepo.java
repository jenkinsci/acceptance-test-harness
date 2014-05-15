package org.jenkinsci.test.acceptance.plugins.git;

import org.apache.commons.io.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.ProcessBuilder.Redirect.*;
import static java.util.Arrays.*;

/**
 * Manipulates git repository locally.
 *
 * TODO: this needs to define a mechanism to transfer git repo to the place accessible by Jenkins under test.
 *
 * @author Kohsuke Kawaguchi
 */
public class GitRepo implements Closeable {
    public final File dir;

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
        File dir = File.createTempFile("jenkins", "git");
        dir.delete();
        dir.mkdir();
        return dir;
    }

    public void git(String... args) throws IOException, InterruptedException {
        List<String> cmds = new ArrayList<>();
        cmds.add("git");
        cmds.addAll(asList(args));
        int r = new ProcessBuilder(cmds).directory(dir)
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
    }
}
