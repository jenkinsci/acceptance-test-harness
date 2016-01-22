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

import org.openqa.selenium.NoSuchElementException;

/**
 * Global page area to configure Artifact management.
 *
 * Note this feature is available since 1.532.
 *
 * @author ogondza
 */
public class ArtifactManagement extends PageAreaImpl {

    public ArtifactManagement(JenkinsConfig config) {
        super(config, "/jenkins-model-ArtifactManagerConfiguration");
    }

    public <T extends Factory> T add(Class<T> impl) {
        control("hetero-list-add[artifactManagerFactories]").selectDropdownMenu(impl);
        String factoryPath = last(by.xpath("//div[@name='%s']", "artifactManagerFactories")).getAttribute("path");
        return newInstance(impl, this, factoryPath.substring(path("").toString().length()));
    }

    public void clear() {
        try {
            while (control("artifactManagerFactories/repeatable-delete").exists()) {
                control("artifactManagerFactories/repeatable-delete").click();
                // We allow slow Javascripts to remove the deleted element before we click again (JENKINS-32569)
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    // just ignore and continue the cycle
                }
            }
        } catch (NoSuchElementException ignored) {
            //done no more buttons to push
        }
    }

    public static class Factory extends PageAreaImpl {
        protected Factory(ArtifactManagement management, String relativePath) {
            super(management, relativePath);
        }
    }
}
