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

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.html_publisher.HtmlPublisher;
import org.jenkinsci.test.acceptance.plugins.html_publisher.HtmlPublisher.Report;
import org.jenkinsci.test.acceptance.plugins.html_publisher.HtmlReport;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

@WithPlugins("htmlpublisher")
public class HtmlPublisherPluginTest extends AbstractJUnitTest {

    @Test
    public void publish_whole_directory() {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.addShellStep("echo 'root' > root.html && mkdir dir && echo 'indir' > dir/indir.html");
        Report report = job.addPublisher(HtmlPublisher.class).addDir(".");
        report.index.set("root.html");
        report.name.set("MyReport");
        job.save();

        job.startBuild().shouldSucceed();
        job.action(HtmlReport.class, "MyReport", "MyReport")
                .<HtmlReport>openViaLink()
                .fileShouldMatch("root.html", "root")
                .fileShouldMatch("dir/indir.html", "indir");
    }
}
