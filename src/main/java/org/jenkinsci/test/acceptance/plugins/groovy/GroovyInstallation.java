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
package org.jenkinsci.test.acceptance.plugins.groovy;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.ToolInstallation;
import org.jenkinsci.test.acceptance.po.ToolInstallationPageObject;
import org.jenkinsci.utils.process.CommandBuilder;

@ToolInstallationPageObject("Groovy")
public class GroovyInstallation extends ToolInstallation {
    public GroovyInstallation(JenkinsConfig context, String path) {
        super(context, path);
    }

    @Override
    public Pattern updatesPattern() {
        return Pattern.compile("Obtained the updated data file for hudson.plugins.groovy.GroovyInstaller");
    }

    public void useNative() {
        installedIn(fakeHome("groovy", "GROOVY_HOME"));
    }

    private String fakeHome(String binary, String homeEnvName) {
        try {
            final File home = File.createTempFile("toolhome", binary);

            home.delete();
            new File(home, "bin").mkdirs();
            home.deleteOnExit();

            final String path = new CommandBuilder("which", "groovy").popen().asText().trim();
            final String code = String.format(
                    "#!/bin/sh\nexport %s=\nexec %s \"$@\"\n",
                    homeEnvName, path
            );

            final File command = new File(home, "bin/" + binary);
            FileUtils.writeStringToFile(command, code);
            command.setExecutable(true);

            return home.getAbsolutePath();
        } catch (IOException ex) {
            throw new Error(ex);
        } catch (InterruptedException ex) {
            throw new Error(ex);
        }
    }
}
