/*
 * The MIT License
 *
 * Copyright (c) 2015 Red Hat, Inc.
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
package org.jenkinsci.test.acceptance.plugins.config_file_provider;

import hudson.util.VersionNumber;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.openqa.selenium.WebElement;

/**
 * Abstract class for files provided by {@link ConfigFileProvider}.
 *
 * @author ogondza
 */
public abstract class ProvidedFile extends PageObject {

    private final String fileId;

    public final Control id = control("/config/id");
    public final Control name = control("/config/name");
    public final Control content = control("/config/content");

    public ProvidedFile(ConfigFileProvider context, String id) {
        super(context, context.url("editConfig?id=" + id));
        this.fileId = id;
    }

    public String id() {
        return this.fileId;
    }

    public void name(String string) {
        this.name.set(string);
    }

    public void save() {
        clickButton("Submit");
    }

    public void remove() {
        // Use the direct link with versions without the security improvement, otherwise go to the url via GET
        
        //add all the versions
        boolean useNew = getJenkins().getPlugin("config-file-provider").getVersion().isOlderThan(new VersionNumber("3.7.0"));

        if (useNew) {
            WebElement e = find(by.xpath("//a[contains(@onclick, 'removeConfig?id=" + this.fileId + "')]"));
            e.click();    
        } else {
            visit(url("removeConfig?id=" + this.fileId));
        }
    }

    public abstract void content(String content);

}
