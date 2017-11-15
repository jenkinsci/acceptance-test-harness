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
package org.jenkinsci.test.acceptance.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

import javax.annotation.Nonnull;

public class IOUtil {

    private static final int CONNECTION_TIMEOUT = Integer.parseInt(SystemEnvironmentVariables.getPropertyVariableOrEnvironment("CONNECTION_TIMEOUT", "10000"));

    /**
     * Get First existing file or directory.
     * @param directory true if looking for file.
     * @return File representing the file or directory.
     * @throws IOException If nothing found.
     */
    public static @Nonnull File firstExisting(boolean directory, String... candidatePaths) throws IOException {
        for (String path: candidatePaths) {
            if (path == null) continue;
            File f = new File(path);
            if (directory ? f.isDirectory() : f.isFile()) {
                return f;
            }
        }

        throw new IOException("None of the paths exist: " + Arrays.asList(candidatePaths).toString());
    }

    /**
     * Open URL connection with sanity timeout.
     */
    public static HttpURLConnection openConnection(@Nonnull URL url) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setConnectTimeout(CONNECTION_TIMEOUT);
        httpURLConnection.setReadTimeout(CONNECTION_TIMEOUT);
        return httpURLConnection;
    }
}
