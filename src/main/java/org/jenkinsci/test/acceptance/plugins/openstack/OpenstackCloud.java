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
package org.jenkinsci.test.acceptance.plugins.openstack;

import org.jenkinsci.test.acceptance.po.Cloud;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageObject;

/**
 * Openstack Cloud.
 *
 * @author ogondza
 */
@Describable("Cloud (Openstack)")
public class OpenstackCloud extends Cloud {

    public OpenstackCloud(PageObject context, String path) {
        super(context, path);
    }

    public OpenstackCloud profile(String value) {
        control("profile").set(value);
        return this;
    }

    public OpenstackCloud endpoint(String value) {
        control("endPointUrl").set(value);
        return this;
    }

    public OpenstackCloud identity(String value) {
        control("identity").set(value);
        return this;
    }

    public OpenstackCloud credential(String value) {
        control("credential").set(value);
        return this;
    }

    public OpenstackCloud associateFloatingIp() {
        control("advanced-button").click();
        control("floatingIps").check();
        return this;
    }

    public OpenstackCloud testConnection() {
        clickButton("Test Connection");
        return this;
    }

    public OpenstackSlaveTemplate addSlaveTemplate() {
        control("repeatable-add").click();
        sleep(500); // Wait for template to appear
        final String newPath = last(by.name("templates")).getAttribute("path");

        assert newPath.startsWith(getPath());
        return new OpenstackSlaveTemplate(this, newPath);
    }
}
