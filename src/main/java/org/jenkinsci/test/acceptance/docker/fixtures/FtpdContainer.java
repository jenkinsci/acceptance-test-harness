package org.jenkinsci.test.acceptance.docker.fixtures;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;
import java.io.FileInputStream;

import java.io.File;
import java.io.IOException;

import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;

/**
 * Represents a server with FTPD.
 * Uses FtpClient to allow easy acces to files.
 * @author Tobias Meyer
 */
@DockerFixture(id="ftpd",ports={21,7050,7051,7052,7053,7054,7055},bindIp="127.0.0.2")
public class FtpdContainer extends DockerContainer {
    private FTPClient ftpClient;
    private final String username = "test";

    public FtpdContainer()
    {
        ftpClient = new FTPClient();
    }
    public String getPassword() {
        return password;
    }
    public  void FtpDisconnect(){
        try {
            ftpClient.disconnect();
        } catch (IOException f) {

        }
    }

    public Boolean FtpConnect(){
        try {
            ftpClient.connect(ipBound(21), port(21));
            ftpClient.enterLocalPassiveMode();
            ftpClient.setRemoteVerificationEnabled(false);

            if(!ftpClient.login(username,password))
                return false;

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        }
        catch (IOException ex)
        {
            if(ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException f) {

                }
            }
            return false;
        }
        return true;
    }

    public String getUsername() {
        return username;
    }

    private final String password = "test";

    public void UploadBinary(String localPath, String remotePath) throws IOException{
        FileInputStream fis = null;
        if(!FtpConnect())
            throw new IOException("Connection to ftp Failed!");
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            fis = new FileInputStream(localPath);
            ftpClient.storeFile(remotePath, fis);
        }
        finally {
            if(fis!=null)
                fis.close();
            FtpDisconnect();
        }
    }

    public Boolean PathExist(String Path) throws IOException{
        if(!FtpConnect())
            throw new IOException("Connection to ftp Failed!");
        FTPFile[] files = ftpClient.listFiles(Path);
        FtpDisconnect();
        if(files.length>0)
            return true;
        else
            return false;
    }

}
