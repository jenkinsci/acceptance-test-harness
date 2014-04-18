package plugins;

import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.scp.ScpGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.scp.ScpGlobalConfig.Site;
import org.jenkinsci.test.acceptance.plugins.scp.ScpPublisher;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 Feature: Tests for SCP plugin
 */
@WithPlugins("scp")
public class ScpPluginTest extends AbstractJUnitTest {
    @Inject
    Docker docker;

    /**
     @native(docker)
     Scenario: Configure a job with SCP publishing
       Given I have installed the "scp" plugin
       And a docker fixture "sshd"
       And a job
       When I configure docker fixture as SCP site
       And I configure the job
       And I copy resource "pmd_plugin/pmd.xml" into workspace
       And I publish "pmd.xml" with SCP plugin
       And I save the job
       And I build the job
       Then the build should succeed
       And SCP plugin should have published "pmd.xml" on docker fixture
     */
    @Test
    @Native("docker")
    public void configure_job_with_scp_publishing() throws IOException, InterruptedException {
        SshdContainer sshd = docker.start(SshdContainer.class);
        Resource pmd_xml = resource("/pmd_plugin/pmd.xml");

        FreeStyleJob j = jenkins.jobs.create();

        jenkins.configure();
        Site s = new ScpGlobalConfig(jenkins).addSite(); {
            s.hostname.set("localhost");
            s.port.set(sshd.port(22));
            s.username.set("test");
            s.password.set("test");
            s.rootRepositoryPath.set("/tmp");
        }
        jenkins.save();

        j.configure(); {
            j.copyResource(pmd_xml);
            ScpPublisher sp = j.addPublisher(ScpPublisher.class);
            ScpPublisher.Site sps = sp.add();
            sps.sourceFile.set("pmd.xml");
            sps.filePath.set("abc");
        }
        j.save();

        j.startBuild().shouldSucceed();

        sshd.cp("/tmp/abc/pmd.xml", new File("/tmp"));
        assertThat(FileUtils.readFileToString(new File("/tmp/pmd.xml")), CoreMatchers.is(pmd_xml.asText()));

    }
}
