/*
 * The MIT License
 *
 * Copyright (c) 2014 Ericsson
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
package org.jenkinsci.test.acceptance.plugins.gerrit_trigger;

import static org.junit.Assume.assumeNotNull;

/**
 * Thread-unsafe singleton serving gt-prefixed test env args.
 * @author Marco Miller
 */
public class GerritTriggerEnv {

    private static GerritTriggerEnv instance = null;

    private String hostName;
    private String keyPath;
    private String project;
    private String userName;

    private GerritTriggerEnv() {
        hostName = getEnv("gtHostname");
        keyPath = getEnv("gtKeypath");
        project = getEnv("gtProject");
        userName = getEnv("gtUsername");
    }

    public static GerritTriggerEnv getInstance() {
        if(instance == null) {
            instance = new GerritTriggerEnv();
        }
        return instance;
    }

    public String getHostName() {
        return hostName;
    }

    public String getKeyPath() {
        return keyPath;
    }

    public String getProject() {
        return project;
    }

    public String getUserName() {
        return userName;
    }

    private String getEnv(String name) {
        String env = System.getenv(name);
        assumeNotNull(env);
        return env;
    }
}
