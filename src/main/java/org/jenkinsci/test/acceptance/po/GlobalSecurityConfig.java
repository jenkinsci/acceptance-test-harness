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

import java.net.URL;

import org.jenkinsci.test.acceptance.plugins.authorize_project.BuildAccessControl;
import org.jenkinsci.test.acceptance.plugins.workflow_multibranch.BranchSource;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

/**
 * Global security configuration UI.
 */
public class GlobalSecurityConfig extends ContainerPageObject {

    private static final String SAFE_HTML = "Safe HTML";

    public final Control csrf = control(by.name("_.csrf"));

    public GlobalSecurityConfig(Jenkins context) {
        super(context, context.url("configureSecurity/"));
    }

    @Override
    public URL getConfigUrl() {
        return url;
    }

    public <T extends SecurityRealm> T useRealm(Class<T> type) {
        maybeCheckUseSecurity();
        return selectFromRadioGroup(type);
    }

    public <T extends AuthorizationStrategy> T useAuthorizationStrategy(Class<T> type) {
        maybeCheckUseSecurity();
        return selectFromRadioGroup(type);
    }

    private void maybeCheckUseSecurity() {
        try {
            control("/useSecurity").check();
        } catch (NoSuchElementException x) {
            // JENKINS-40228, OK
        }
    }

    public <T extends BuildAccessControl> T addBuildAccessControl(final Class<T> type) {
        final String path = createPageArea("/jenkins-security-QueueItemAuthenticatorConfiguration/authenticators", new Runnable() {
            @Override public void run() {
                control(by.path("/jenkins-security-QueueItemAuthenticatorConfiguration/hetero-list-add[authenticators]")).selectDropdownMenu(type);
            }
        });

        return newInstance(type, this, path);
    }

    public void selectSafeHtmlFormatter() {
        this.selectMarkupFormatter(SAFE_HTML);
    }

    private void selectMarkupFormatter(final String formatter) {
        find(by.option(formatter)).click();
    }

    private <T> T selectFromRadioGroup(Class<T> type) {
        WebElement radio = findCaption(type, new Finder<WebElement>() {
            @Override protected WebElement find(String caption) {
                return getElement(by.radioButton(caption));
            }
        });

        radio.click();

        return newInstance(type, this, radio.getAttribute("path"));
    }

    /**
     * Decides whether script security for Job DSL scripts is enabled.
     *
     * @param enable Use script security if true.
     */
    public void setJobDslScriptSecurity(boolean enable) {
        control(by.name("_.useScriptSecurity")).check(enable);
    }
}
