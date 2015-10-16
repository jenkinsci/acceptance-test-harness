package org.jenkinsci.test.acceptance.junit;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.jenkinsci.test.acceptance.guice.TestName;
import org.jenkinsci.test.acceptance.guice.TestScope;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import com.google.inject.Inject;

/**
 * Attach diagnostics file related to a test failure.
 *
 * The harness can attach any number of diagnostic files to be stored in /target/diagnostics/$TEST_NAME/.
 * The same 'kind' of diagnostics information is expected to use the same file/subdir name.
 *
 * @author ogondza
 */
@GlobalRule
@TestScope
public class FailureDiagnostics extends TestWatcher {

    private final File dir;

    @Inject
    public FailureDiagnostics(TestName test) {
        this.dir = new File("target/diagnostics/" + test.get());
    }

    /**
     * Get test specific diagnostics directory.
     */
    public File getDir() {
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

    @Override
    protected void succeeded(Description description) {
        System.out.println("CLOSED " + dir);
        // Delete the directory if no diagnostics information written
        if (dir.exists()) {
            System.out.println("exists");
            String[] files = dir.list();
            // Some diagnostic tools can produce data even though test succeeded
            // TODO introduce single switch for all diagnostic tools (yes/no/failure only)?
            if (files != null && files.length == 0) {
                System.out.println("purge");
                try {
                    FileUtils.deleteDirectory(dir);
                } catch (IOException e) {
                    // Nah
                }
            }
        }
    }
}
