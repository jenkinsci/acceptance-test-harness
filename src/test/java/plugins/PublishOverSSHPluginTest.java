package plugins;

import com.google.common.io.LineReader;
import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.publish_over_ssh.PublishOverSSHGlobalConfig.AdvancedConfig;
import org.jenkinsci.test.acceptance.plugins.publish_over_ssh.PublishOverSSHGlobalConfig.CommonConfig;
import org.jenkinsci.test.acceptance.plugins.publish_over_ssh.PublishOverSSHGlobalConfig.InstanceSite;
import org.jenkinsci.test.acceptance.plugins.publish_over_ssh.PublishOverSSHPublisher;
import org.jenkinsci.test.acceptance.plugins.publish_over_ssh.PublishOverSSHGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.publish_over_ssh.PublishOverSSHPublisher;
import org.jenkinsci.test.acceptance.plugins.publish_over_ssh.PublishOverSSHPublisher.Publishers;
import org.jenkinsci.test.acceptance.plugins.publish_over_ssh.PublishOverSSHPublisher.TransferSet;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 Feature: Tests for SSH plugin
 */
@WithPlugins("publish-over-ssh")
public class PublishOverSSHPluginTest extends AbstractJUnitTest {
    @Inject
    Docker docker;

    @Native("docker")
    /**
     @native(docker)
     Scenario: Configure a job with over ssh publishing
     Given I have installed the "publish-over-ssh" plugin
     And a docker fixture "sshd"
     And a job
     When I configure docker fixture as SSH site
     And I configure the job to use a unsecure keyfile without passphrase
     And I copy resource "scp_plugin/lorem-ipsum-scp.txt" into workspace
     And I publish "lorem-ipsum-scp.txt" with SSH plugin
     And I save the job
     And I build the job
     Then the build should succeed
     And SCP plugin should have published "lorem-ipsum-scp.txt" on docker fixture
     */
    @Test

    public void configure_job_with_ssh_key_path_and_no_password_publishing() throws IOException, InterruptedException {
        SshdContainer sshd = docker.start(SshdContainer.class);
        Resource cp_file = resource("/scp_plugin/lorem-ipsum-scp.txt");
        File sshFile = sshd.getPrivateKey();
        BufferedReader sshReader = new BufferedReader(new FileReader(sshFile));

        FreeStyleJob j = jenkins.jobs.create();

        jenkins.configure();
        CommonConfig cc = new PublishOverSSHGlobalConfig(jenkins).setCommonConfig(); {
            //cc.encryptedPassphrase.set("jenkins-ci");
            cc.keyPath.set(sshFile.getAbsolutePath());

            // for private key copied in form
//            String ssh_priv_key_string="";
//
//            while(sshReader.ready()){
//                ssh_priv_key_string= ssh_priv_key_string + sshReader.readLine();
//            }
//            cc.key.set(ssh_priv_key_string);


            //or like this:
            // String ssh_priv_key_string = FileUtils.readFileToString(sshFile);

            // for disabling exec global
            //cc.disableAllExecGlobal.check();

        }

        InstanceSite is = new PublishOverSSHGlobalConfig(jenkins).addInstanceSite(); {
            is.name.set("testSSHserver");
            is.hostname.set(sshd.ipBound(22));
            is.username.set("test");
            is.remoteRootDir.set("/tmp");

        }

        AdvancedConfig ac = is.addAdvancedConfig(); {
            ac.port.set(sshd.port(22));
            ac.timeout.set("300000");

            // for disabling exec per instance
            //ac.disableAllExecInstance.check();
        }

        // delete button
        //is.delete.click();

        // validate input
        //is.validate.click();

        jenkins.save();

        j.configure(); {
            j.copyResource(cp_file);
            PublishOverSSHPublisher popsp = j.addPublisher(PublishOverSSHPublisher.class);
            // set default set
            Publishers publishers = popsp.setPublishers();
            // add new set
            //Publishers newSps = popsp.addPublishers();
            // set default set
            TransferSet ts = publishers.setTransferSet();
            // add new set
            //TransferSet newTs = publishers.addTransferSet();
            ts.sourceFiles.set("lorem-ipsum-scp.txt");
            //ts.remoteDirectory.set("testfolder");
            //ts.removePrefix.set("/tmp/");
            //ts.execCommand.set("echo 'i was here' >> /tmp/testecho");
        }
        j.save();

        j.startBuild().shouldSucceed();

        sshd.cp("/tmp/lorem-ipsum-scp.txt", new File("/tmp"));
        assertThat(FileUtils.readFileToString(new File("/tmp/lorem-ipsum-scp.txt")), CoreMatchers.is(cp_file.asText()));
    }

