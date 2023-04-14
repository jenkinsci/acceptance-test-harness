/*
 * The MIT License
 *
 * Copyright (c) 2023 CloudBees, Inc.
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
package org.jenkinsci.test.acceptance.utils;

import org.jenkinsci.test.acceptance.docker.fixtures.GitListContainer;
import org.openqa.selenium.WebDriver;

public class RepositoryBrowserUtils {

    private final WebDriver driver;
    private final String repoHost;
    private final int repoPort;

    public RepositoryBrowserUtils(WebDriver driver, String repoHost, int repoPort) {
        this.driver = driver;
        this.repoHost = repoHost;
        this.repoPort = repoPort;
    }

    public String getCommitUrl(final String revision, final RepositoryBrowserType type) {
        String commitURL;
        switch (type) {
            case GITLAB:
                commitURL = "http://" + repoHost + ":" + repoPort + "/" + GitListContainer.REPO_NAME + "/commits/" + revision;
                break;
            case PHABRICATOR:
                commitURL = "http://" + repoHost + ":" + repoPort + "/r" + GitListContainer.REPO_NAME + revision;
                break;
            case VIEWGIT:
                commitURL = "http://" + repoHost + ":" + repoPort + "/" + GitListContainer.REPO_NAME + "/\\?p=" + GitListContainer.REPO_NAME + "&amp;a=commit&amp;h=" + revision;
                break;
            case GITBLIT:
                commitURL = "http://" + repoHost + ":" + repoPort + "/" + GitListContainer.REPO_NAME + "/commit\\?r=" + GitListContainer.REPO_NAME + "&amp;h=" + revision;
                break;
            default:
                commitURL = "http://" + repoHost + ":" + repoPort + "/" + GitListContainer.REPO_NAME + "/commit/" + revision;
        }

        return commitURL;
    }

    public WebDriver visit(String revision) {
        return visit(revision, RepositoryBrowserType.DEFAULT_BROWSER);
    }

    public WebDriver visit(String revision, RepositoryBrowserType type) {
        driver.get(getCommitUrl(revision, type));
        return driver;
    }

    /**
     * Enum with all possible values for special configurations for Repository browser
     */
    public enum RepositoryBrowserType {
        GITLAB,
        PHABRICATOR,
        VIEWGIT,
        GITBLIT,
        DEFAULT_BROWSER
    }
}
