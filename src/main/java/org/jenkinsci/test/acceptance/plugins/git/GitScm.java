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
package org.jenkinsci.test.acceptance.plugins.git;

import org.jenkinsci.test.acceptance.po.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.support.ui.Select;

@Describable("Git")
public class GitScm extends Scm {
    public final Control branch = control("branches/name");
    private final Control url = control("userRemoteConfigs/url");

    public GitScm(Job job, String path) {
        super(job, path);
    }

    public GitScm url(String url) {
        this.url.set(url);
        return this;
    }

    public GitScm credentials(String name) {
        Select select = new Select(control(By.className("credentials-select")).resolve());
        select.selectByVisibleText(name);
        return this;
    }

    public void localBranch(String branch) {
        try {
            advanced();
            control("localBranch").set(branch);
        }
        catch (NoSuchElementException ex) { // Git 2.0
            addBehaviour(CheckoutToLocalBranch.class).name.set(branch);
        }
    }

    public void localDir(String dir) {
        try {
            advanced();
            control("relativeTargetDir").set(dir);
        }
        catch (NoSuchElementException ex) { // Git 2.0
            addBehaviour(CheckoutToLocalDir.class).name.set(dir);
        }
    }

    public void remoteName(String name) {
        remoteAdvanced();
        control("userRemoteConfigs/name").set(name);
    }

    public <T extends Behaviour> T addBehaviour(Class<T> type) {
        control("hetero-list-add[extensions]").click();
        return newInstance(type, this, "extensions");   // FIXME: find the last extension added
    }

    private void advanced() {
        control("advanced-button").click();
    }

    private void remoteAdvanced() {
        control("userRemoteConfigs/advanced-button").click();
    }

    public static class Behaviour extends PageAreaImpl {
        public Behaviour(GitScm git, String path) {
            super(git, path);
        }
    }

    public static class CheckoutToLocalBranch extends Behaviour {
        private final Control name = control("localBranch");

        public CheckoutToLocalBranch(GitScm git, String path) {
            super(git, path);
            clickLink("Check out to specific local branch");
        }
    }

    public static class CheckoutToLocalDir extends Behaviour {
        private final Control name = control("relativeTargetDir");

        public CheckoutToLocalDir(GitScm git, String path) {
            super(git, path);
            clickLink("Check out to a sub-directory");
        }
    }
}
