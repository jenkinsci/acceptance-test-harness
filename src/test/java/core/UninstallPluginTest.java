/*
 * The MIT License
 *
 * Copyright (c) 2014 Sony Mobile Communications Inc. All rights reserved.
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

import com.google.inject.Inject;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Test;
import org.openqa.selenium.WebElement;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeTrue;

/**
 * Feature: Uninstall plugin test
 * @author Orjan Percy <orjan.percy@sonymobile.com>
 */
@WithPlugins("gerrit-trigger")
public class UninstallPluginTest extends AbstractJUnitTest {

    @Inject
    private SlaveController slaves;

    /** Uninstall a plugin (gerrit-trigger), restart jenkins and verify that the plugin is not installed */
    @Test
    public void gerrit_uninstall_plugin() throws InterruptedException, ExecutionException {
        assumeTrue("This test requires a restartable Jenkins", jenkins.canRestart());
        jenkins.getPluginManager().visit("installed");
        check(find(by.url("plugin/gerrit-trigger")), false);
        WebElement form = find(by.action("plugin/gerrit-trigger/uninstall"));
        form.submit();
        jenkins.restart();
        Slave s = slaves.install(jenkins).get();
        s.waitUntilOnline();
        jenkins.getPluginManager().visit("installed");
        WebElement trigger = find(by.url("plugin/gerrit-trigger"));
        assertFalse(trigger.isSelected());
    }
}
