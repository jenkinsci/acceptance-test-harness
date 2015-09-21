/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
        Description shouldNotRecordSuccessTestExecutionByDefault = Description.createTestDescription("org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest", "shouldNotRecordSuccessTestExecutionByDefault");
        testRecorderRule.starting(shouldNotRecordSuccessTestExecutionByDefault);

        System.out.println("Good Bye World");
        //succeeded is not called since a failure is simulated
        //testRecorderRule.succeeded(shouldNotRecordSuccessTestExecutionByDefault);
        testRecorderRule.finished(shouldNotRecordSuccessTestExecutionByDefault);

        File outputFile = new File("target", "org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest-shouldNotRecordSuccessTestExecutionByDefault.mov");
        assertThat(outputFile.exists(), is(true));

        //Clean the field
        outputFile.delete();
    }

    @Test
    public void shouldRecordSuccessTestExecutionWhenSaveAll() {
        String oldValue = System.getProperty("RECORDER_SAVE_ALL");
        System.setProperty("RECORDER_SAVE_ALL", "true");

        TestRecorderRule testRecorderRule = new TestRecorderRule();
        Description shouldNotRecordSuccessTestExecutionByDefault = Description.createTestDescription("org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest", "shouldNotRecordSuccessTestExecutionByDefault");
        testRecorderRule.starting(shouldNotRecordSuccessTestExecutionByDefault);

        System.out.println("Hello World");

        testRecorderRule.succeeded(shouldNotRecordSuccessTestExecutionByDefault);
        testRecorderRule.finished(shouldNotRecordSuccessTestExecutionByDefault);

        File outputFile = new File("target", "org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest-shouldNotRecordSuccessTestExecutionByDefault.mov");
        assertThat(outputFile.exists(), is(true));

        //Clean the field
        if(oldValue != null) {
            System.setProperty("RECORDER_SAVE_ALL", oldValue);
        }
        outputFile.delete();
    }

    @Test
    public void shouldNotRecordWhenRecorderIsDisabled() {
        String oldValue = System.getProperty("RECORDER_DISABLED");
        System.setProperty("RECORDER_DISABLED", "true");

        TestRecorderRule testRecorderRule = new TestRecorderRule();
        Description shouldNotRecordSuccessTestExecutionByDefault = Description.createTestDescription("org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest", "shouldNotRecordSuccessTestExecutionByDefault");
        testRecorderRule.starting(shouldNotRecordSuccessTestExecutionByDefault);

        System.out.println("Hello World");

        //testRecorderRule.succeeded(shouldNotRecordSuccessTestExecutionByDefault);
        testRecorderRule.finished(shouldNotRecordSuccessTestExecutionByDefault);

        File outputFile = new File("target", "org.jenkinsci.test.acceptance.recorder.TestRecorderRuleTest-shouldNotRecordSuccessTestExecutionByDefault.mov");
        assertThat(outputFile.exists(), is(false));

        //Clean the field
        if(oldValue != null) {
            System.setProperty("RECORDER_DISABLED", oldValue);
        }
        outputFile.delete();
    }

}
