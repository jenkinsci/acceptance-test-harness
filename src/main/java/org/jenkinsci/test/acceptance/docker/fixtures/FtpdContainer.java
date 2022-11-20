package org.jenkinsci.test.acceptance.docker.fixtures;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Represents a server with FTPD.
 * Uses FtpClient to allow easy acces to files.
 * It is assumed that the ftpd server of the docker fixture is configured using passive connection
 * and the ports 21,7050,...,7055
 *
 * @author Tobias Meyer
 */
@DockerFixture(id = "ftpd", matchHostPorts = true, ports = {21, 9050, 9051, 9052, 9053, 9054, 9055})
public class FtpdContainer extends DockerContainer implements IPasswordDockerContainer {
    private FTPClient ftpClient;

    private final String username = "test";

    private final String password = "test";

    public FtpdContainer() {
        ftpClient = new FTPClient();
    }

    /**
     * Gets the ftp password of the ftp user on the docker server
     *
     * @return ftp password
     */
    public String getPassword() {
        return password;
    }

    /**
     * If the internal ftp client is connected to ftp server on the docker fixture, disconnects.
     */
    public void ftpDisconnect() {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.disconnect();
            } catch (IOException ignored) {

            }
        }
    }

    /**
     * Establish a ftp connection to the ftp server running on the docker fixture
     *
     * @return true if connection is okay, false if failed
     */
    public Boolean ftpConnect() {
        try {
            ftpClient.connect(ipBound(21), port(21));
            ftpClient.enterLocalPassiveMode();
            ftpClient.setRemoteVerificationEnabled(false);

            if (!ftpClient.login(username, password))
                return false;

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        } catch (IOException ex) {
            ftpDisconnect();
            return false;
        }
        return true;
    }

    /**
     * Gets the username of the ftp user on the docker server
     *
     * @return ftp username
     */
    public String getUsername() {
        return username;
    }


    /**
     * Connects to the ftp server and uploads one File from the localPath to the remote Path.
     * The binary Transfer mode is used. After the transfer is finished the client disconnects from the server.
     *
     * @param localPath  The file to transfer
     * @param remotePath The remote path
     */
    public void uploadBinary(String localPath, String remotePath) throws IOException {
        FileInputStream fis = null;
        if (!ftpConnect())
            throw new IOException("Connection to ftp Failed!");
        try {
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE, FTP.BINARY_FILE_TYPE);
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE);
            fis = new FileInputStream(localPath);
            ftpClient.storeFile(remotePath, fis);
        } finally {
            if (fis != null)
                fis.close();
            ftpDisconnect();
        }
    }

    /**
     * Checks if a Path exist on the docker ftp server
     *
     * @param Path the Path to check
     * @return true if the Path exist, else false
     */
    public Boolean pathExist(String Path) throws IOException {
        if (!ftpConnect())
            throw new IOException("Connection to ftp Failed!");
        FTPFile[] files = ftpClient.listFiles(Path);
        ftpDisconnect();
        return files.length > 0;
    }

}
