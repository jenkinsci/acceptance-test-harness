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
package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Bug;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.BuildOtherProjects;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ListView;
import org.junit.Test;

@WithPlugins("upstream-downstream-view")
public class UpstreamDownstreamColumnPluginTest extends AbstractJUnitTest {

    @Test @Bug("JENKINS-25943")
    public void links_should_point_to_correct_location() {
        jenkins.jobs.create(FreeStyleJob.class, "in_no_view");
        FreeStyleJob inObservedView = jenkins.jobs.create(FreeStyleJob.class, "in_observed_view");
        FreeStyleJob inDifferentView = jenkins.jobs.create(FreeStyleJob.class, "in_different_view");
        FreeStyleJob sut = jenkins.jobs.create(FreeStyleJob.class, "sut");

        sut.configure();
        sut.addPublisher(BuildOtherProjects.class).childProjects.set("in_no_view, in_observed_view, in_different_view");
        sut.save();

        ListView observed = jenkins.views.create(ListView.class, "observed");
        ListView different = jenkins.views.create(ListView.class, "diffferent");

        observed.configure();
        observed.addJob(inObservedView);
        observed.addJob(sut);
        observed.save();

        different.configure();
        different.addJob(inDifferentView);
        different.save();

        observed.open();
        last(by.link("in_no_view")).click();
        assertThat(driver, hasContent("Project in_no_view"));

        observed.open();
        last(by.link("in_observed_view")).click();
        assertThat(driver, hasContent("Project in_observed_view"));

        observed.open();
        last(by.link("in_different_view")).click();
        assertThat(driver, hasContent("Project in_different_view"));
    }
}
