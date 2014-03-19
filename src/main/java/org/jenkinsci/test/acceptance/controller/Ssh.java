package org.jenkinsci.test.acceptance.controller;

import com.google.common.io.Files;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.Session;
import org.jclouds.domain.LoginCredentials;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.base.Charsets.UTF_8;

/**
 * @author Vivek Pandey
 */
public class Ssh{
    private final Connection connection;
    private final LoginCredentials loginCredentials;
    private final String user;

    public Ssh(String user, String hostname) throws IOException {
        this.loginCredentials = getLoginForCommandExecution();
        this.connection = new Connection(hostname);

        this.user = user;
        connection.connect();
    }

    /**
     * Get the connection and authenticate before using any method
     */
    public Connection getConnection(){
        return connection;
    }

    public void authenticateWithPublicKey(LoginCredentials credential){
        try {
            if(!connection.authenticateWithPublicKey(user,
                    loginCredentials.getPrivateKey().toCharArray(),
                    loginCredentials.getPassword())){
                throw new RuntimeException("Authentication failed");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } catch (IOException e) {
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

    public static  LoginCredentials getLoginForCommandExecution(){
        return getLoginForCommandExecution(System.getProperty("user.name"),new File(PRIVATE_KEY_FILE));
    }

    public static  LoginCredentials getLoginForCommandExecution(String user, File privateKeyFile){
        try {
            String privateKey = Files.toString(
                    privateKeyFile, UTF_8);
            return LoginCredentials.builder().
                    user(user).privateKey(privateKey).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String PRIVATE_KEY_FILE=System.getProperty("user.home") + "/.ssh/id_rsa";
}