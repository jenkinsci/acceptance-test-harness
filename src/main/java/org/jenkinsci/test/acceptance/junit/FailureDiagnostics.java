package org.jenkinsci.test.acceptance.junit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.codehaus.plexus.util.FileUtils;
import org.jenkinsci.test.acceptance.guice.TestName;
import org.jenkinsci.test.acceptance.guice.TestScope;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Attach diagnostic file related to a test failure.
 * <p>
 * The harness can attach any number of diagnostic files to be stored in /target/diagnostics/$TEST_NAME/.
 * The same 'kind' of diagnostic information is expected to use the same file/subdir name.
 *
 * @author ogondza
 */
@GlobalRule(priority = Integer.MIN_VALUE) // Make sure diagnostics are available for all other rules
@TestScope
public class FailureDiagnostics extends TestWatcher {

    private static String JUNIT_ATTACHMENT = "[[ATTACHMENT|%s]]";

    private final File dir;

    @Inject
    public FailureDiagnostics(TestName test) {
        this.dir = new File("target/diagnostics/" + test.get());
    }

    /**
     * Get test specific diagnostic directory.
     */
    private File getDir() {
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new Error("Directory " + dir + " could not be created. mkdirs operation returned false.");
            }
        } else {
            if (!dir.isDirectory()) {
                throw new Error(dir + " is not a directory.");
            }
        }
        return dir;
    }

    /**
     * Get ready for writing in diagnosis file.
     */
    public File touch(String filename) {
        return new File(getDir(), filename);
    }

    /**
     * Write string in diagnostic file.
     *
     * @param filename Name of the file
     * @param content Content to write.
     */
    public void write(String filename, String content) {
        try {
            try (FileWriter writer = new FileWriter(touch(filename))) {
                writer.write(content);
            }
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    public File mkdirs(String path) {
        File subdir = new File(getDir(), path);
        try {
            Files.createDirectories(subdir.toPath());
        } catch (IOException e) {
            throw new Error(e);
        }
        return subdir;
    }

    @Override
    protected void succeeded(Description description) {
        // Delete the directory if no diagnostics information written
        if (dir.exists()) {
            String[] files = dir.list();
            // Some diagnostic tools can produce data even though test succeeded
            // TODO introduce single switch for all diagnostic tools (yes/no/failure only)?
            if (files != null && files.length == 0) {
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    // Nah
                }
            }
        }
    }

    @Override
    public void failed(Throwable e, Description description) {
        if (dir.exists()) {
            try {
                Files.walk(dir.toPath()).forEach(p -> {
                    if (Files.isRegularFile(p)) {
                        //https://wiki.jenkins-ci.org/display/JENKINS/JUnit+Attachments+Plugin#JUnitAttachmentsPlugin-ByprintingoutthefilenameinaformatthatJenkinswillunderstand
                        System.out.printf((JUNIT_ATTACHMENT) + "%n", p.toAbsolutePath());
                    }
                });
            } catch (IOException x) {
                Logger.getLogger(FailureDiagnostics.class.getName()).log(Level.WARNING, "failed to search " + dir + " for attachments", x);
            }
        }
    }
}
