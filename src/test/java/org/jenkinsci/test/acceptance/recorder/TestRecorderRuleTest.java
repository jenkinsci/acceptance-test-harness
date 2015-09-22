package org.jenkinsci.test.acceptance.recorder;

import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
        String oldValue = System.getProperty("RECORDER_SAVE_ALL");
        System.setProperty("RECORDER_SAVE_ALL", "true");

        TestRecorderRule testRecorderRule = new TestRecorderRule();
        Description shouldNotRecordSuccessTestExecutionByDefault = Description.createTestDescription("org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest", "shouldRecordSuccessTestExecutionWhenSaveAll");
        testRecorderRule.starting(shouldNotRecordSuccessTestExecutionByDefault);

        System.out.println("Hello World");

        testRecorderRule.succeeded(shouldNotRecordSuccessTestExecutionByDefault);
        testRecorderRule.finished(shouldNotRecordSuccessTestExecutionByDefault);

        File outputFile = new File("target", "org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest-shouldRecordSuccessTestExecutionWhenSaveAll.mov");
        assertThat(outputFile.exists(), is(true));

        //Clean the field
        if(oldValue != null) {
            System.setProperty("RECORDER_SAVE_ALL", oldValue);
        } else {
            System.clearProperty("RECORDER_SAVE_ALL");
        }
        outputFile.delete();
    }

    @Test
    public void shouldNotRecordWhenRecorderIsDisabled() {
        String oldValue = System.getProperty("RECORDER_DISABLED");
        System.setProperty("RECORDER_DISABLED", "true");

        TestRecorderRule testRecorderRule = new TestRecorderRule();
        Description shouldNotRecordSuccessTestExecutionByDefault = Description.createTestDescription("org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest", "shouldNotRecordWhenRecorderIsDisabled");
        testRecorderRule.starting(shouldNotRecordSuccessTestExecutionByDefault);

        System.out.println("Hello World");

        //testRecorderRule.succeeded(shouldNotRecordSuccessTestExecutionByDefault);
        testRecorderRule.finished(shouldNotRecordSuccessTestExecutionByDefault);

        File outputFile = new File("target", "org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest-shouldNotRecordWhenRecorderIsDisabled.mov");
        assertThat(outputFile.exists(), is(false));

        //Clean the field
        if(oldValue != null) {
            System.setProperty("RECORDER_DISABLED", oldValue);
        } else {
            System.clearProperty("RECORDER_DISABLED");
        }
        outputFile.delete();
    }

}
