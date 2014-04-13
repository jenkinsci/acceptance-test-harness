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
package org.jenkinsci.test.acceptance.plugins.disk_usage;

import java.net.URL;
import java.util.regex.Pattern;

import org.hamcrest.Description;
import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.po.JenkinsLogger;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.jenkinsci.test.acceptance.po.PluginPageObject;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

import com.google.inject.Injector;

@PluginPageObject("disk-usage")
public class DiskUsage extends PageObject {

    public DiskUsage(Injector i, URL url) {
        super(i, url);
    }

    public void reload() {
        open();
        try {
            clickButton("Record Disk Usage");
        } catch (NoSuchElementException ex) { // v0.22
            clickButton("Record Builds Disk Usage");
            clickButton("Record Jobs Disk Usage");
            clickButton("Record Workspaces Disk Usage");
        }
    }

    public static Matcher<DiskUsage> reloaded() {
        return new Matcher<DiskUsage>("disk usage reloaded") {
            private Exception cause;
            @Override protected boolean matchesSafely(DiskUsage item) {
                JenkinsLogger logger = item.getJenkins().getLogger("all");
                try {
                    logger.waitForLogged(Pattern.compile("Finished Project disk usage. \\d+ ms"));
                    return true;
                } catch (TimeoutException ex) {
                    // v0.22 and newer
                    cause = ex;
                }

                try {
                    logger.waitForLogged(Pattern.compile("Finished Calculation of builds disk usage.*"));
                    logger.waitForLogged(Pattern.compile("Finished Calculation of job directories.*"));
                    logger.waitForLogged(Pattern.compile("Finished Calculation of workspace usage.*"));
                    return true;
                } catch (TimeoutException ex) {
                    cause = ex;
                    return false;
                }
            }

            @Override protected void describeMismatchSafely(DiskUsage item, Description dsc) {
                dsc.appendText(cause.getMessage());
            }
        };
    }
}
