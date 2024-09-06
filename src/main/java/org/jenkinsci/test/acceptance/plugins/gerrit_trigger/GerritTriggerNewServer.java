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

/**
 * Page Object for Gerrit Trigger newServer (configuration) page.
 * @author Marco.Miller@ericsson.com
 */
public class GerritTriggerNewServer extends PageObject {

    public final Jenkins jenkins;
    public final Control name = control("/name");
    public final Control modeDefault = control("/mode[com.sonyericsson.hudson.plugins.gerrit.trigger.GerritServer]");

    public GerritTriggerNewServer(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url("gerrit-trigger/newServer"));
        this.jenkins = jenkins;
    }

    /**
     * Saves harness' gerrit-trigger server if none already configured.<br>
     * Does not matter if already configured; no-op with harmless error then.
     */
    public void saveNewTestServerConfigIfNone(String serverName) {
        avoidOccasional404FromNonReadyGerritUI();
        open();
        name.set(serverName);
        modeDefault.click();
        clickButton("OK");
    }

    private void avoidOccasional404FromNonReadyGerritUI() {
        elasticSleep(1000);
    }
}
