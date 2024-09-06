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
package org.jenkinsci.test.acceptance.plugins.job_config_history;

import java.util.ArrayList;
import java.util.List;
import org.jenkinsci.test.acceptance.po.Action;
import org.jenkinsci.test.acceptance.po.ActionPageObject;
import org.jenkinsci.test.acceptance.po.ContainerPageObject;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.WebElement;

@ActionPageObject("jobConfigHistory")
public class JobConfigHistory extends Action {

    public JobConfigHistory(Job parent, String relative) {
        super(parent, relative);
    }

    public void showLastChange() {
        open();
        clickButton("Show Diffs");
    }

    public List<Change> getChanges() {
        open();

        final List<WebElement> rows = all(by.xpath("//table//a[text()='(RAW)']/../../*[1]"));
        final ArrayList<Change> changes = new ArrayList<Change>(rows.size());

        for (WebElement row : rows) {
            changes.add(new Change(this, row.getText()));
        }

        return changes;
    }

    @Override
    public boolean isApplicable(ContainerPageObject po) {
        return po instanceof Job;
    }

    public static class Change extends PageObject {

        private final String timestamp;

        public Change(JobConfigHistory owner, String timestamp) {
            super(owner, owner.url("configOutput?type=xml&timestamp=" + timestamp));

            this.timestamp = timestamp;
        }
    }
}
