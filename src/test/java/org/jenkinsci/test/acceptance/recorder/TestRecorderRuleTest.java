package org.jenkinsci.test.acceptance.recorder;

import org.junit.Test;
import org.junit.runner.Description;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class TestRecorderRuleTest {

    @Test
    public void shouldNotRecordSuccessTestExecutionByDefault() {
        TestRecorderRule testRecorderRule = new TestRecorderRule();
        Description shouldNotRecordSuccessTestExecutionByDefault = Description.createTestDescription("org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest", "shouldNotRecordSuccessTestExecutionByDefault");
        testRecorderRule.starting(shouldNotRecordSuccessTestExecutionByDefault);

        System.out.println("Hello World");

        testRecorderRule.succeeded(shouldNotRecordSuccessTestExecutionByDefault);
        testRecorderRule.finished(shouldNotRecordSuccessTestExecutionByDefault);

        File outputFile = new File("target", "org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest-shouldNotRecordSuccessTestExecutionByDefault.mov");
        assertThat(outputFile.exists(), is(false));

        //Clean the field
        outputFile.delete();
    }

    @Test
    public void shouldRecordFailingTestExecutionByDefault() {

        TestRecorderRule testRecorderRule = new TestRecorderRule();
        Description shouldNotRecordSuccessTestExecutionByDefault = Description.createTestDescription("org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest", "shouldRecordFailingTestExecutionByDefault");
        testRecorderRule.starting(shouldNotRecordSuccessTestExecutionByDefault);

        System.out.println("Good Bye World");
        //succeeded is not called since a failure is simulated
        //testRecorderRule.succeeded(shouldNotRecordSuccessTestExecutionByDefault);
        testRecorderRule.finished(shouldNotRecordSuccessTestExecutionByDefault);

        File outputFile = new File("target", "org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest-shouldRecordFailingTestExecutionByDefault.mov");
        assertThat(outputFile.exists(), is(true));

        //Clean the field
        outputFile.delete();
    }

    @Test
    public void shouldRecordSuccessTestExecutionWhenSaveAll() {

        //Since configured recorder option is static we need to set it manually in each test.
        TestRecorderRule.RECORDER_OPTION = TestRecorderRule.ALWAYS;

        TestRecorderRule testRecorderRule = new TestRecorderRule();
        Description shouldNotRecordSuccessTestExecutionByDefault = Description.createTestDescription("org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest", "shouldRecordSuccessTestExecutionWhenSaveAll");
        testRecorderRule.starting(shouldNotRecordSuccessTestExecutionByDefault);

        System.out.println("Hello World");

        testRecorderRule.succeeded(shouldNotRecordSuccessTestExecutionByDefault);
        testRecorderRule.finished(shouldNotRecordSuccessTestExecutionByDefault);

        File outputFile = new File("target", "org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest-shouldRecordSuccessTestExecutionWhenSaveAll.mov");
        assertThat(outputFile.exists(), is(true));

        TestRecorderRule.RECORDER_OPTION = TestRecorderRule.FAILURES;
        outputFile.delete();
    }

    @Test
    public void shouldNotRecordWhenRecorderIsDisabled() {

        //Since configured recorder option is static we need to set it manually in each test.
        TestRecorderRule.RECORDER_OPTION = TestRecorderRule.OFF;

        TestRecorderRule testRecorderRule = new TestRecorderRule();
        Description shouldNotRecordSuccessTestExecutionByDefault = Description.createTestDescription("org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest", "shouldNotRecordWhenRecorderIsDisabled");
        testRecorderRule.starting(shouldNotRecordSuccessTestExecutionByDefault);

        System.out.println("Hello World");

        //testRecorderRule.succeeded(shouldNotRecordSuccessTestExecutionByDefault);
        testRecorderRule.finished(shouldNotRecordSuccessTestExecutionByDefault);

        File outputFile = new File("target", "org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest-shouldNotRecordWhenRecorderIsDisabled.mov");
        assertThat(outputFile.exists(), is(false));

        //Clean the field
        TestRecorderRule.RECORDER_OPTION = TestRecorderRule.FAILURES;
        outputFile.delete();
    }

}
