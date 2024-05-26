/*
 * The MIT License
 *
 * Copyright (c) Red Hat, Inc.
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
package core;

import static org.junit.Assert.assertThrows;

import java.util.concurrent.ExecutionException;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.CapybaraPortingLayerImpl;
import org.jenkinsci.test.acceptance.po.PluginManager;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

public class PluginManagerTest extends AbstractJUnitTest {

    @Test @Issue("JENKINS-36239")
    public void reproduce() throws Exception {
        PluginManager pm = jenkins.getPluginManager();
        pm.checkForUpdates();
        pm.open();
    }

    @Test
    @WithPlugins("matrix-auth")
    public void uninstall_plugin() throws InterruptedException, ExecutionException {
        jenkins.getPluginManager().visit("installed");
        try {
            WebElement uninstallButton = find(by.xpath(".//button[./@data-href = 'plugin/matrix-auth/doUninstall']"));
            uninstallButton.click();
            waitFor(by.button("Yes"));
        } catch (NoSuchElementException te) {
            // TODO remove this handling when Jenkins 2.415 is the lowest we support
            WebElement form = find(by.action("plugin/matrix-auth/uninstall"));
            form.submit();
            waitFor(form).until(CapybaraPortingLayerImpl::isStale);
        }
        clickButton("Yes");
        jenkins.restart();
        jenkins.getPluginManager().visit("installed");
        assertThrows(NoSuchElementException.class, () -> find(by.url("plugin/matrix-auth")));
    }
}
