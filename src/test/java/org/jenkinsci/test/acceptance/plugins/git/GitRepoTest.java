package org.jenkinsci.test.acceptance.plugins.git;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

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
        long elaspsed = System.nanoTime() - start;

        assertThat(
                "git commands should compelte within 10 seconds",
                elaspsed,
                lessThan(Duration.ofSeconds(10).toNanos()));
    }
}
