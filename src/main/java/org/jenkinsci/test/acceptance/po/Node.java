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

import com.google.inject.Injector;
import java.net.URL;
import org.openqa.selenium.NoSuchElementException;

/**
 * Common base for Jenkins and Slave.
 *
 * @author ogondza
 */
public abstract class Node extends ContainerPageObject {
    protected Node(Jenkins j, URL url) {
        super(j, url);
    }

    protected Node(Injector i, URL url) {
        super(i, url);
    }

    public abstract String getName();

    public void setExecutors(int n) {
        // On high Res screens the UI redraws, so best wait for numExecutors to be present
        waitFor(by.path("/numExecutors"));
        control("/numExecutors").set(n);
    }

    public void setRemoteFs(String s) {
        find(by.input("_.remoteFS")).sendKeys(s);
    }

    public void setLabels(String l) {
        find(by.path("/labelString")).sendKeys(l);
    }

    /**
     * Run groovy string in groovy console.
     * Defaults to a 30 second timeout.
     *
     * @param script Script text to run.
     * @param args Arguments to String#format in the script.
     * @return String output of the script or null if there is none.
     */
    public String runScript(String script, Object... args) {
        return runScript(script, 30, args);
    }

    /**
     * Run groovy string in groovy console.
     *
     * @param script Script text to run.
     * @param args Arguments to String#format in the script.
     * @param timeoutSeconds Script execution timeout in seconds
     * @return String output of the script or null if there is none.
     * @since TODO
     */
    public String runScript(String script, int timeoutSeconds, Object... args) {
        visit("script");
        CodeMirror cm = new CodeMirror(this, "/script");
        cm.set(String.format(script, args));

        clickButton("Run");

        waitFor(by.xpath("//h2[contains(text(), 'Result')]"), timeoutSeconds);

        try {
            return find(by.css("h2 + pre")).getText().replaceAll("^Result: ", "");
        } catch (NoSuchElementException ex) {
            // Pre element does not exists if there is nothing returned.
            return null;
        }
    }

    public BuildHistory getBuildHistory() {
        return new BuildHistory(this);
    }

    public boolean isTemporarillyOffline() {
        return getJson().get("temporarilyOffline").asBoolean();
    }
}
