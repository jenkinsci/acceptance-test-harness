package plugins;

import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.IPasswordDockerContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.SMBContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.publish_over.CifsGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.publish_over.CifsPublisher;
import org.jenkinsci.test.acceptance.plugins.publish_over.PublishGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.publish_over.PublishGlobalPublisher;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveProvider;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Feature: Tests for CIFS plugin
 *
 * @author Tobias Meyer
 */
@WithPlugins("publish-over-cifs")
public class CIFSPublishPluginTest extends GlobalPublishPluginTest {


    /**
     * Creates & Returns the instance of the correspending Docker Container for this publisher.
     * @return
     */
    protected  DockerContainer CreatePublisherContainer()
    {
        return docker.start(SMBContainer.class);
    }

    /**
     *
     * @return
     */
    protected  PublishGlobalConfig.GlobalSite CreateGlobalConfig()
    {
        return new CifsGlobalConfig(jenkins).addSite();
    }

    /**
     *
     * @return
     */
    protected  PublishGlobalPublisher AddGlobalPublisher(FreeStyleJob j)
    {
        return  j.addPublisher(CifsPublisher.class);
    }

    /**
     * Helper method to configure Jenkins.
     * It adds the DockerContainer with smbsmb Server with the name
     *
     * @param servername Name to Access Instance
     * @param dock       Docker Instance of the Server
     */
    protected void configurePublisher(String servername, DockerContainer dock) {
        jenkins.configure();
        CifsGlobalConfig.CifSite s = new CifsGlobalConfig(jenkins).addSite();
        {
            s.name.set(servername);
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
