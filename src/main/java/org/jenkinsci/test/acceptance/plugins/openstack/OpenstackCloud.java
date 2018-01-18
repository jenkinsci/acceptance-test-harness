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

import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshCredentialDialog;
import org.jenkinsci.test.acceptance.po.Cloud;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.FormValidation;
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

    public OpenstackCloud credential(String user, String userDomain, String project, String projectDomain, String password) {
        try {
            boolean keystoneV3 = userDomain != null || projectDomain != null;
            String identity = keystoneV3
                    ? user + ":" + project + ":" + projectDomain
                    : user + ":" + project
            ;
            control("identity").set(identity);
            control("credential").set(password);
        } catch (NoSuchElementException ex) {
            // 2.30+
            Control creds = control("credentialId");
            creds.resolve().findElement(by.xpath("./following-sibling::span/*/button[@class='credentials-add-menu']")).click();
            creds.resolve().findElement(by.xpath("./following-sibling::div[contains(@class,'credentials-add-menu-items')]//*[normalize-space(text())='Jenkins']")).click();
            SshCredentialDialog d = new SshCredentialDialog(getPage(), "/credentials");
            boolean keystoneV3 = userDomain != null || projectDomain != null;
            if (keystoneV3) {
                getPage().control("/").select("OpenStack auth v3");
                d.control("projectDomain").set(projectDomain);
                d.control("projectName").set(project);
                d.control("userDomain").set(userDomain);
                d.control("username").set(user);
            } else {
                getPage().control("/").select("OpenStack auth v2");
                d.control("tenant").set(project);
                d.control("username").set(user);
            }
            d.control("password").set(password);
            String knownId = PageObject.createRandomName();
            d.control("id").set(knownId);
            d.control(by.id("credentials-add-submit-button")).click();
            creds.select(knownId);
        }
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

    public FormValidation testConnection() {
        Control button = control("validate-button");
        button.click();
        return button.getFormValidation();
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
