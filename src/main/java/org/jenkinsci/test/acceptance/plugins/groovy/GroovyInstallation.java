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

import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.ToolInstallation;
import org.jenkinsci.test.acceptance.po.ToolInstallationPageObject;

@ToolInstallationPageObject(name="Groovy", installer="hudson.plugins.groovy.GroovyInstaller")
public class GroovyInstallation extends ToolInstallation {

    public static final String DEFAULT_GROOVY_ID = "default_Groovy";

    public static void installSomeGroovy(Jenkins jenkins) {
        installGroovy(jenkins, DEFAULT_GROOVY_ID, "4.0.12");
    }

    public static void installGroovy(Jenkins jenkins, String name, String version) {
        ToolInstallation.installTool(jenkins, GroovyInstallation.class, name, version);
    }

    public GroovyInstallation(Jenkins context, String path) {
        super(context, path);
    }

    public void useNative() {
        installedIn(fakeHome("groovy", "GROOVY_HOME"));
    }
}
