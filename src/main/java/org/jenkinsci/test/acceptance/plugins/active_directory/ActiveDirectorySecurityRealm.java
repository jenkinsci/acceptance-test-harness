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


import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.SecurityRealm;

/**
 * Realm for AD security (global) configuration page.
 * @author Marco.Miller@ericsson.com
 */
@Describable("Active Directory")
public class ActiveDirectorySecurityRealm extends SecurityRealm {

    public ActiveDirectorySecurityRealm(GlobalSecurityConfig context,String path) {
        super(context,path);
    }

    /**
     * Configures AD-based global security.
     */
    public void configure() {
        control("domain").set(ActiveDirectoryEnv.get().getDomain());
        control("advanced-button").click();

        if(ActiveDirectoryEnv.get().getController() != null) {
            control("server").set(ActiveDirectoryEnv.get().getController());
        }
        if(ActiveDirectoryEnv.get().getSite() != null) {
            control("site").set(ActiveDirectoryEnv.get().getSite());
        }
        control("bindPassword").set(ActiveDirectoryEnv.get().getPassword());

        String bindDN = ActiveDirectoryEnv.get().getBindDN();
        if(bindDN == null) {
            bindDN = ActiveDirectoryEnv.get().getUser()+"@"+ActiveDirectoryEnv.get().getDomain();
        }
        control("bindName").set(bindDN);

        String groupLookupStrategy = ActiveDirectoryEnv.get().getGroupLookupStrategy();
        if (groupLookupStrategy != null) {
            control("groupLookupStrategy").select(groupLookupStrategy);
        }
    }

    public void validateConfig() {
        control("validate-button").click();
        waitFor(driver, hasContent("Success"), 10);
    }
}
