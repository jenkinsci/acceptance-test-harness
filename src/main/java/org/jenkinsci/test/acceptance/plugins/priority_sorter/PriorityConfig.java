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
package org.jenkinsci.test.acceptance.plugins.priority_sorter;

import org.jenkinsci.test.acceptance.po.*;
import org.openqa.selenium.NoSuchElementException;

@ActionPageObject("advanced-build-queue")
public class PriorityConfig extends Action {

    public PriorityConfig(ContainerPageObject parent, String relative) {
        super(parent, relative);
    }

    @Override
    public boolean isApplicable(ContainerPageObject po) {
        return po instanceof Jenkins;
    }

    public Group addGroup() {
        String path = createPageArea("/jobGroup", () -> control("/repeatable-add").click());
        return new Group(this, path);
    }

    public void save() {
        clickButton("Save");
    }

    public void configure() {
        open();
    }

    public static class Group extends PageAreaImpl {
        public final Control priority = control("priority");

        private Group(PriorityConfig context, String path) {
            super(context, path);
        }

        public void pattern(String pattern) {
            control("").select("Jobs included in a view");
            control("jobGroupStrategy/viewName").select("All");
            control("jobGroupStrategy/jobFilter").check();
            control("jobGroupStrategy/jobFilter/jobPattern", "jobGroupStrategy/jobPattern").set(pattern);
        }

        public void byView(String name) {
            control("").select("Jobs included in a view");
            control("jobGroupStrategy/viewName").select(name);
        }
    }
}
