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
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.test.acceptance.log.LoggingController;
import org.jenkinsci.utils.process.CommandBuilder;

/**
 * @author ogondza
 * @see ToolInstallationPageObject
 */
public abstract class ToolInstallation extends PageAreaImpl {
    public final Control name = control("name");
    private final Control autoInstall = control("properties/hudson-tools-InstallSourceProperty");

    public static void waitForUpdates(Jenkins jenkins, Class<? extends ToolInstallation> type) {
        final ToolInstallationPageObject annotation = type.getAnnotation(ToolInstallationPageObject.class);

        final Pattern pattern = Pattern.compile("Obtained the updated data file for " + Pattern.quote(annotation.installer()));

        // TODO make all controllers LoggingControllers
        if (jenkins instanceof LoggingController) {
            ((LoggingController) jenkins).getLogWatcher().waitForLogged(pattern, 60);
        } else {
            jenkins.getLogger("all").waitForLogged(pattern, 60);
        }
    }

    public ToolInstallation(JenkinsConfig context, String path) {
        super(context, path);
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

            final String path = new CommandBuilder("which", binary).popen().asText().trim();
            final String code = String.format(
                    "#!/bin/sh\nexport %s=\nexec %s \"$@\"\n",
                    homeEnvName, path
            );

            final File command = new File(home, "bin/" + binary);
            FileUtils.writeStringToFile(command, code);
            command.setExecutable(true);

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
