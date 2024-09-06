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
package org.jenkinsci.test.acceptance.plugins.xvnc;

import org.hamcrest.Description;
import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

public class XvncJobConfig extends PageAreaImpl {

    public XvncJobConfig(Job context) {
        super(context, "/hudson-plugins-xvnc-Xvnc");
    }

    public XvncJobConfig useXvnc() {
        control("").check();
        return this;
    }

    public XvncJobConfig takeScreenshot() {
        control("takeScreenshot").check();
        return this;
    }

    public static Matcher<Build> runXvnc() {
        return new BuildMatcher("xvnc run during the build") {
            @Override
            public boolean matchesSafely(Build item) {
                String out = item.getConsole();
                return out.contains("Starting xvnc") && out.contains("Terminating xvnc");
            }
        };
    }

    public static Matcher<Build> tookScreenshot() {
        return new BuildMatcher("screenshot was taken") {
            @Override
            public boolean matchesSafely(Build item) {
                String out = item.getConsole();
                return out.contains("Taking screenshot");
            }
        };
    }

    public static Matcher<Build> usedDisplayNumber(final int number) {
        return new BuildMatcher("display number %d was used", number) {
            @Override
            public boolean matchesSafely(Build item) {
                return item.getConsole().contains(String.format(" :%s ", number));
            }
        };
    }

    private abstract static class BuildMatcher extends Matcher<Build> {
        protected BuildMatcher(String format, Object... args) {
            super(format, args);
        }

        @Override
        public void describeMismatchSafely(Build item, Description d) {
            d.appendText("had output: ").appendText(item.getConsole());
        }
    }
}
