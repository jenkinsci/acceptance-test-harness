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

import java.net.URL;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AbstractCodeStylePluginBuildSettings;
import org.jenkinsci.test.acceptance.po.*;
import org.openqa.selenium.WebElement;

import com.google.inject.Injector;

@Describable("hudson.maven.MavenModuleSet")
public class MavenModuleSet extends Job {
    public final Control version = control("/name");
    public final Control goals = control("/goals");

    private Control advancedButton = control("/advanced-button[1]");

    public MavenModuleSet(Injector injector, URL url, String name) {
        super(injector, url, name);
    }

    public MavenModuleSet options(String options) {
        ensureAdvanced();
        control("/mavenOpts").set(options);
        return this;
    }

    private void ensureAdvanced() {
        if (advancedButton == null) return;

        advancedButton.click();
        advancedButton = null;
    }

    /**
     * Change build settings.
     * @param type The setting you would like to set and configure, e.g. FindbugsCodeStylePluginMavenBuildSettings.
     * @return an instance of AbstractCodeStylePluginMavenBuildSettings (e.g. FindbugsCodeStylePluginMavenBuildSettings)
     */
    public <T extends AbstractCodeStylePluginBuildSettings> T addBuildSettings(Class<T> type) {
        WebElement radio = findCaption(type, new Finder<WebElement>() {
            @Override protected WebElement find(String caption) {
                return outer.find(by.checkbox(caption));
            }
        });
        radio.click();
        T bs = newInstance(type, this);

        publishers.add(bs);
        return bs;
    }

    /**
     * Wrapper function to get a previously added build settings object.
     * @param type the type of the build settings to be retrieved
     * @return the build settings object
     */
    public <T extends AbstractCodeStylePluginBuildSettings & PostBuildStep> T getBuildSettings(Class<T> type) {
        return getPublisher(type);
    }

    @Override
    public <T extends BuildStep> T addBuildStep(Class<T> type) {
        throw new UnsupportedOperationException("There are no build steps in maven projects");
    }

    @Override
    public ShellBuildStep addShellStep(String shell) {
        ShellBuildStep step = addPreBuildStep(ShellBuildStep.class);
        step.command(shell);
        return step;
    }

    public MavenModule module(String name) {
        return new MavenModule(this, name);
    }

    @Override
    public MavenBuild build(int buildNumber) {
        return new MavenBuild(this,buildNumber);
    }

    @Override
    public MavenBuild getLastBuild() {
        return new MavenBuild(this,"lastBuild");
    }


}
