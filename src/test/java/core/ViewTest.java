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
package core;

import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.po.View.*;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ListView;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ViewTest extends AbstractJUnitTest {
    @Test
    @Category(SmokeTest.class)
    public void renameJob() {
        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class, "original_name");
        ListView view = jenkins.views.create(ListView.class, "a_view");
        view.configure();
        view.check(job.name);
        view.save();

        assertThat(view, containsJob(job));

        job = job.renameTo("new_name");

        assertThat(view, containsJob(job));
    }

    @Test
    @Category(SmokeTest.class)
    public void findJobThroughRegexp() {
        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class, "my_name");
        ListView view = jenkins.views.create(ListView.class, "a_view");
        view.configure();
        view.check("Use a regular expression to include jobs into the view");
        fillIn("includeRegex", "my.*");
        view.save();
        assertThat(view, containsJob(job));
    }

    @Test
    public void viewDescription() {
        ListView view = jenkins.views.create(ListView.class, "a_view");
        view.configure();
        view.setDescription("This is a description");
        view.save();
        assertThat(view, hasDescription("This is a description"));
    }
}
