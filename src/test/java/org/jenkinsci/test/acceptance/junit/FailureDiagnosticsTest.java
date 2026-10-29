package org.jenkinsci.test.acceptance.junit;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import org.jenkinsci.test.acceptance.guice.TestName;
import org.junit.Test;

public class FailureDiagnosticsTest {

    @Test
    public void reruns_get_their_own_directory() {
        TestName test =
                new TestName("reruns_get_their_own_directory(" + getClass().getName() + ")");
        Path first = new FailureDiagnostics(test).getPath();
        assertEquals(Path.of("target", "diagnostics", test.get()), first);
        // the files of a failed run are not mixed up with those of its rerun
        assertEquals(Path.of("target", "diagnostics", test.get() + "-run2"), new FailureDiagnostics(test).getPath());
    }
}
