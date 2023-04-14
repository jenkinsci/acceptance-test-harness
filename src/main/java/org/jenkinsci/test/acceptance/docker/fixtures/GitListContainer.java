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
package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

import java.io.IOException;
import java.net.URL;

/**
 * Runs gitlist server container.
 */
@DockerFixture(id = "git", ports = {22, 80})
public class GitListContainer extends DockerContainer {
    protected static final String REPO_DIR = "/home/git/gitRepo";
    public static final String REPO_NAME = "gitRepo";

    public String hostSSH() {
        return ipBound(22);
    }

    public int portSSH() {
        return port(22);
    }

    public String hostHTTP() {
        return ipBound(80);
    }

    public int portHTTP() {
        return port(80);
    }

    public URL getSSHUrl() throws IOException {
        return new URL("http://" + hostSSH() + ":" + portSSH());
    }

    public URL getHTTPUrl() throws IOException {
        return new URL("http://" + hostHTTP() + ":" + portHTTP());
    }

    /** URL visible from the host. */
    public String getRepoUrl() {
        return "ssh://git@" + hostSSH() + ":" + portSSH() + REPO_DIR;
    }

    /** URL browser. */
    public String getUrlRepositoryBrowser() {
        return "http://" + hostHTTP() + ":" + portHTTP() + "/" + REPO_NAME;
    }

}
