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
 * Feature: Tests for SSH plugin
 * @author jenky-hm
 *
 */
@WithPlugins("publish-over-ssh")
@Native("docker")
public class PublishOverSSHPluginTest extends AbstractJUnitTest {
    @Inject
    Docker docker;

    /*helper method for creating a common config*/
    private void commonConfigKeyFileAllowExec(File sshFile){
        CommonConfig cc = new PublishOverSSHGlobalConfig(jenkins).setCommonConfig(); {
            cc.keyPath.set(sshFile.getAbsolutePath());

        }
    }

    /*helper method for creating a common config no exec*/
    private void commonConfigKeyFileNoExec(File sshFile){
        CommonConfig cc = new PublishOverSSHGlobalConfig(jenkins).setCommonConfig(); {
            cc.keyPath.set(sshFile.getAbsolutePath());

            // for disabling exec global
            cc.disableAllExecGlobal.check();

        }
    }

    /*helper method for creating a common config no exec*/
    private void commonConfigKeyFileAndPasswordNoExec(File sshFile){
        CommonConfig cc = new PublishOverSSHGlobalConfig(jenkins).setCommonConfig(); {
            cc.encryptedPassphrase.set("jenkins-ci");
            cc.keyPath.set(sshFile.getAbsolutePath());

            // for disabling exec global
            cc.disableAllExecGlobal.check();

        }
    }

    /*helper method for creating a common config allow exec*/
    private void commonConfigKeyFileAndPasswordAllowExec(File sshFile){
        CommonConfig cc = new PublishOverSSHGlobalConfig(jenkins).setCommonConfig(); {
            cc.encryptedPassphrase.set("jenkins-ci");
            cc.keyPath.set(sshFile.getAbsolutePath());

        }
    }

    /*helper method for creating */
    private void commonConfigKeyTextAllowExec(File sshFile) throws IOException {
        CommonConfig cc = new PublishOverSSHGlobalConfig(jenkins).setCommonConfig(); {
            String ssh_priv_key_string = FileUtils.readFileToString(sshFile);
            cc.key.set(ssh_priv_key_string);

        }
    }

    /*helper method for creating */
    private void commonConfigKeyTextNoExec(File sshFile) throws IOException {
        CommonConfig cc = new PublishOverSSHGlobalConfig(jenkins).setCommonConfig(); {
            String ssh_priv_key_string = FileUtils.readFileToString(sshFile);
            cc.keyPath.set(ssh_priv_key_string);

            // for disabling exec global
            cc.disableAllExecGlobal.check();

        }
    }

    /*helper method for creating a ssh server config*/
    private InstanceSite instanceConfig(SshdContainer sshd){
        InstanceSite is = new PublishOverSSHGlobalConfig(jenkins).addInstanceSite(); {
            is.name.set("testSSHserver");
            is.hostname.set(sshd.ipBound(22));
            is.username.set("test");
            is.remoteRootDir.set("/tmp");

        }
        return is;
    }

    /*helper method for creating a advanced config*/
    private void advancedConfigAllowExec(InstanceSite is, SshdContainer sshd){
        AdvancedConfig ac = is.addAdvancedConfig(); {
            ac.port.set(sshd.port(22));
            ac.timeout.set("300000");

            // for disabling exec per instance
            //ac.disableAllExecInstance.check();
        }
    }

    /*helper method for creating a advanced config no exec*/
    private void advancedConfigNoExec(InstanceSite is, SshdContainer sshd){
        AdvancedConfig ac = is.addAdvancedConfig(); {
            ac.port.set(sshd.port(22));
            ac.timeout.set("300000");

            // for disabling exec per instance
            ac.disableAllExecInstance.check();
        }
    }

    private void configureJob(FreeStyleJob j, Resource cp_file){
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
    }

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

        FreeStyleJob j = jenkins.jobs.create();
        jenkins.configure();
        this.commonConfigKeyFileAllowExec(sshFile);
        InstanceSite is = this.instanceConfig(sshd);
        this.advancedConfigAllowExec(is,sshd);
        // delete button
        //is.delete.click();

        // validate input
        //is.validate.click();
        jenkins.save();
        this.configureJob(j,cp_file);
        j.save();
        j.startBuild().shouldSucceed();

        sshd.cp("/tmp/lorem-ipsum-scp.txt", new File("/tmp"));
        assertThat(FileUtils.readFileToString(new File("/tmp/lorem-ipsum-scp.txt")), CoreMatchers.is(cp_file.asText()));
    }

    /**
     @native(docker)
     Scenario: Configure a job with over ssh publishing
     Given I have installed the "publish-over-ssh" plugin
     And a docker fixture "sshd"
     And a job
     When I configure docker fixture as SSH site
     And I configure the job to use a unsecure keyfile with passphrase
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
        this.commonConfigKeyFileAndPasswordAllowExec(sshFile);
        InstanceSite is = this.instanceConfig(sshd);
        this.advancedConfigAllowExec(is,sshd);
        jenkins.save();
        this.configureJob(j,cp_file);
        j.save();
        j.startBuild().shouldSucceed();

        sshd.cp("/tmp/lorem-ipsum-scp.txt", new File("/tmp"));
        assertThat(FileUtils.readFileToString(new File("/tmp/lorem-ipsum-scp.txt")), CoreMatchers.is(cp_file.asText()));
    }

    /**
     @native(docker)
     Scenario: Configure a job with over ssh publishing
     Given I have installed the "publish-over-ssh" plugin
     And a docker fixture "sshd"
     And a job
     When I configure docker fixture as SSH site
     And I configure the job to use a unsecure key in a text field without passphrase
     And I copy resource "scp_plugin/lorem-ipsum-scp.txt" into workspace
     And I publish "lorem-ipsum-scp.txt" with SSH plugin
     And I save the job
     And I build the job
     Then the build should succeed
     And SCP plugin should have published "lorem-ipsum-scp.txt" on docker fixture
     */
    @Test

    public void configure_job_with_ssh_key_text_and_no_password_publishing() throws IOException, InterruptedException {
        SshdContainer sshd = docker.start(SshdContainer.class);
        Resource cp_file = resource("/scp_plugin/lorem-ipsum-scp.txt");
        File sshFile = sshd.getPrivateKey();

        FreeStyleJob j = jenkins.jobs.create();

        jenkins.configure();
        this.commonConfigKeyTextAllowExec(sshFile);
        InstanceSite is = this.instanceConfig(sshd);
        this.advancedConfigAllowExec(is,sshd);
        jenkins.save();
        this.configureJob(j,cp_file);
        j.save();
        j.startBuild().shouldSucceed();

        sshd.cp("/tmp/lorem-ipsum-scp.txt", new File("/tmp"));
        assertThat(FileUtils.readFileToString(new File("/tmp/lorem-ipsum-scp.txt")), CoreMatchers.is(cp_file.asText()));
    }

    // FIXME Tests
    // public void configure_job_with_ssh_key_file_and_key_password_publishing() throws IOException, InterruptedException {
    // public void configure_job_with_ssh_key_file_and_no_password_publishing() throws IOException, InterruptedException {
    // public void configure_job_with_ssh_key_text_and_key_password_publishing() throws IOException, InterruptedException {

}
