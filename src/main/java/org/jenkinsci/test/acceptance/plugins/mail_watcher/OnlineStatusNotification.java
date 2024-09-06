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
package org.jenkinsci.test.acceptance.plugins.mail_watcher;

import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.Slave;

public class OnlineStatusNotification extends PageAreaImpl {

    public OnlineStatusNotification(Jenkins context) {
        super(
                context,
                "/jenkins-model-GlobalNodePropertiesConfiguration/globalNodeProperties/org-jenkinsci-plugins-mailwatcher-WatcherNodeProperty");
        control("").click();
    }

    public OnlineStatusNotification(Slave context) {
        super(context, "/nodeProperties/org-jenkinsci-plugins-mailwatcher-WatcherNodeProperty");
        control(by.checkbox("Notify when Node online status changes")).click();
    }

    public void onOnline(String addresses) {
        control("onlineAddresses").set(addresses);
    }

    public void onOffline(String addresses) {
        control("offlineAddresses").set(addresses);
    }
}
