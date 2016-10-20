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

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.jenkinsci.utils.process.CommandBuilder;

/**
 * @author ogondza
 * @see ToolInstallationPageObject
 */
public abstract class ToolInstallation extends PageAreaImpl {
    public final Control name = control("name");
    private final Control autoInstall = control("properties/hudson-tools-InstallSourceProperty");

    public static void waitForUpdates(final Jenkins jenkins, final Class<? extends ToolInstallation> type) {

        if (hasUpdatesFor(jenkins, type)) return;

        jenkins.getPluginManager().checkForUpdates();

        jenkins.waitFor()
                .withMessage("tool installer metadata for %s has arrived", type.getAnnotation(ToolInstallationPageObject.class).installer())
                .withTimeout(60, TimeUnit.SECONDS)
                .until(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return hasUpdatesFor(jenkins, type);
                    }
        });
    }

    private static boolean hasUpdatesFor(final Jenkins jenkins, Class<? extends ToolInstallation> type) {
        return Boolean.parseBoolean(jenkins.runScript(
                "println DownloadService.Downloadable.get('%s').data != null",
                type.getAnnotation(ToolInstallationPageObject.class).installer()
        ));
    }

    public static <T extends ToolInstallation> T addTool(Jenkins jenkins, Class<T> type) {
        final ConfigurablePageObject page = getPageObject(jenkins);
        page.configure();

        final String name = type.getAnnotation(ToolInstallationPageObject.class).name();
        final Control button = page.control(by.button("Add " + name));

        String pathPrefix = button.resolve().getAttribute("path").replaceAll("repeatable-add", "tool");
        String path = page.createPageArea(pathPrefix, new Runnable() {
            @Override public void run() {
                button.click();
            }
        });
        return page.newInstance(type, jenkins, path);
    }

    public static <T extends ToolInstallation> void installTool(Jenkins jenkins, Class<T> type, String name, String version) {
        waitForUpdates(jenkins, type);

        ConfigurablePageObject tools = getPageObject(jenkins);
        tools.configure();
        T maven = addTool(jenkins, type);
        maven.name.set(name);
        maven.installVersion(version);
        tools.save();
    }

    public ToolInstallation(Jenkins jenkins, String path) {
        super(getPageObject(jenkins), path);
    }

    protected static ConfigurablePageObject getPageObject(Jenkins jenkins) {
        // TODO: change to jenkins.getVersion().isOlderThan(new VersionNumber("2")) after 2.0 is released.
        // yeah, you could do a number of things here e.g. isNewerThan("1.1000") etc,
        // but this is fine as it's temporary.
        return jenkins.getVersion().toString().startsWith("1.")
                ? new JenkinsConfig(jenkins)
                : new GlobalToolConfig(jenkins);
    }

    @Override
    public ConfigurablePageObject getPage() {
        return (ConfigurablePageObject) super.getPage();
    }

    public ToolInstallation installVersion(String version) {
        autoInstall.check();
        control("properties/hudson-tools-InstallSourceProperty/installers/id").select(version);
        return this;
    }

    public ToolInstallation installedIn(String home) {
        autoInstall.uncheck();
        control("home").set(home);
        return this;
    }

    protected String fakeHome(String binary, String homeEnvName) {
        try {
            final File home = File.createTempFile("toolhome", binary);

            home.delete();
            new File(home, "bin").mkdirs();
            home.deleteOnExit();

            if (SystemUtils.IS_OS_UNIX) {
                final String path = new CommandBuilder("which", binary).popen().asText().trim();
                final String code = String.format(
                        "#!/bin/sh\nexport %s=\nexec %s \"$@\"\n",
                        homeEnvName, path
                );

                final File command = new File(home, "bin/" + binary);
                FileUtils.writeStringToFile(command, code);
                command.setExecutable(true);
            }
            else {
                String path = new CommandBuilder("where.exe", binary).popen().asText().trim();
                // where will return all matches and we only want the first.
                path = path.replaceAll("\r\n.*", "");
                final String code = String.format("set %s=\r\ncall %s %%*\r\n",
                                                  homeEnvName, path
                                          );
                final File command = new File(home, "bin/" + binary + ".cmd");
                FileUtils.writeStringToFile(command, code);
                command.setExecutable(true);
            }
            return home.getAbsolutePath();
        }
        catch (IOException ex) {
            throw new Error(ex);
        }
        catch (InterruptedException ex) {
            throw new Error(ex);
        }
    }
}
