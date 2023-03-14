/*
 * The MIT License
 *
 * Copyright (c) 2023 CloudBees, Inc.
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
package org.jenkinsci.test.acceptance.plugins.windowsslaves;

import org.jenkinsci.test.acceptance.po.ComputerLauncher;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageObject;

@Describable({"hudson.os.windows.ManagedWindowsServiceLauncher"})
public class WindowsSlaveLauncher extends ComputerLauncher {

    private final Control user = this.control("userName");
    private final Control password = this.control("password");
    private final Control host = this.control("host");
    private final Control runAs = this.control("/");

    public WindowsSlaveLauncher(PageObject context, String path) {
        super(context, path);
    }

    public WindowsSlaveLauncher user(String usernameToConnect) {
        user.set(usernameToConnect);
        return this;
    }

    public WindowsSlaveLauncher password(String passwordToConnect) {
        password.set(passwordToConnect);
        return this;
    }

    public WindowsSlaveLauncher host(String hostToConnect) {
        host.set(hostToConnect);
        return this;
    }

    public WindowsSlaveLauncher runAs(String runAs) {
        this.runAs.select(runAs);
        return this;
    }
}

