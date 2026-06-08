package org.jenkinsci.test.acceptance.plugins.git;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThrows;

import java.time.Duration;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

public class GitRepoTest {

    @Test
    @Issue("#2701")
    public void ensureGitCommandsExecuteQuickly() throws Exception {
        long start = System.nanoTime();
        try (GitRepo gr = new GitRepo()) {
            for (int i = 0; i < 5; i++) {
                gr.git("config", "testing.akey" + i, "" + i);
            }
        }
        long elapsed = System.nanoTime() - start;

        assertThat(
                "git commands should complete within 10 seconds",
                elapsed,
                lessThan(Duration.ofSeconds(10).toNanos()));
    }

    @Test
    public void ensureErrorsAreClear() throws Exception {
        try (GitRepo gr = new GitRepo()) {
            AssertionError thrown = assertThrows(AssertionError.class, () -> gr.git("non-existing-command"));
            assertThat(thrown.getMessage(), containsString("'non-existing-command' is not a git command."));
        }
    }
}
