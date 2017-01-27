/*
 * The MIT License
 *
 * Copyright 2015 CloudBees, Inc.
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
package org.jenkinsci.test.acceptance.po;

import java.net.URL;

import com.google.inject.Injector;

/**
 * A container that stores nested items like {@link Job jobs}. A folder may contain a hierarchy of folders.
 */
@Describable("com.cloudbees.hudson.plugins.folder.Folder")
public class Folder extends TopLevelItem implements Container {

    private final JobsMixIn jobs;
    private final ViewsMixIn views;

    public Folder(final Injector injector, final URL url, final String name) {
        super(injector, url, name);

        jobs = new JobsMixIn(this);
        views = new ViewsMixIn(this);
    }

    public Folder(PageObject context, URL url, String name) {
        super(context, url, name);
        jobs = new JobsMixIn(this);
        views = new ViewsMixIn(this);
    }

    @Override
    public JobsMixIn getJobs() {
        return jobs;
    }

    @Override
    public ViewsMixIn getViews() {
        return views;
    }
}
