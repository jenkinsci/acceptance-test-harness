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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Logger;

import edu.umd.cs.findbugs.annotations.NonNull;

public class IOUtil {

    private static final Logger LOGGER = Logger.getLogger(IOUtil.class.getName());
    private static final ElasticTime time = new ElasticTime();

    /**
     * Get First existing file or directory.
     * @param directory true if looking for file.
     * @return File representing the file or directory.
     * @throws IOException If nothing found.
     */
    public static @NonNull File firstExisting(boolean directory, String... candidatePaths) throws IOException {
        for (String path: candidatePaths) {
            if (path == null) continue;
            File f = new File(path);
            if (directory ? f.isDirectory() : f.isFile()) {
                return f;
            }
        }

        throw new IOException("None of the paths exist: " + Arrays.asList(candidatePaths));
    }

    /**
     * Open URL connection with sanity timeout.
     */
    public static HttpURLConnection openConnection(@NonNull URL url) throws IOException {
        int timeout = (int) time.milliseconds(10000);

        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setConnectTimeout(timeout);
        httpURLConnection.setReadTimeout(timeout);
        return httpURLConnection;
    }

    public static String multiline(String... lines) {
        String newline = System.lineSeparator();
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append(newline);
        }
        return sb.toString();
    }

    /**
     * Gives random available TCP port in the given range.
     *
     * @param from if {@code <=0} then default value 49152 is used
     * @param to   if {@code <=0} then default value 65535 is used
     */
    public static int randomTcpPort(int from, int to){
        from = (from <=0) ? 49152 : from;
        to = (to <= 0) ? 65535 : to;


        while(true){
            int candidate = (int) ((Math.random() * (to-from)) + from);
            if(isTcpPortFree(candidate)){
                return candidate;
            }
            LOGGER.info(String.format("Port %s is in use", candidate));
        }
    }

    /**
     * Gives random available TCP port.
     */
    public static int randomTcpPort(){
        return randomTcpPort(-1,-1);
    }

    public static boolean isTcpPortFree(int port){
        try {
            ServerSocket ss = new ServerSocket(port);
            ss.close();
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
