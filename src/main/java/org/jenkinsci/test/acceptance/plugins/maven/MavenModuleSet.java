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
import java.util.function.Consumer;

import org.jenkinsci.test.acceptance.po.BuildStep;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Job;
import org.jenkinsci.test.acceptance.po.PostBuildStep;
import org.jenkinsci.test.acceptance.po.ShellBuildStep;
import org.openqa.selenium.WebElement;

import com.google.inject.Injector;

@Describable("hudson.maven.MavenModuleSet")
public class MavenModuleSet extends Job {
    public final Control version = control("/name"); // only visible if there are at least 2 versions
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

    public void setGoals(final String goals) {
        this.goals.set(goals);
    }

    private void ensureAdvanced() {
        if (advancedButton == null) return;

        advancedButton.click();
        advancedButton = null;
    }

    /**
     * Enables the specified settings for this job. Settings (i.e. publishers) are stored in a list member to provide
     * later access for modification. After the settings have been added they are configured
     * with the specified configuration lambda. Afterwards, the job configuration page still is visible and
     * not saved.
     *
     * @param type          the settings to configure
     * @param configuration the additional configuration options for this job
     * @param <T>           the type of the settings
     * @see #addBuildSettings(Class)
     * @see #getPublisher(Class)
     */
    public <T extends PostBuildStep> T addBuildSettings(final Class<T> type, final Consumer<T> configuration) {
        T settings = addBuildSettings(type);

        configuration.accept(settings);

        return settings;
    }

    /**
     * Enables the specified settings for this job. Settings (i.e. publishers) are stored in a list member to provide
     * later access for modification. Afterwards, the job configuration page still is visible and
     * not saved.
     *
     * @param type          the settings to configure
     * @param <T>           the type of the settings
     * @see #addBuildSettings(Class)
     * @see #getPublisher(Class)
     */
    public <T extends PostBuildStep> T addBuildSettings(Class<T> type) {
        WebElement checkbox = findCaption(type, new Finder<WebElement>() {
            @Override protected WebElement find(String caption) {
                return outer.find(by.checkbox(caption));
            }
        });
        checkbox.click();
        T bs = newInstance(type, this, checkbox.getAttribute("path"));

        publishers.add(bs);
        return bs;
    }

    /**
     * Wrapper function to get a previously added build settings object.
     * @param type the type of the build settings to be retrieved
     * @return the build settings object
     */
    public <T extends PostBuildStep> T getBuildSettings(Class<T> type) {
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

    /**
     * Use the default maven version for a job. Note that this maven version needs to be installed before
     * this method is called. Additionally, at least 2 versions need to be installed. Otherwise the drop down
     * menu is not shown and the default version is used.
     *
     * @see MavenInstallation#ensureThatMavenIsInstalled(Jenkins)
     * @see MavenInstallation#installSomeMaven(Jenkins)
     */
    public void useDefaultMavenVersion() {
        version.select(MavenInstallation.DEFAULT_MAVEN_ID);
    }
}
