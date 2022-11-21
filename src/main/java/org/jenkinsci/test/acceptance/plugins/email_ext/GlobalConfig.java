/*
 * The MIT License
 *
 * Copyright (c) 2015 Red Hat, Inc.
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
package org.jenkinsci.test.acceptance.plugins.email_ext;

import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.openqa.selenium.NoSuchElementException;

public class GlobalConfig extends PageAreaImpl {

    public GlobalConfig(JenkinsConfig context) {
        super(context, "/hudson-plugins-emailext-ExtendedEmailPublisher");
    }

    public void replyTo(String reply) {
        control("ext_mailer_default_replyto").set(reply);
    }

    public void smtpServer(String smtp) {
        control("ext_mailer_smtp_server").set(smtp);
    }

    public void smtpPort(int port) {
        ensureAdvanced();
        control("ext_mailer_smtp_port").set(port);
    }

    public void auth(String name, String passwd) {
        ensureAdvanced();
        control("ext_mailer_use_smtp_auth").check();
        control("ext_mailer_use_smtp_auth/ext_mailer_smtp_username").set(name);
        control("ext_mailer_use_smtp_auth/ext_mailer_smtp_password").set(passwd);
    }

    private void ensureAdvanced() {
        try {
            control("advanced-button").click();
        } catch(NoSuchElementException ignored) {
        }
    }
}
