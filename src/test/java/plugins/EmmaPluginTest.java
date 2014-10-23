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
import org.jenkinsci.test.acceptance.plugins.maven.MavenBuildStep;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Job;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks the successfully execution of Emma coverage reports.
 * Reuses and is inspired by Jacoco test case.
 *
 * @author Orjan Percy
 */
@WithPlugins("emma")
public class EmmaPluginTest extends AbstractJUnitTest {

    private Job job;

    /*
     * Performs a coverage test by enabling coverage reporting and when tests are run a coverage report is created.
     * The coverage report data is extracted and verified.
     */
    @Test
    public void coverage_test() {

        job = jenkins.jobs.create();
        job.configure();
        // Reuse Jacoco test files.
        job.copyDir(resource("/jacoco/test"));

        // In the maven build step an Emma goal is added to enable coverage reporting.
        MavenBuildStep mbs = job.addBuildStep(MavenBuildStep.class);
        mbs.targets.set("clean emma:emma package");
        EmmaPublisher ep = job.addPublisher(EmmaPublisher.class);
        ep.maxClass.sendKeys("100");
        ep.maxMethod.sendKeys("70");
        ep.maxBlock.sendKeys("80");
        ep.maxLine.sendKeys("80");
        ep.maxCondition.sendKeys("80");
        ep.minClass.sendKeys("0");
        ep.minMethod.sendKeys("0");
        ep.minBlock.sendKeys("0");
        ep.minLine.sendKeys("0");
        ep.minCondition.sendKeys("0");
        job.save();

        Build build = job.startBuild().waitUntilFinished().shouldSucceed();
        assert(build.getNavigationLinks().containsValue("Coverage Report"));
        find(by.link("Coverage Report")).click();

        // Extract the result data and verify.
        String content = find(by.css("div#main-panel-content")).getText();
        Pattern p = Pattern.compile("\\d+(?:[,.]\\d+)");
        Matcher m = p.matcher(content);
        List<String> l = new ArrayList<String>();
        while (m.find()) {
            l.add(m.group());
        }
        assert(l.get(0).compareTo("100.0") == 0); // class
        assert(l.get(1).compareTo("50.0") == 0);  // method
        assert(l.get(2).compareTo("45.5") == 0); // block
        assert(l.get(3).compareTo("50.0") == 0); // line
        assert(l.get(4).compareTo("100.0") == 0);
        assert(l.get(5).compareTo("50.0") == 0);
        assert(l.get(6).compareTo("45.5") == 0);
        assert(l.get(7).compareTo("50.0") == 0);
    }
}
