package org.jenkinsci.test.acceptance;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.Session;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Vivek Pandey
 */
public class Ssh implements AutoCloseable {
    private final Connection connection;

    public Ssh(String hostname) throws IOException {
        this.connection = new Connection(hostname);
        connection.connect();
    }

    /**
     * Escapes a path to a form suitable for use on a command-line.
     * @param path the path.
     * @return the escaped path.
     */
    public static String escape(String path) {
        int index = path.indexOf('\'');
        if (index == -1) {
            if (path.matches("^[a-zA-Z0-9_/:-]*$")) {
                return path;
            }
            return "'" + path + "'";
        }
        StringBuilder buf = new StringBuilder(path.length() + 16);
        int start = 0;
        do {
            if (index > 0) {
                buf.append('\'');
                buf.append(path.substring(start, index));
                buf.append("'\\'");
            } else {
                buf.append("\\'");
            }
            start = index + 1;
            index = path.indexOf('\'', start);
        } while (index != -1);
        if (start < path.length()) {
            buf.append('\'');
            buf.append(path.substring(start));
            buf.append('\'');
        }
        return buf.toString();
    }

    /**
     * Get the connection and authenticate before using any method
     */
    public Connection getConnection() {
        return connection;
    }


    public int executeRemoteCommand(String cmd, OutputStream os) {
        Session session = null;
        try {
            session = connection.openSession();
            int status = connection.exec(cmd, os);
            if (status != 0) {
                throw new RuntimeException("Failed to execute command: " + cmd + ", exit code = " + status);
            }
            return status;
        } catch (InterruptedException | IOException e) {
            throw new AssertionError(e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Execute remote command, log output using System.out *
     */
    public int executeRemoteCommand(String cmd) {
        return executeRemoteCommand(cmd, System.out);
    }

    public void copyTo(String localFile, String remoteFile, String targetDir) throws IOException {
        SCPClient scpClient = new SCPClient(connection);
        scpClient.put(localFile, remoteFile, targetDir, "0755");
    }

    /**
     * @deprecated use {@link #close()}
     */
    @Deprecated
    public void destroy() {
        connection.close();
    }

    @Override
    public void close() {
        connection.close();
    }
}