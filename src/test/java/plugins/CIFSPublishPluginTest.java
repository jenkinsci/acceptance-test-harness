package plugins;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.IPasswordDockerContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.SMBContainer;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.publish_over.CifsGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.publish_over.CifsPublisher;
import org.jenkinsci.test.acceptance.plugins.publish_over.PublishGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.publish_over.PublishGlobalPublisher;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;

/**
 * Feature: Tests for CIFS plugin
 *
 * @author Tobias Meyer
 */
@WithPlugins("publish-over-cifs")
public class CIFSPublishPluginTest extends GlobalPublishPluginTest<SMBContainer> {

    @Override
    protected PublishGlobalConfig.GlobalSite createGlobalConfig() {
        return new CifsGlobalConfig(jenkins).addSite();
    }

    @Override
    protected PublishGlobalPublisher addGlobalPublisher(FreeStyleJob j) {
        return j.addPublisher(CifsPublisher.class);
    }

    /**
     * Helper method to configure Jenkins.
     * It adds the DockerContainer with smb Server with the name
     *
     * @param serverName Name to Access Instance
     * @param dock       Docker Instance of the Server
     */
    @Override
    protected void configurePublisher(String serverName, DockerContainer dock) {
        jenkins.configure();
        CifsGlobalConfig.CifSite s = new CifsGlobalConfig(jenkins).addSite();
        {
            s.name.set(serverName);
            s.hostname.set(dock.ipBound(139));
            s.port.set(dock.port(139));
            if(dock instanceof IPasswordDockerContainer) {
                s.username.set(((IPasswordDockerContainer)dock).getUsername());
                s.password.set(((IPasswordDockerContainer)dock).getPassword());
            }
            s.share.set("/tmp");
        }
        jenkins.save();
    }
}
