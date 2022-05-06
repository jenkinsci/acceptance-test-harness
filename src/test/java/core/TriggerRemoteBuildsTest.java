/*
 * The MIT License
 *
 * Copyright (c) 2014 Sony Mobile Communications Inc.
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

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.SmokeTest;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.ServletSecurityRealm;
import org.jenkinsci.test.acceptance.po.StringParameter;
import org.jenkinsci.test.acceptance.selenium.Scroller;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openqa.selenium.WebElement;

/**
 * Test to trigger builds remotely.
 */
public class TriggerRemoteBuildsTest extends AbstractJUnitTest {

    @Test
    @Category(SmokeTest.class)
    public void triggerBuildRemotely() {

        GlobalSecurityConfig sc = new GlobalSecurityConfig(jenkins);
        sc.open();
        sc.useRealm(ServletSecurityRealm.class);
        sc.save();

        FreeStyleJob subject = jenkins.jobs.create();
        subject.configure();
        subject.addParameter(StringParameter.class).setName("ID");
        // Trigger builds remotely (e.g., from scripts)")
        // TODO move to page area
        jenkins.control("/pseudoRemoteTrigger").resolve().findElement(by.xpath("../label")).click();
        jenkins.control("/pseudoRemoteTrigger/authToken").fillIn("authToken", "TOKEN");
        subject.addShellStep("test 'id_to_pass' = $ID");
        subject.save();

        FreeStyleJob trigger = jenkins.jobs.create();
        trigger.addShellStep(
                "curl " + subject.url.toString() + "buildWithParameters?token=TOKEN\\&ID=id_to_pass"
        );
        trigger.save();

        trigger.startBuild().shouldSucceed();
        subject.getLastBuild().shouldSucceed();
    }
}
