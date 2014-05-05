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
package org.jenkinsci.test.acceptance.plugins.violations;

import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.*;

@Describable("Report Violations")
public class Violations extends PostBuildStepImpl {

    // This is an abuse as it is not a publisher in maven project.
    public Violations(MavenModuleSet parent) {
        super(parent, "/hudson-plugins-violations-hudson-maven-ViolationsMavenReporter");
        control("").click();
    }

    public Violations(Job parent, String path) {
        super(parent, path);
    }

    public Config config(String name) {
        return new Config(parent, name);
    }

    public class Config extends PageAreaImpl {
        private final String name;

        // Config controls does not reside of shared path prefix!
        private Config(Job context, String name) {
            super(context, Violations.this.path);
            this.name = name;
        }

        public Config sunnyLimit(int limit) {
            column("min").set(limit);
            return this;
        }

        public Config cloudyLimit(int limit) {
            column("max").set(limit);
            return this;
        }

        public Config unstableLimit(int limit) {
            column("unstable").set(limit);
            return this;
        }

        public Config pattern(String pattern) {
            column("pattern").set(pattern);
            return this;
        }

        private Control column(String column) {
            return control(by.name("%s.%s", name, column));
        }
    }
}
