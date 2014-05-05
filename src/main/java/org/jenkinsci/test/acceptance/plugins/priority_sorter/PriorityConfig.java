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
        control("/repeatable-add").click();
        sleep(1000);
        String prefix = last(by.name("view")).getAttribute("path").replace("/view", "");
        return new Group(this, prefix);
    }

    public void save() {
        clickButton("Save");
    }

    public void configure() {
        open();
    }

    public static class Group extends PageAreaImpl {
        public final Control view = control("view");
        public final Control priority = control("priority");

        private Group(PriorityConfig context, String path) {
            super(context, path);
        }

        public void pattern(String pattern) {
            control("useJobFilter").check();
            control("useJobFilter/jobPattern").set(pattern);
        }
    }
}
