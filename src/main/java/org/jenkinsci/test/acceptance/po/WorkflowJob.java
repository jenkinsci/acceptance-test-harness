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

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

import com.google.inject.Injector;

import org.jenkinsci.test.acceptance.junit.Resource;

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
                String cssSelector;
                try {
                    cssSelector = waitFor("#workflow-editor-1");
                } catch(TimeoutException e) {
                    cssSelector = waitFor("#workflow-editor");
                }
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

        private String waitFor(@Nonnull final String selector) {
            waitForRenderOf(selector + " .ace_text-input", getJenkins());
            return selector;
        }
    };
    private static void waitForRenderOf(@Nonnull final String cssSelector, @Nonnull final Jenkins jenkins) {
        jenkins.waitFor().withMessage("Timed out waiting on '" + cssSelector + "' to be rendered.")
                .withTimeout(20, TimeUnit.SECONDS)
                .until(new Callable<Boolean>() {
                    @Override public Boolean call() throws Exception {
                        return isRendered(cssSelector, jenkins);
                    }
                })
        ;
    }
    private static boolean isRendered(@Nonnull String cssSelector, @Nonnull Jenkins jenkins) {
        return (boolean) jenkins.executeScript(
                "var targets = document.getElementsBySelector(arguments[0]);" +
                        "if (!targets || targets.length === 0) {" +
                        "    return false;" +
                        "} else {" +
                        "    return true;" +
                        "}", cssSelector);
    }

    public final Control sandbox = control("/definition/sandbox");

    public String copyResourceStep(String filePath) {
        final Resource res = resource(filePath);
        return String.format("sh '''%s'''%n", copyResourceShell(res, res.getName()));
    }

    public void delete() {
        open();
        clickLink("Delete Pipeline");
        confirmAlert(2);
    }

}
