/*
 * The MIT License
 *
 * Copyright (c) 2020 Olivier Lamy.
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

import java.io.File;

public class MavenLocalRepository {

    private final File mavenLocalRepository;

    private MavenLocalRepository() {
        if (System.getProperty("mavenRepoPath") != null) {
            mavenLocalRepository = new File(System.getProperty("mavenRepoPath"));
        } else {
            File userHome = new File(System.getProperty("user.home"));
            mavenLocalRepository = new File(new File(userHome, ".m2"), "repository");
        }
    }

    private static class LazyHolder {
        static final MavenLocalRepository INSTANCE = new MavenLocalRepository();
    }

    public static File getMavenLocalRepository() {
        return LazyHolder.INSTANCE.mavenLocalRepository;
    }
}
