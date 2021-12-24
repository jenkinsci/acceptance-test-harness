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

import org.jenkinsci.test.acceptance.po.*;

@Describable("Invoke Gradle script")
public class GradleStep extends AbstractStep implements BuildStep {

    private final Control file = control("buildFile");
    private final Control dir = control("rootBuildScriptDir");
    private final Control switches = control("switches");
    private final Control tasks = control("tasks");
    private final Control wrapperLocation = control("wrapperLocation");
    private final Control projectProperties = control("projectProperties");
    private final Control systemProperties = control("systemProperties");

    public GradleStep(Job parent, String path) {
        super(parent, path);
    }

    private void ensureAdvancedOptionsOpen() {
        Control advancedButton = control("advanced-button");
        if(advancedButton.exists()) {
            advancedButton.click();

            // Sticky footer obscures this, trying another click seems to work better
            if (advancedButton.exists()) {
                advancedButton.click();
            }
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
        control("").resolve().findElement(by.checkbox("Use Gradle Wrapper")).click();
    }

    public void setWrapperLocation(final String wrapperLocation){
        ensureAdvancedOptionsOpen();
        this.wrapperLocation.set(wrapperLocation);
    }

    public void setMakeWrapperExecutable(){
        ensureAdvancedOptionsOpen();
        control("").resolve().findElement(by.checkbox("Make gradlew executable")).click();
    }

    public void setProjectProperties(final String projectProperties){
        ensureAdvancedOptionsOpen();
        this.projectProperties.set(projectProperties);
    }

    public void setPassAllAsProjectProperties(){
        ensureAdvancedOptionsOpen();
        control("").resolve().findElement(by.checkbox("Pass all job parameters as Project properties")).click();
    }

    public void setSystemProperties(final String systemProperties){
        ensureAdvancedOptionsOpen();
        this.systemProperties.set(systemProperties);
    }

    public void setPassAllAsSystemProperties(){
        ensureAdvancedOptionsOpen();
        control("").resolve().findElement(by.checkbox("Pass all job parameters as System properties")).click();
    }

    public void setForceGradleHomeToUseWorkspace(){
        ensureAdvancedOptionsOpen();
        control("").resolve().findElement(by.checkbox("Force GRADLE_USER_HOME to use workspace")).click();
    }

}
