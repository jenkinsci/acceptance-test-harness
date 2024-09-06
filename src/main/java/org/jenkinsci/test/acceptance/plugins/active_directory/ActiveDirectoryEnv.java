/*
 * The MIT License
 *
 * Copyright (c) 2014 Ericsson
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
package org.jenkinsci.test.acceptance.plugins.active_directory;

import static org.junit.Assume.assumeNotNull;

/**
 * Thread-unsafe singleton serving ad-prefixed test env args.
 * @author Marco.Miller@ericsson.com
 */
public class ActiveDirectoryEnv {

    private static ActiveDirectoryEnv instance = null;

    private String bindDN;
    private String controller;
    private String domain;
    private String group;
    private String password;
    private String site;
    private String user;
    private String groupLookupStrategy;

    private ActiveDirectoryEnv() {
        bindDN = System.getenv("adBindDN");
        controller = System.getenv("adController");
        domain = System.getenv("adDomain");
        group = System.getenv("adGroup");
        password = System.getenv("adPassword");
        site = System.getenv("adSite");
        user = System.getenv("adUser");
        groupLookupStrategy = System.getenv("adGroupLookupStrategy");

        assumeNotNull(domain);
        assumeNotNull(group);
        assumeNotNull(password);
        assumeNotNull(user);
    }

    public static ActiveDirectoryEnv get() {
        if (instance == null) {
            instance = new ActiveDirectoryEnv();
        }
        return instance;
    }

    public String getBindDN() {
        return bindDN;
    }

    public String getController() {
        return controller;
    }

    public String getDomain() {
        return domain;
    }

    public String getGroup() {
        return group;
    }

    public String getPassword() {
        return password;
    }

    public String getSite() {
        return site;
    }

    public String getUser() {
        return user;
    }

    public String getGroupLookupStrategy() {
        return groupLookupStrategy;
    }
}
