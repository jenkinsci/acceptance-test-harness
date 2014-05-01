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

import static org.junit.Assume.assumeNotNull;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Page Object for Gerrit Trigger server (configuration) page.
 * @author Marco Miller
 */
public class GerritTriggerServer extends PageObject {

    public final Jenkins jenkins;
    public final Control hostName = control("/gerritHostName");
    public final Control feUrl = control("/gerritFrontEndUrl");
    public final Control userName = control("/gerritUserName");
    public final Control keyFile = control("/gerritAuthKeyFile");

    public GerritTriggerServer(Jenkins jenkins) {
        super(jenkins.injector,jenkins.url("gerrit-trigger/server/"+GerritTriggerServer.class.getPackage().getName()));
        this.jenkins = jenkins;
    }

    /**
     * Saves harness' gerrit-trigger server configuration.<br>
     * Set these (data) at mvn-test command line to use this test:<br>
     * - gtHostname=gerrit.company.com<br>
     * - gtUsername=companion<br>
     * - gtKeypath=/home/companion/.ssh/id_rsa<br>
     * (We might change this approach to a better one.)
     */
    public void saveTestServerConfig() {
        String hostNameEnv = System.getenv("gtHostname");
        assumeNotNull(hostNameEnv);
        String userNameEnv = System.getenv("gtUsername");
        assumeNotNull(userNameEnv);
        String keyFileEnv = System.getenv("gtKeypath");
        assumeNotNull(keyFileEnv);
        open();
        hostName.set(hostNameEnv);
        feUrl.set("https://"+hostNameEnv);
        userName.set(userNameEnv);
        keyFile.set(keyFileEnv);
        clickButton("Save");
    }
}