    @Native("docker")
    /**
     @native(docker)
     Scenario: Configure a job with over ssh publishing
     Given I have installed the "publish-over-ssh" plugin
     And a docker fixture "sshd"
     And a job
     When I configure docker fixture as SSH site
     And I configure the job to use a unsecure keyfile without passphrase
     And I copy resource "scp_plugin/lorem-ipsum-scp.txt" into workspace
     And I publish "lorem-ipsum-scp.txt" with SSH plugin
     And I save the job
     And I build the job
     Then the build should succeed
     And SCP plugin should have published "lorem-ipsum-scp.txt" on docker fixture
     */
    @Test

    public void configure_job_with_ssh_key_path_and_key_password_publishing() throws IOException, InterruptedException {
        SshdContainer sshd = docker.start(SshdContainer.class);
        Resource cp_file = resource("/scp_plugin/lorem-ipsum-scp.txt");
        File sshFile = sshd.getEncryptedPrivateKey();

        FreeStyleJob j = jenkins.jobs.create();

        jenkins.configure();
        CommonConfig cc = new PublishOverSSHGlobalConfig(jenkins).setCommonConfig(); {
            cc.encryptedPassphrase.set("jenkins-ci");
            cc.keyPath.set(sshFile.getAbsolutePath());

            // for disabling exec global
            //cc.disableAllExecGlobal.check();

        }

        InstanceSite is = new PublishOverSSHGlobalConfig(jenkins).addInstanceSite(); {
            is.name.set("testSSHserver");
            is.hostname.set(sshd.ipBound(22));
            is.username.set("test");
            is.remoteRootDir.set("/tmp");

        }

        AdvancedConfig ac = is.addAdvancedConfig(); {
            ac.port.set(sshd.port(22));
            ac.timeout.set("300000");

            // for disabling exec per instance
            //ac.disableAllExecInstance.check();
        }

        // delete button
        //is.delete.click();

        // validate input
        //is.validate.click();

        jenkins.save();

        j.configure(); {
            j.copyResource(cp_file);
            PublishOverSSHPublisher popsp = j.addPublisher(PublishOverSSHPublisher.class);
            // set default set
            Publishers publishers = popsp.setPublishers();
            // set default set
            TransferSet ts = publishers.setTransferSet();

            ts.sourceFiles.set("lorem-ipsum-scp.txt");

        }
        j.save();

        j.startBuild().shouldSucceed();

        sshd.cp("/tmp/lorem-ipsum-scp.txt", new File("/tmp"));
        assertThat(FileUtils.readFileToString(new File("/tmp/lorem-ipsum-scp.txt")), CoreMatchers.is(cp_file.asText()));
    }

    // public void configure_job_with_ssh_key_file_and_key_password_publishing() throws IOException, InterruptedException {
    // public void configure_job_with_ssh_key_file_and_no_password_publishing() throws IOException, InterruptedException {
    // public void configure_job_with_ssh_key_text_and_key_password_publishing() throws IOException, InterruptedException {
    // public void configure_job_with_ssh_key_text_and_no_password_publishing() throws IOException, InterruptedException {

}
