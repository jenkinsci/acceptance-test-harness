package plugins;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.FtpdContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.IPasswordDockerContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.SMBContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.publish_over.*;
import org.jenkinsci.test.acceptance.plugins.publish_over.FtpGlobalConfig.FtpSite;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveProvider;
import org.junit.Test;

import com.google.inject.Inject;

import static org.junit.Assert.*;

/**
 * Feature: Tests for FTP plugin
 * Warning for a working Test of FTP with docker, we need to disable
 * RemoteVerification in the FTPClient of Java!
 *
 * @author Tobias Meyer
 */
@WithPlugins("publish-over-ftp")
public class FtpPublishPluginTest extends GlobalPublishPluginTest {


    /**
     * Creates & Returns the instance of the correspending Docker Container for this publisher.
     * @return
     */
    protected DockerContainer CreatePublisherContainer()
    {
        return docker.start(FtpdContainer.class);
    }

    /**
     *
     * @return
     */
    protected  PublishGlobalConfig.GlobalSite CreateGlobalConfig()
    {
        return new FtpGlobalConfig(jenkins).addSite();
    }

    /**
     *
     * @return
     */
    protected  PublishGlobalPublisher AddGlobalPublisher(FreeStyleJob j)
    {
        return  j.addPublisher(FtpPublisher.class);
    }

    /**
     * Helper method to configure Jenkins.
     * It adds the DockerContainer as FTP Server with the name
     *
     * @param servername Name to Access Instance
     * @param dock       Docker Instance of the Server
     */
    protected void configurePublisher(String servername, DockerContainer dock) {
        jenkins.configure();
        FtpSite s = new FtpGlobalConfig(jenkins).addSite();
        {
            s.name.set(servername);
            s.hostname.set(dock.ipBound(21));
            s.port.set(dock.port(21));
            if(dock instanceof IPasswordDockerContainer) {
                s.username.set(((IPasswordDockerContainer)dock).getUsername());
                s.password.set(((IPasswordDockerContainer)dock).getPassword());
            }
            s.remoteDir.set("/tmp");
            s.disableRemoteVerification.check(true);
        }
        jenkins.save();
    }
}
