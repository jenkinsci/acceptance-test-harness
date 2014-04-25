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

import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindbugsAction;
import org.jenkinsci.test.acceptance.plugins.findbugs.FindbugsPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.jenkinsci.test.acceptance.Matchers.hasAction;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

@WithPlugins("findbugs")
public class FindbugsPluginTest extends AbstractCodeStylePluginHelper {

    /**
     * Runs test and check if warnings of Findbugs are displayed.
     */
    @Test
    public void record_analysis() {
        FreeStyleJob job = setupJob("/findbugs_plugin/findbugsXml.xml", FindbugsPublisher.class, "findbugsXml.xml");

        Build lastBuild = buildJobWithSuccess(job);

        assertThat(lastBuild, hasAction("FindBugs Warnings"));
        assertThat(job, hasAction("FindBugs Warnings"));
        assertThat(lastBuild.open(), hasContent("FindBugs: 6 warnings from one analysis."));

        FindbugsAction fa = new FindbugsAction(job);
        assertThat(fa.getWarningNumber(), is(6));
        assertThat(fa.getNewWarningNumber(), is(6));
        assertThat(fa.getFixedWarningNumber(), is(0));
        assertThat(fa.getHighWarningNumber(), is(2));
        assertThat(fa.getNormalWarningNumber(), is(4));
        assertThat(fa.getLowWarningNumber(), is(0));
    }

    /**
     * Runs job two times to check if new and fixed warnings are displayed.
     */
    @Test
    public void record_analysis_two_runs() {
        final FreeStyleJob job = setupJob("/findbugs_plugin/findbugsXml.xml", FindbugsPublisher.class, "findbugsXml.xml");
        editJobAndDelLastResource(job, "/findbugs_plugin/findbugsXml-2.xml", "findbugsXml.xml");

        Build lastBuild = buildJobWithSuccess(job);

        assertThat(lastBuild, hasAction("FindBugs Warnings"));
        assertThat(job, hasAction("FindBugs Warnings"));
        assertThat(lastBuild.open(), hasContent("FindBugs: 5 warnings from one analysis."));

        FindbugsAction fa = new FindbugsAction(job);
        assertThat(fa.getWarningNumber(), is(5));
        assertThat(fa.getNewWarningNumber(), is(1));
        assertThat(fa.getFixedWarningNumber(), is(2));
        assertThat(fa.getHighWarningNumber(), is(3));
        assertThat(fa.getNormalWarningNumber(), is(2));
        assertThat(fa.getLowWarningNumber(), is(0));
    }
}
