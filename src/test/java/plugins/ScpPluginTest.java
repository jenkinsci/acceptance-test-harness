package plugins;

import com.google.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.scp.ScpGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.scp.ScpGlobalConfig.Site;
import org.jenkinsci.test.acceptance.plugins.scp.ScpPublisher;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Feature: Tests for SCP plugin
 */
@WithPlugins("scp")
@Category(DockerTest.class)
@WithDocker
public class ScpPluginTest extends AbstractJUnitTest {
    @Inject
    DockerContainerHolder<SshdContainer> docker;

    @Test
    public void configure_job_with_scp_password_publishing() throws Exception {
        SshdContainer sshd = docker.get();
        Resource cp_file = resource("/scp_plugin/lorem-ipsum-scp.txt");

        FreeStyleJob j = jenkins.jobs.create();

        jenkins.configure();
        Site s = new ScpGlobalConfig(jenkins).addSite();
        {
            s.hostname.set(sshd.ipBound(22));
            s.port.set(sshd.port(22));
            s.username.set("test");
            s.password.set("test");
            s.rootRepositoryPath.set("/tmp");
        }
        jenkins.save();

        j.configure();
        {
            j.copyResource(cp_file);
            ScpPublisher sp = j.addPublisher(ScpPublisher.class);
            ScpPublisher.Site sps = sp.add();
            sps.sourceFile.set("lorem-ipsum-scp.txt");
            sps.filePath.set("abc");
        }
        j.save();

        j.startBuild().shouldSucceed();

        sshd.cp("/tmp/abc/lorem-ipsum-scp.txt", "/tmp");
        assertThat(FileUtils.readFileToString(new File("/tmp/lorem-ipsum-scp.txt")), CoreMatchers.is(cp_file.asText()));
    }

    @Test
    public void configure_job_with_scp_key_publishing() throws Exception {
        SshdContainer sshd = docker.get();
        Resource cp_file = resource("/scp_plugin/lorem-ipsum-scp.txt");

        FreeStyleJob j = jenkins.jobs.create();

        jenkins.configure();
        Site s = new ScpGlobalConfig(jenkins).addSite();
        {
            s.hostname.set(sshd.ipBound(22));
            s.port.set(sshd.port(22));
            s.username.set("test");
            s.keyfile.set(sshd.getPrivateKey().getAbsolutePath());
            s.rootRepositoryPath.set("/tmp");
        }
        jenkins.save();

        j.configure();
        {
            j.copyResource(cp_file);
            ScpPublisher sp = j.addPublisher(ScpPublisher.class);
            ScpPublisher.Site sps = sp.add();
            sps.sourceFile.set("lorem-ipsum-scp.txt");
            sps.filePath.set("abc");
        }
        j.save();

        j.startBuild().shouldSucceed();

        sshd.cp("/tmp/abc/lorem-ipsum-scp.txt", "/tmp");
        assertThat(FileUtils.readFileToString(new File("/tmp/lorem-ipsum-scp.txt")), CoreMatchers.is(cp_file.asText()));
    }
}
