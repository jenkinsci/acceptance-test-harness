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
import org.openqa.selenium.NoSuchElementException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

/**
 * Page Object for Gerrit Trigger server (configuration) page.
 * @author Marco.Miller@ericsson.com
 */
public class GerritTriggerServer extends PageObject {

    public final Jenkins jenkins;
    public final Control hostName = control("/gerritHostName");
    public final Control feUrl = control("/gerritFrontEndUrl");
    public final Control userName = control("/gerritUserName");
    public final Control keyFile = control("/gerritAuthKeyFile");
    public final Control advanced = control("/advanced-button");
    public final Control add = control("/repeatable-add");
    public final Control codeReview = control("/verdictCategories[2]/verdictValue");
    public final Control codeReviewD = control("/verdictCategories[2]/verdictDescription");
    public final Control verified = control("/verdictCategories[3]/verdictValue");
    public final Control verifiedD = control("/verdictCategories[3]/verdictDescription");
    public final Control start = control("/button");

    private static final String serverUrl = "gerrit-trigger/server/" + GerritTriggerServer.class.getPackage().getName();

    public GerritTriggerServer(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url(serverUrl));
        this.jenkins = jenkins;
    }

    /**
     * Saves harness' gerrit-trigger server configuration.
     */
    public void saveTestServerConfig() {
        open();
        hostName.set(GerritTriggerEnv.get().getHostName());
        feUrl.set("https://"+GerritTriggerEnv.get().getHostName());
        userName.set(GerritTriggerEnv.get().getGerritUser());
        keyFile.set(GerritTriggerEnv.get().getUserHome()+"/.ssh/id_rsa");
        advanced.click();
        try {
            codeReview.resolve();
        } catch (NoSuchElementException e) {
            add.click();
            codeReview.set("Code-Review");
            codeReviewD.set("New Code Review");
            add.click();
            verified.set("Verified");
            verifiedD.set("Verified");
        }
        clickButton("Save");
        try {
            start.click();
        } catch (NoSuchElementException e) {
            try {
                HttpURLConnection c = (HttpURLConnection) new URL(url + "/wakeup").openConnection();
                c.setRequestMethod("GET");
                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);
                assertThat(c.getResponseCode(), is(equalTo(200)));
            } catch (IOException ioe) {
                fail(ioe.getMessage());
            }
        }
    }
}
