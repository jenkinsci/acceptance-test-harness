package org.jenkinsci.test.acceptance.recorder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.test.acceptance.Matchers.existingFile;

import java.io.File;
import org.jenkinsci.test.acceptance.guice.TestName;
import org.jenkinsci.test.acceptance.junit.FailureDiagnostics;
import org.junit.Test;
import org.junit.runner.Description;

public class TestRecorderRuleTest {

    @Test
    public void shouldNotRecordSuccessTestExecutionByDefault() {

        final String oldMode = updateRecorderToDefaultOption();

        try {
            Description desc = description("shouldNotRecordSuccessTestExecutionByDefault");
            TestRecorderRule testRecorderRule = rule(desc);
            testRecorderRule.starting(desc);

            System.out.println("Hello World");

            testRecorderRule.succeeded(desc);
            testRecorderRule.finished(desc);

            File outputFile = outputFile(desc);
            assertThat(outputFile, is(not(existingFile())));

            //Clean the field
            outputFile.delete();
        } finally {
            restoreRecorderOption(oldMode);
        }
    }

    @Test
    public void shouldRecordFailingTestExecutionByDefault() {

        final String oldValue = updateRecorderToDefaultOption();

        try {
            Description desc = description("shouldRecordFailingTestExecutionByDefault");
            TestRecorderRule testRecorderRule = rule(desc);
            testRecorderRule.starting(desc);

            System.out.println("Good Bye World");
            //succeeded is not called since a failure is simulated
            //testRecorderRule.succeeded(shouldNotRecordSuccessTestExecutionByDefault);
            testRecorderRule.finished(desc);

            File outputFile = outputFile(desc);
            assertThat(outputFile, is(existingFile()));

            //Clean the field
            outputFile.delete();
        } finally {
            restoreRecorderOption(oldValue);
        }

    }

    @Test
    public void shouldRecordSuccessTestExecutionWhenSaveAll() {

        final String oldValue = updateRecorderToDefaultOption();
        try {
            //Since configured recorder option is static we need to set it manually in each test.
            TestRecorderRule.RECORDER_OPTION = TestRecorderRule.ALWAYS;

            Description desc = description("shouldRecordSuccessTestExecutionWhenSaveAll");
            TestRecorderRule testRecorderRule = rule(desc);
            testRecorderRule.starting(desc);

            System.out.println("Hello World");

            testRecorderRule.succeeded(desc);
            testRecorderRule.finished(desc);

            File outputFile = outputFile(desc);
            assertThat(outputFile, is(existingFile()));

            TestRecorderRule.RECORDER_OPTION = TestRecorderRule.FAILURES;
            outputFile.delete();
        } finally {
            restoreRecorderOption(oldValue);
        }
    }

    @Test
    public void shouldNotRecordWhenRecorderIsDisabled() {

        try {
            //Since configured recorder option is static we need to set it manually in each test.
            TestRecorderRule.RECORDER_OPTION = TestRecorderRule.OFF;

            Description desc = description("shouldNotRecordWhenRecorderIsDisabled");
            TestRecorderRule testRecorderRule = rule(desc);
            testRecorderRule.starting(desc);

            System.out.println("Hello World");

            //testRecorderRule.succeeded(shouldNotRecordSuccessTestExecutionByDefault);
            testRecorderRule.finished(desc);

            File outputFile = outputFile(desc);
            assertThat(outputFile, is(not(existingFile())));
            outputFile.delete();
        } finally {
            //Clean the field
            TestRecorderRule.RECORDER_OPTION = TestRecorderRule.FAILURES;
        }
    }

    private Description description(String method) {
        return Description.createTestDescription(getClass(), method);
    }

    private TestRecorderRule rule(Description desc) {
        return new TestRecorderRule(new FailureDiagnostics(new TestName(desc.getDisplayName())));
    }

    private File outputFile(Description desc) {
        return new File("target/diagnostics/" +desc + "/ui-recording.mov");
    }

    private String updateRecorderToDefaultOption() {
        final String envVar = System.getenv("RECORDER");
        final String recorder = System.getProperty("RECORDER");

        if ((envVar != null && !envVar.isEmpty()) || (recorder != null && !recorder.isEmpty())) {
            System.setProperty("RECORDER", TestRecorderRule.FAILURES);
            return recorder;
        }
        return null;
    }

    private void restoreRecorderOption(String oldValue) {
        if (oldValue != null) {
            System.setProperty("RECORDER", oldValue);
        } else {
            System.clearProperty("RECORDER");
        }
    }
}
