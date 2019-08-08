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

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;

/**
 * Single slave template of JClouds cloud.
 *
 * @author ogondza
 */
public class OpenstackSlaveTemplate extends PageAreaImpl {

    protected OpenstackSlaveTemplate(OpenstackCloud area, String relativePath) {
        super(area, relativePath);
    }

    public OpenstackSlaveTemplate name(String name) {
        control("name").set(name);
        return this;
    }

    public OpenstackSlaveTemplate labels(String labels) {
        control("labels", "labelString").set(labels);
        return this;
    }

    public OpenstackSlaveTemplate hardwareId(final String value) {
        ensureAdvancedOpened();
        waitFor().withMessage("Hardware ID select populates").ignoring(NoSuchElementException.class).until(() -> {
            control("slaveOptions/hardwareId", "hardwareId").select(value);
            return true;
        });
        return this;
    }

    public OpenstackSlaveTemplate imageId(final String value) {
        ensureAdvancedOpened();
        String[] pathComponents;
        try {
            control("slaveOptions").select("Image");
            pathComponents = new String[] { "slaveOptions/bootSource/name" };
        }  catch (NoSuchElementException ex) {
            pathComponents = new String[] { "slaveOptions/imageId", "imageId" };
        }
        final String[] pc = pathComponents;
        waitFor().withMessage("Image ID select populates").ignoring(NoSuchElementException.class).until(() -> {
            control(pc).select(value);
            return true;
        });
        return this;
    }

    public OpenstackSlaveTemplate sshCredentials(String value) {
        ensureAdvancedOpened();
        control("slaveOptions/launcherFactory/credentialsId", "slaveOptions/credentialsId", "credentialsId").select(value);
        return this;
    }

    public OpenstackSlaveTemplate connectionType(String type) {
        ensureAdvancedOpened();
        control("slaveOptions/[1]", "slaveOptions/slaveType", "slaveType").select(type);
        return this;
    }

    public OpenstackSlaveTemplate userData(String name) {
        ensureAdvancedOpened();
        control("slaveOptions/userDataId", "userDataId").select(name);
        return this;
    }

    public OpenstackSlaveTemplate networkId(String id) {
        ensureAdvancedOpened();
        Control control = control("slaveOptions/networkId", "networkId");
        WebElement we = control.resolve();
        if (we.getTagName().equals("input") && we.getAttribute("type").equals("text")) {
            control.set(id);
        } else {
            waitFor().withMessage("Network ID select populates").ignoring(NoSuchElementException.class).until(() -> {
                control.select(id);
                return true;
            });
        }
        return this;
    }

    public OpenstackSlaveTemplate keyPair(String name) {
        ensureAdvancedOpened();
        Control control = control("slaveOptions/keyPairName", "keyPairName");
        try {
            control.select(name);
        } catch (WebDriverException ex) {
            control.set(name);
        }
        return this;
    }

    public OpenstackSlaveTemplate fsRoot(String path) {
        ensureAdvancedOpened();
        control("slaveOptions/fsRoot", "fsRoot").set(path);
        return this;
    }

    private boolean advanced = false;
    private void ensureAdvancedOpened() {
        if (advanced) return;
        control("advanced-button").click();
        advanced = true;
    }
}
