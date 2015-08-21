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

import java.util.concurrent.Callable;

import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.openqa.selenium.NoSuchElementException;

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
        control("labelString").set(labels);
        return this;
    }

    public OpenstackSlaveTemplate hardwareId(final String value) {
       // Wait until the select populates
        waitFor().ignoring(NoSuchElementException.class).until(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                control("hardwareId").select(value);
                return true;
            }
        });
        return this;
    }

    public OpenstackSlaveTemplate imageId(final String value) {
        // Wait until the select populates
        waitFor().ignoring(NoSuchElementException.class).until(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                control("imageId").select(value);
                return true;
            }
        });
        return this;
    }

    public OpenstackSlaveTemplate credentials(String value) {
        control("credentialsId").select(value);
        return this;
    }

    public OpenstackSlaveTemplate slaveType(String type) {
        ensureAdvancedOpened();
        control("slaveType").select(type);
        return this;
    }

    public OpenstackSlaveTemplate userData(String name) {
        ensureAdvancedOpened();
        control("userDataId").select(name);
        return this;
    }

    public OpenstackSlaveTemplate keyPair(String name) {
        ensureAdvancedOpened();
        control("keyPairName").set(name);
        return this;
    }

    private boolean advanced = false;
    private void ensureAdvancedOpened() {
        if (advanced) return;
        control("advanced-button").click();
        advanced = true;
    }
}
