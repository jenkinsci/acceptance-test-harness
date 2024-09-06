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
import org.openqa.selenium.StaleElementReferenceException;

/**
 * Global page area to configure Artifact management.
 * <p>
 * Note this feature is available since 1.532.
 *
 * @author ogondza
 */
public class ArtifactManagement extends PageAreaImpl {

    public ArtifactManagement(JenkinsConfig config) {
        super(config, "/jenkins-model-ArtifactManagerConfiguration");
    }

    public <T extends Factory> T add(final Class<T> impl) {
        String path =
                createPageArea("artifactManagerFactories", () -> control("hetero-list-add[artifactManagerFactories]")
                        .selectDropdownMenu(impl));
        return newInstance(impl, this, path);
    }

    public void clear() {
        while (control("artifactManagerFactories/repeatable-delete").exists()) {
            try {
                control("artifactManagerFactories/repeatable-delete").click();
            } catch (NoSuchElementException | StaleElementReferenceException e) {
                // control of race condition, the element might have been deleted but selenium cache not updated?
            }
        }
    }

    public static class Factory extends PageAreaImpl {
        protected Factory(ArtifactManagement management, String relativePath) {
            super(management, relativePath);
        }
    }
}
