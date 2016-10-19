/*
 * The MIT License
 *
 * Copyright (c) 2014 Sony Mobile Communications Inc.
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

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.emma.EmmaPublisher;
import org.jenkinsci.test.acceptance.plugins.emma.EmmaResultsPage;
import org.jenkinsci.test.acceptance.plugins.maven.MavenBuildStep;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Job;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Checks the successfully execution of Emma coverage reports.
 * Reuses Jacoco test files.
 *
 * @author Orjan Percy
 */
@WithPlugins("emma")
public class EmmaPluginTest extends AbstractJUnitTest {

    private Job job;

    /*
     * Performs a coverage test by enabling coverage reporting and when
     * tests are run a coverage report is created.
     * The coverage report data is then verified.
     */
    @Test
    public void coverage_test() {

        job = jenkins.jobs.create();
        job.configure();
        job.copyDir(resource("/emma/test"));

        // In the maven build step an Emma goal is added to enable coverage reporting.
        MavenBuildStep mbs = job.addBuildStep(MavenBuildStep.class);
        mbs.properties("jacoco.version=0.7.5.201505241946");

        mbs.targets.set("clean emma:emma package");
        EmmaPublisher ep = job.addPublisher(EmmaPublisher.class);
        ep.setReportingThresholds(100, 70, 80, 80, 80, 0, 0, 0, 0, 0);
        job.save();

        Build build = job.startBuild().waitUntilFinished().shouldSucceed();
        EmmaResultsPage resultsPage = new EmmaResultsPage(jenkins.injector, build.getConsoleUrl());
        //                                     class    method  block   line
        List<String> expected = Arrays.asList("100.0", "50.0", "45.5", "50.0", "100.0", "50.0", "45.5", "50.0");
        resultsPage.assertHasResult(expected);
    }
}
