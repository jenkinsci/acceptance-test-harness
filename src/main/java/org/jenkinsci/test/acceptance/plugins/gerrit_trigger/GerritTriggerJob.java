/*
 * The MIT License
 *
 * Copyright (c) 2014 Ericsson
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
package org.jenkinsci.test.acceptance.plugins.gerrit_trigger;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageObject;

import static org.junit.Assume.assumeNotNull;

/**
 * Page Object for Gerrit Trigger test-job (configuration) page.
 * @author Marco Miller
 */
public class GerritTriggerJob extends PageObject {

    public final Jenkins jenkins;
    public final Control event = control("/com-sonyericsson-hudson-plugins-gerrit-trigger-hudsontrigger-GerritTrigger");
    public final Control server = control("/com-sonyericsson-hudson-plugins-gerrit-trigger-hudsontrigger-GerritTrigger/serverName");
    public final Control project = control("/com-sonyericsson-hudson-plugins-gerrit-trigger-hudsontrigger-GerritTrigger/gerritProjects/pattern");
    public final Control branch = control("/com-sonyericsson-hudson-plugins-gerrit-trigger-hudsontrigger-GerritTrigger/gerritProjects/branches/pattern");

    public GerritTriggerJob(Jenkins jenkins,String jobName) {
        super(jenkins.injector,jenkins.url("job/"+jobName+"/configure"));
        this.jenkins = jenkins;
    }

    /**
     * Saves harness' gerrit-trigger test-job configuration.
     */
    public void saveTestJobConfig() {
        String projectEnv = System.getenv("gtProject");
        assumeNotNull(projectEnv);
        open();
        if(!event.resolve().isSelected()) event.click();
        server.select(this.getClass().getPackage().getName());
        project.set(projectEnv);
        branch.set("master");
        clickButton("Save");
    }
}
