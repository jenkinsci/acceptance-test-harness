/*
 * The MIT License
 *
 * Copyright (c) 2016 CloudBees, Inc.
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

package plugins;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.WithOS;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.msbuild.MSBuildStep;
import org.jenkinsci.test.acceptance.plugins.mstestrunner.MSTestRunnerBuildStep;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

@WithPlugins({"mstestrunner","msbuild"})
@Native({"MSTest"})
@WithOS(os = {WithOS.OS.WINDOWS})
public class MSTestRunnerPluginTest extends AbstractJUnitTest{

    private static final String FAILING_TESTS_PROJECT = "/mstestrunner_plugin/BankIncorrect/";
    private static final String SUCCESSFUL_TESTS_PROJECT = "/mstestrunner_plugin/BankCorrect/";

    private static final String VALID_TESTS_FILES = "BankTest\\bin\\Debug\\BankTest.dll";
    private static final String INVALID_TESTS_FILES = "InvalidFile";

    /**
     * Asserts that the logs for a certain build contains the text passed as parameter
     *
     * @param b the build
     * @param text the text to check
     */
    private void assertLogContains(Build b, String text) {
        String console = b.getConsole();
        assertThat(console, containsString(text));
    }

    @Test
    public void runTestsBasic() {
        FreeStyleJob job = msBuildJobWithTests(SUCCESSFUL_TESTS_PROJECT, VALID_TESTS_FILES, "", "", false);
        Build b = job.scheduleBuild();
        b.shouldSucceed();
        assertLogContains(b, "2/2 test(s) Passed");
    }

    @Test
    public void runTestsFail() {
        FreeStyleJob job = msBuildJobWithTests(FAILING_TESTS_PROJECT, VALID_TESTS_FILES, "", "", false);
        Build b = job.scheduleBuild();
        b.shouldFail();
        assertLogContains(b, "0/2 test(s) Passed, 2 Failed");
    }

    @Test
    public void ignoreFailingTests() throws InterruptedException, ExecutionException, URISyntaxException, IOException {
        FreeStyleJob job = msBuildJobWithTests(FAILING_TESTS_PROJECT, VALID_TESTS_FILES, "", "", true);
        Build b = job.scheduleBuild();
        b.shouldSucceed();
        assertLogContains(b, "0/2 test(s) Passed, 2 Failed");
    }

    @Test
    public void runTestsWithCategories() throws URISyntaxException, IOException, InterruptedException, ExecutionException {
        FreeStyleJob job = msBuildJobWithTests(SUCCESSFUL_TESTS_PROJECT, VALID_TESTS_FILES, "RealTests", "", false);
        Build b = job.scheduleBuild();
        b.shouldSucceed();
        assertLogContains(b, "1/1 test(s) Passed");
    }

    @Test
    public void runTestsWithCmdArguments() throws URISyntaxException, IOException, InterruptedException, ExecutionException {
        FreeStyleJob job = msBuildJobWithTests(SUCCESSFUL_TESTS_PROJECT, VALID_TESTS_FILES, "", "/usestderr", false);
        Build b = job.scheduleBuild();
        b.shouldSucceed();
        assertLogContains(b, "2/2 test(s) Passed");
        assertLogContains(b, "/usestderr");
    }

    @Test
    public void invalidTests() throws URISyntaxException, IOException, InterruptedException, ExecutionException {
        FreeStyleJob job = msBuildJobWithTests(SUCCESSFUL_TESTS_PROJECT, INVALID_TESTS_FILES, "", "", false);
        Build b = job.scheduleBuild();
        b.shouldFail();
        assertLogContains(b, "No test files was found");
    }

    /**
     * Creates a job with MSBuild and MSTestRunner steps
     *
     * @param workspacePath the path of the project to build and test
     * @param testFiles the path of the test files to use
     * @param categories the test categories to run
     * @param cmdLineArgs the command line options to use
     * @param ignoreFailingTests whether to ignore failing tests or not
     * @return
     */
    private FreeStyleJob msBuildJobWithTests(String workspacePath, String testFiles, String categories, String cmdLineArgs, boolean ignoreFailingTests) {
        if (workspacePath != null && testFiles != null) {
            FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class);
            job.copyDir(resource(workspacePath));
            // Add MSBuildStep to generate the dll files for the test step.
            MSBuildStep msBuildStep = job.addBuildStep(MSBuildStep.class);
            msBuildStep.setMSBuildFile("Bank.sln");
            // Add MSTestStep
            MSTestRunnerBuildStep msTestBuildStep = job.addBuildStep(MSTestRunnerBuildStep.class);
            msTestBuildStep.configure(testFiles, "resultFile", categories, cmdLineArgs, ignoreFailingTests);
            job.save();
            return job;
        } else {
            throw new IllegalArgumentException("Workspace and testFiles must be different from null.");
        }
    }
}
