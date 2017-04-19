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
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.NoSuchElementException;

import java.util.concurrent.Callable;

/**
 * Openstack Cloud.
 *
 * @author ogondza
 */
@Describable({"Cloud (OpenStack)", "Cloud (Openstack)"})
public class OpenstackCloud extends Cloud {

    private boolean advancedOpen = false;

    public OpenstackCloud(PageObject context, String path) {
        super(context, path);
    }

    public OpenstackCloud profile(String value) {
        control("name", "profile").set(value);
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

    public OpenstackCloud associateFloatingIp(final String pool) {
        ensureAdvancedOpen();
        try {
            // Prior 2.1
            control("floatingIps").check();
        } catch (NoSuchElementException ex) {
            waitFor().withMessage("Floating IP pool select populates").ignoring(NoSuchElementException.class).until(new Callable<Boolean>() {
                @Override public Boolean call() throws Exception {
                    control("slaveOptions/floatingIpPool", "floatingIpPool").select(pool);
                    return true;
                }
            });
        }
        return this;
    }

    private void ensureAdvancedOpen() {
        if (advancedOpen == false) {
            control("advanced-button").click();
            advancedOpen = true;
        }
    }

    public OpenstackCloud instanceCap(int instanceCap) {
        ensureAdvancedOpen();
        control("slaveOptions/instanceCap").set(instanceCap);
        return this;
    }

    public OpenstackCloud testConnection() {
        Control button = control(by.xpath("//div[@descriptorid='jenkins.plugins.openstack.compute.JCloudsCloud']"
                + "//button[contains(.,'Test Connection')]"));
        button.click();
        return this;
    }

    public OpenstackSlaveTemplate addSlaveTemplate() {
        String newPath = createPageArea("templates", new Runnable() {
            @Override public void run() {
                control("repeatable-add").click();
            }
        });

        return new OpenstackSlaveTemplate(this, newPath);
    }
}
