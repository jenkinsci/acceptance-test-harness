/*
 * The MIT License
 *
 * Copyright 2014 Jesse Glick.
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

import com.google.inject.Injector;
import java.net.URL;
import javax.annotation.Nonnull;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.junit.Assert;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

@Describable("org.jenkinsci.plugins.workflow.job.WorkflowJob")
public class WorkflowJob extends Job {

    public WorkflowJob(Injector injector, URL url, String name) {
        super(injector, url, name);
    }

    public final Control script = new Control(this, "/definition/script") {
        @Override
        public void set(String text) {
            try {
                super.set(text);
            } catch (NoSuchElementException x) {
                // As of JENKINS-28769, the textarea is set display: none due to the ACE editor, so CapybaraPortingLayerImpl.find, called by super.resolve, calls isDisplayed and fails.
                // Anyway we cannot use the web driver to interact with the hidden textarea.
                // Some code cannibalized from #45:
                String cssSelector = "#workflow-editor-1";
                waitForRenderOf(cssSelector + " .ace_text-input", driver);
                executeScript(
                    "var targets = document.getElementsBySelector(arguments[0]);" +
                            "if (!targets || targets.length === 0) {" +
                            "    throw '**** Failed to find ACE Editor target object on page. Selector: ' + arguments[0];" +
                            "}" +
                            "if (!targets[0].aceEditor) {" +
                            "    throw '**** Selected ACE Editor target object is not an active ACE Editor. Selector: ' + arguments[0];" +
                            "}" +
                            "targets[0].aceEditor.setValue(arguments[1]);",
                    cssSelector, text);
            }
        }
    };
    private static void waitForRenderOf(@Nonnull String cssSelector, @Nonnull WebDriver driver) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() < start + 10000) {
            if (isRendered(cssSelector, driver)) {
                return;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                break;
            }
        }
        Assert.fail("Timed out waiting on '" + cssSelector + "' to be rendered.");
    }
    private static boolean isRendered(@Nonnull String cssSelector, @Nonnull WebDriver driver) {
        assertIsJavaScriptExecutor(driver);
        return (boolean) ((JavascriptExecutor)driver).executeScript(
                "var targets = document.getElementsBySelector(arguments[0]);" +
                        "if (!targets || targets.length === 0) {" +
                        "    return false;" +
                        "} else {" +
                        "    return true;" +
                        "}", cssSelector);
    }
    private static void assertIsJavaScriptExecutor(WebDriver driver) {
        if (!(driver instanceof JavascriptExecutor)) {
            throw new IllegalArgumentException("WebDriver type " + driver.getClass().getName() + " does not implement JavascriptExecutor.");
        }
    }

    public final Control sandbox = control("/definition/sandbox");

    public String copyResourceStep(String filePath) {
        final Resource res = resource(filePath);
        return String.format("sh '''%s'''%n", copyResourceShell(res, res.getName()));
    }

}
