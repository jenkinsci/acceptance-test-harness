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
package plugins;

import static org.junit.Assert.assertEquals;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the secure-requester-whitelist plugin.
 */
@WithPlugins({"secure-requester-whitelist"})
public class SecureRequesterWhitelistTest extends AbstractJUnitTest {

    private final static String DESCRIPTOR_COMMON_PATH = "/org-jenkinsci-plugins-secure_requester_whitelist-Whitelist";
    private final static String ALLOW_NO_REFERER_PATH = DESCRIPTOR_COMMON_PATH + "/allowNoReferer";
    private final static String DOMAINS_PATH = DESCRIPTOR_COMMON_PATH + "/domains";

    private final static boolean CHECKED = true;
    private final static boolean NOT_CHECKED = false;
    private final static String EMPTY_DOMAINS = "";
    private final static String MOCK_DOMAINS = "domain1 domain2";

    private GlobalSecurityConfig security;

    @Before
    public void setup() {
        security = new GlobalSecurityConfig(jenkins);
        security.configure();
    }

    @Test
    public void allowNoRefererPersisted() {
        final Control allowNoReferer = new Control(injector, by.path(ALLOW_NO_REFERER_PATH));

        assertControlChecked(allowNoReferer, NOT_CHECKED);

        allowNoReferer.check();
        this.saveAndReopen();

        assertControlChecked(allowNoReferer, CHECKED);

        allowNoReferer.uncheck();
        this.saveAndReopen();

        assertControlChecked(allowNoReferer, NOT_CHECKED);
    }

    @Test
    public void domainsPersisted() {
        final Control domains = new Control(injector, by.path(DOMAINS_PATH));

        assertControlTextIs(domains, EMPTY_DOMAINS);

        domains.sendKeys(MOCK_DOMAINS);
        this.saveAndReopen();

        assertControlTextIs(domains, MOCK_DOMAINS);
        domains.resolve().clear();
        this.saveAndReopen();

        assertControlTextIs(domains, EMPTY_DOMAINS);
    }

    private void assertControlChecked(final Control controlToCheck, final boolean isChecked) {
        assertEquals(controlToCheck.resolve().isSelected(), isChecked);
    }

    private void assertControlTextIs(final Control textArea, final String textValue) {
        assertEquals(textArea.resolve().getText(), textValue);
    }

    private void saveAndReopen() {
        security.save();
        security.configure();
    }

}
