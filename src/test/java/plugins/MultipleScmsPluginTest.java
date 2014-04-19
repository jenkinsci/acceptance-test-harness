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
import org.jenkinsci.test.acceptance.plugins.git.GitScm;
import org.jenkinsci.test.acceptance.plugins.multiple_scms.MutlipleScms;
import org.jenkinsci.test.acceptance.plugins.subversion.SubversionScm;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

@WithPlugins({"subversion", "git", "multiple-scms"})
public class MultipleScmsPluginTest extends AbstractJUnitTest {

    @Test
    public void checkout_several_scms() {
        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        MutlipleScms scms = job.useScm(MutlipleScms.class);
        GitScm git = scms.addScm(GitScm.class);
        git.url("git://github.com/jenkinsci/acceptance-test-harness.git");
        git.localDir("git-project");

        SubversionScm svn = scms.addScm(SubversionScm.class);
        svn.url.set("https://svn.jenkins-ci.org/trunk/jenkins/test-projects/model-ant-project/");
        svn.local.set("svn-project");

        job.addShellStep("test -d svn-project/.svn && test -f git-project/pom.xml");
        job.save();

        job.startBuild().shouldSucceed();
    }
}
