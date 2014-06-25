package plugins;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.FtpdContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.IPasswordDockerContainer;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.publish_over.*;
import org.jenkinsci.test.acceptance.plugins.publish_over.FtpGlobalConfig.FtpSite;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;

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
     * Creates & Returns a FtpdContainer for the SMB Tests
     * @return FtpdContainer
     */
    protected DockerContainer CreatePublisherContainer()
    {
        return docker.start(FtpdContainer.class);
    }

    /**
     * Creates and returns a FtpGlobalConfig.GlobalSite for the FTP Test
     * @return FtpGlobalConfig.GlobalSite
     */
    protected  PublishGlobalConfig.GlobalSite CreateGlobalConfig()
    {
        return new FtpGlobalConfig(jenkins).addSite();
    }

    /**
     * Creates and returns a PublishGlobalPublisher  for the FTP Test
     * @return PublishGlobalPublisher
     */
    protected  PublishGlobalPublisher AddGlobalPublisher(FreeStyleJob j)
    {
        return  j.addPublisher(FtpPublisher.class);
    }

    /**
     * Helper method to configure Jenkins.
     * It adds the DockerContainer as FTP Server with the name
     *
     * @param serverName Name to Access Instance
     * @param dock       Docker Instance of the Server
     */
    protected void configurePublisher(String serverName, DockerContainer dock) {
        jenkins.configure();
        FtpSite s = new FtpGlobalConfig(jenkins).addSite();
        {
            s.name.set(serverName);
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
