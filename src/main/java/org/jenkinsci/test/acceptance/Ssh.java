package org.jenkinsci.test.acceptance;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.Session;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Vivek Pandey
 */
public class Ssh{
    private final Connection connection;

    public Ssh(String hostname) throws IOException {
        this.connection = new Connection(hostname);
        connection.connect();
    }

    /**
     * Get the connection and authenticate before using any method
     */
    public Connection getConnection(){
        return connection;
    }


    public int executeRemoteCommand(String cmd,OutputStream os){
        Session session=null;
        try {
            session = connection.openSession();
            int status = connection.exec(cmd, os);
            if(status != 0){
                throw new RuntimeException("Failed to execute command: "+cmd);
            }
            return status;
        } catch (InterruptedException | IOException e) {
            throw new AssertionError(e);
        } finally {
            if(session != null)
                session.close();
        }
    }

    /** Execute remote command, log output using System.out **/
    public int executeRemoteCommand(String cmd){
        return executeRemoteCommand(cmd,System.out);
    }
    public void copyTo(String localFile, String remoteFile, String targetDir) throws IOException {
        SCPClient scpClient = new SCPClient(connection);
        scpClient.put(localFile,remoteFile, targetDir,"0755");
    }

    public void destroy(){
        connection.close();
    }
}