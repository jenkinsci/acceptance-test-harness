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
package org.jenkinsci.test.acceptance.plugins.artifactory;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

public class ArtifactoryGlobalConfig extends PageAreaImpl {

    public ArtifactoryGlobalConfig(JenkinsConfig context) {
        super(context, "/org-jfrog-hudson-ArtifactoryBuilder");
    }

    public Server addServer() {
        control("repeatable-add").click();
        return new Server(this, "jfrogInstances");
    }

    public static class Server extends PageAreaImpl {
        public final Control id = control("instanceId");
        public final Control url = control("platformUrl");
        public final Control username = control("deployerCredentialsConfig/username");
        public final Control password = control("deployerCredentialsConfig/password");
        public final Control testConnectionButton = control("validate-button");
        public final Control goodConnectionFeedback = control("div.ok");
        public final Control errorConnectionFeedback = control("div.error");

        protected Server(ArtifactoryGlobalConfig area, String path) {
            super(area, path);
        }
    }
}
