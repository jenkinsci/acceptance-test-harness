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
package org.jenkinsci.test.acceptance.plugins.gradle;

import hudson.util.VersionNumber;
import org.jenkinsci.test.acceptance.po.*;

@Describable("Invoke Gradle script")
public class GradleStep extends AbstractStep implements BuildStep {

    private final Control file = control("buildFile");
    private final Control dir = control("rootBuildScriptDir");
    private final Control switches = control("switches");
    private final Control tasks = control("tasks");
    private final Control useWrapper = control("useWrapper[true]");
    private final Control makeWrapperExecutable = control("makeExecutable");
    private final Control passAsProperties = control("passAsProperties");

    public GradleStep(Job parent, String path) {
        super(parent, path);
    }

    private void ensureAdvancedOptionsOpen() {
        if (parent.getJenkins().getPlugin("gradle").getVersion().compareTo(new VersionNumber("1.26")) > 0) {
            control("advanced-button").click();
        }
    }

    public void setVersion(String version) {
        String path = parent.getJenkins().getPlugin("gradle").isOlderThan("1.24")
            ? "useWrapper[false]/gradleName"
            : "gradleName"
            ;

        control(path).select(version);
    }

    public void setFile(final String file) {
        ensureAdvancedOptionsOpen();
        this.file.set(file);
    }

    public void setDir(final String dir) {
        ensureAdvancedOptionsOpen();
        this.dir.set(dir);
    }

    public void setSwitches(final String switches) {
        ensureAdvancedOptionsOpen();
        this.switches.set(switches);
    }

    public void setTasks(final String tasks) {
        ensureAdvancedOptionsOpen();
        this.tasks.set(tasks);
    }

    public void setUseWrapper(){
        ensureAdvancedOptionsOpen();
        this.useWrapper.click();
    }

    public void setMakeWrapperExecutable(){
        ensureAdvancedOptionsOpen();
        this.makeWrapperExecutable.click();
    }

    public void setPassAsProperties(){
        ensureAdvancedOptionsOpen();
        this.passAsProperties.click();
    }
}
