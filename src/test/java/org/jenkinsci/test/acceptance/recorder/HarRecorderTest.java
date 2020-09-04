package org.jenkinsci.test.acceptance.recorder;

import com.browserup.bup.BrowserUpProxy;
import org.jenkinsci.test.acceptance.junit.FailureDiagnostics;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.Description;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.jenkinsci.test.acceptance.Matchers.existingFile;
import static org.junit.Assert.assertThat;

public class HarRecorderTest {
    @Rule
    public TestName name = new TestName();

    @Test
    public void shouldRecordFailingTestExecutionByDefault() {
        Description desc = description();
        HarRecorder harRecorder = rule(desc);
        HarRecorder.CAPTURE_HAR = HarRecorder.State.FAILURES_ONLY;
        BrowserUpProxy proxy = HarRecorder.getProxy();
        proxy.newHar("jenkins");

        System.out.println("Good Bye World");
        harRecorder.failed(new Exception(),desc);

        File outputFile = outputFile(desc);
        assertThat(outputFile, is(existingFile()));

        //Clean the field
        outputFile.delete();
    }

    private HarRecorder rule(Description desc) {
        return new HarRecorder(new FailureDiagnostics(new org.jenkinsci.test.acceptance.guice.TestName(desc.getDisplayName())));
    }

    private Description description() {
        return Description.createTestDescription(getClass(), name.getMethodName());
    }


    private File outputFile(Description desc) {
        return new File("target/diagnostics/" +desc + "/jenkins.har");
    }
}
