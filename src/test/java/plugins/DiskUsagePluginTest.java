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
import org.jenkinsci.test.acceptance.plugins.disk_usage.DiskUsage;
import org.jenkinsci.test.acceptance.plugins.disk_usage.DiskUsageGlobalConfig;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Job;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;
import static org.jenkinsci.test.acceptance.Matchers.hasElement;

@WithPlugins("disk-usage")
public class DiskUsagePluginTest extends AbstractJUnitTest {

    private DiskUsage du;

    @Before
    public void setUp() {
        du = jenkins.getPluginPage(DiskUsage.class);
    }

    @Test
    public void update_disk_usage() {
        du.reload();
    }

    @Test
    public void show_graph_on_project_page() {
        FreeStyleJob job = jenkins.jobs.create();
        job.startBuild().waitUntilFinished();

        showGraphOnProjectPage();
        du.reload();

        job.open();
        assertThat(driver, hasElement(by.xpath(JOB_GRAPH)));
    }

    @Test
    public void reflect_disk_changes_in_disk_usage_report() {
        showGraphOnProjectPage();

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.addShellStep("echo 'asdf' > file");
        job.save();
        job.startBuild().waitUntilFinished();

        assertReports(job, "All workspaces [1-9]\\d*");

        job.getWorkspace().wipeOut();
        du.reload();

        assertReports(job, "All workspaces [0\\-]");
    }

    private void showGraphOnProjectPage() {
        jenkins.configure();
        DiskUsageGlobalConfig gc = new DiskUsageGlobalConfig(jenkins.getConfigPage());
        gc.showGraphOnProjectPage.check();
        jenkins.save();
    }

    private void assertReports(Job job, String workspace) {
        job.open();
        assertThat(driver, hasContent(Pattern.compile(workspace)));
    }

    private static final String JOB_GRAPH = "//img[@src='disk-usage/graph/png' or starts-with(@src, 'diskUsage/graph/png')]";
}
