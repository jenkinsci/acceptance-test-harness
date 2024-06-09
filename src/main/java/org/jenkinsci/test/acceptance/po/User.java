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
package org.jenkinsci.test.acceptance.po;

import com.fasterxml.jackson.databind.JsonNode;
import hudson.util.VersionNumber;
import java.net.URL;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

public class User extends ContainerPageObject {

    private String id;
    private String fullName;
    private String mail;

    /*package*/ static User getCurrent(Jenkins context) {
        try {
            return new User(context);
        } catch (Exception ex) {
            // /me not accessible => not logged in
            return null;
        }
    }

    private User(Jenkins context) {
        super(context, context.url("me/"));
    }

    public User(Jenkins context, String name) {
        super(context, context.url("user/%s/", name));
    }

    private void load() {
        if (id != null) return;
        try {
            JsonNode json = getJson();
            id = json.get("id").asText();
            fullName = json.get("fullName").asText();
            JsonNode property = json.get("property");
            if (property != null) {
                if (property.isArray()) {
                    for (JsonNode propertyNodes : property) {
                        if (propertyNodes.get("address") != null && propertyNodes.get("address").asText() != "null") {
                            mail = propertyNodes.get("address").asText();
                        }
                    }
                }
            }
        } catch (NoSuchElementException nse) {
            // /me not accessible => not logged in
        }
    }

    public String id() {
        load();
        return id;
    }

    public String fullName() {
        load();
        return fullName;
    }

    public String mail() {
        load();
        return mail;
    }

    public User fullName(String fullName) {
        control("/fullName").set(fullName);
        return this;
    }

    @Override
    public URL getConfigUrl() {
        return getJenkins().getVersion().isNewerThan(new VersionNumber("2.461")) ? url("account/") : url("configure");
    }

    public void delete() {
        try {
            runThenHandleDialog(() -> clickLink("Delete"));
        } catch (TimeoutException te) {
            visit("delete");
            clickButton("Yes");
        }
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", id(), fullName());
    }

    @Override
    public boolean equals(Object rhs) {
        if (this == rhs) return true;
        if (rhs == null) return false;

        if (!getClass().equals(rhs.getClass())) return false;

        User other = (User) rhs;
        return id().equals(other.id());
    }

    @Override
    public int hashCode() {
        return id().hashCode();
    }
}
