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
package org.jenkinsci.test.acceptance.plugins.maven;

import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.ToolInstallation;
import org.jenkinsci.test.acceptance.po.ToolInstallationPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@ToolInstallationPageObject(name="Maven", installer="hudson.tasks.Maven.MavenInstaller")
public class MavenInstallation extends ToolInstallation {

    public static void installSomeMaven(Jenkins jenkins) {
        installMaven(jenkins, "default_maven", "3.0.5");
    }

    /**
     * Ensures that maven is installed. Installs maven if it is not yet installed.
     * @param jenkins
     */
    public static void ensureThatMavenIsInstalled(Jenkins jenkins) {
        jenkins.configure();
        final By advancedButtonBy = by.path("/hudson-tasks-Maven$MavenInstallation/advanced-button");
        WebElement mavenAdvancedButton = jenkins.getElement(advancedButtonBy);
        if(mavenAdvancedButton == null) {
            installMavenImmediately(jenkins, "default_maven", "3.0.5");
        }
    }

    public static void installMaven(Jenkins jenkins, String name, String version) {
        waitForUpdates(jenkins, MavenInstallation.class);
        installMavenImmediately(jenkins, name, version);
    }

    private static void installMavenImmediately(Jenkins jenkins, String name, String version) {
        jenkins.configure();
        MavenInstallation maven = jenkins.getConfigPage().addTool(MavenInstallation.class);
        maven.name.set(name);
        maven.installVersion(version);
        jenkins.save();
    }

    public MavenInstallation(JenkinsConfig context, String path) {
        super(context, path);
    }

    public void useNative() {
        installedIn(fakeHome("mvn", "M2_HOME"));
    }
}
