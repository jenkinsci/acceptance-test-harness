package org.jenkinsci.test.acceptance.controller;

import com.google.common.io.Files;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.SCPClient;
import com.trilead.ssh2.Session;
import org.jclouds.domain.LoginCredentials;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.base.Charsets.UTF_8;

/**
 * @author Vivek Pandey
 */
public class Ssh{
    private final Connection connection;
    private final Session session;
    private final LoginCredentials loginCredentials;

    public Ssh(String user, String hostname) throws IOException {
        this.loginCredentials = getLoginForCommandExecution();
        this.connection = new Connection(hostname);

        connection.connect();

        if(!connection.authenticateWithPublicKey(user,
                loginCredentials.getPrivateKey().toCharArray(), loginCredentials.getPassword())){
            throw new RuntimeException("Authentication failed");
        }

        this.session = connection.openSession();

    }

    public static void main(String[] args) throws IOException {
        Ssh ssh = new Ssh("user","ec2-54-197-156-232.compute-1.amazonaws.com");
        String jenkinsHome = "jenkins_home";

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        ssh.executeRemoteCommand("mkdir "+jenkinsHome, os);

        ssh.copyTo("/Users/vivek/Downloads/jenkins.war","jenkins.war", ".");

        ssh.executeRemoteCommand(String.format("java -jar jenkins.war -DJENKINS_HOME=%s --ajp13Port=-1 --controlPort=%s --httpPort=%s", jenkinsHome, 8080, 8081), os);

        System.out.println(new String(os.toByteArray()));
    }

    public int executeRemoteCommand(String cmd,OutputStream os) throws IOException {
        try {
            int status = connection.exec(cmd, os);
            if(status != 0){
                throw new RuntimeException("Failed to execute command: "+cmd);
            }
            return status;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            session.close();
        }
    }

    public void copyTo(String localFile, String remoteFile, String targetDir) throws IOException {
        SCPClient scpClient = new SCPClient(connection);
        scpClient.put(localFile,remoteFile, targetDir,"0755");
    }

    public void destroy(){
        session.close();
        connection.close();
    }

    private  LoginCredentials getLoginForCommandExecution(){
        try {
            String user = System.getProperty("user.name");
            String privateKey = Files.toString(
                    new File(System.getProperty("user.home") + "/.ssh/id_rsa"), UTF_8);
            return LoginCredentials.builder().
                    user(user).privateKey(privateKey).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}