package plugins;

import com.google.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;
import org.jenkinsci.test.acceptance.junit.*;
import org.jenkinsci.test.acceptance.plugins.publish_over.PublishOverSSHGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.publish_over.PublishOverSSHGlobalConfig.AdvancedConfig;
import org.jenkinsci.test.acceptance.plugins.publish_over.PublishOverSSHGlobalConfig.CommonConfig;
import org.jenkinsci.test.acceptance.plugins.publish_over.PublishOverSSHGlobalConfig.InstanceSite;
import org.jenkinsci.test.acceptance.plugins.publish_over.PublishOverSSHPublisher;
import org.jenkinsci.test.acceptance.plugins.publish_over.PublishOverSSHPublisher.Publishers;
import org.jenkinsci.test.acceptance.plugins.publish_over.PublishOverSSHPublisher.TransferSet;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Feature: Tests for SSH plugin
 * @author jenky-hm
 */
@WithPlugins("publish-over-ssh")
@Category(DockerTest.class)
@WithDocker
public class PublishOverSSHPluginTest extends AbstractJUnitTest {
    @Inject
    private DockerContainerHolder<SshdContainer> docker;

    private final String password = "jenkins-ci";

    private final String serverName = "testSSHserver";
    private final String userName = "test";
    private final String rootDir = "/tmp";
    private final String timeout = "300000";
    private final String sourceFile = "lorem-ipsum-scp.txt";
    private final String resourceFilePath = "/scp_plugin/lorem-ipsum-scp.txt";
    private final String tempCopyFile = "/tmp/lorem-ipsum-scp.txt";
    private final String tempPath = "/tmp";
    private final String tempCopyFileEcho = "/tmp/testecho";


    /*helper method for creating a common config with key file but no password and a noExec Flag*/
    private void commonConfigKeyFile(File sshFile, boolean disbaleAllowAccess){
        CommonConfig cc = new PublishOverSSHGlobalConfig(jenkins).setCommonConfig(); {
            cc.keyPath.set(sshFile.getAbsolutePath());
            if(disbaleAllowAccess){
                // for disabling exec global
                cc.disableAllExecGlobal.check();
            }
        }
    }

    /*helper method for creating a common config with key file and password and a noExec Flag*/
    private void commonConfigKeyFileAndPassword(File sshFile, boolean disbaleAllowAccess){
        CommonConfig cc = new PublishOverSSHGlobalConfig(jenkins).setCommonConfig(); {
            cc.encryptedPassphrase.set(password);
            cc.keyPath.set(sshFile.getAbsolutePath());
            if(disbaleAllowAccess){
                // for disabling exec global
                cc.disableAllExecGlobal.check();
            }
        }
    }

    /*helper method for creating a common config with key as text and a noExec Flag*/
    private void commonConfigKeyText(File sshFile, boolean disbaleAllowAccess) throws IOException {
        CommonConfig cc = new PublishOverSSHGlobalConfig(jenkins).setCommonConfig(); {
            String ssh_priv_key_string = FileUtils.readFileToString(sshFile);
            cc.key.set(ssh_priv_key_string);
            if(disbaleAllowAccess){
                // for disabling exec global
                cc.disableAllExecGlobal.check();
            }
        }
    }

    /*helper method for creating a common config with key as text and a noExec Flag*/
    private void commonConfigPassword(String password, boolean disbaleAllowAccess) {
        CommonConfig cc = new PublishOverSSHGlobalConfig(jenkins).setCommonConfig(); {
            cc.encryptedPassphrase.set(password);
            if(disbaleAllowAccess){
                // for disabling exec global
                cc.disableAllExecGlobal.check();
            }
        }
    }

    /*helper method for creating a ssh server config*/
    private InstanceSite instanceConfig(SshdContainer sshd){
        InstanceSite is = new PublishOverSSHGlobalConfig(jenkins).addInstanceSite(); {
            is.name.set(serverName);
            is.hostname.set(sshd.ipBound(22));
            is.username.set(userName);
            is.remoteRootDir.set(rootDir);
        }
        return is;
    }

    /*helper method for creating a advanced config*/
    private void advancedConfigAllowExec(InstanceSite is, SshdContainer sshd){
        AdvancedConfig ac = is.addAdvancedConfig(); {
            ac.port.set(sshd.port(22));
            ac.timeout.set(timeout);
        }
    }

    private void configureJobNoExec(FreeStyleJob j, Resource cp_file){
        j.configure(); {
            j.copyResource(cp_file);
            PublishOverSSHPublisher popsp = j.addPublisher(PublishOverSSHPublisher.class);
            // set default set
            Publishers publishers = popsp.setPublishers();
            // add new set
            //Publishers newSps = popsp.addPublishers();
            // set default set
            TransferSet ts = publishers.setTransferSet();
            // set source file
            ts.sourceFiles.set(sourceFile);
        }
    }

    private void configureJobWithExec(FreeStyleJob j, Resource cp_file){
        j.configure(); {
            j.copyResource(cp_file);
            PublishOverSSHPublisher popsp = j.addPublisher(PublishOverSSHPublisher.class);
            // set default set
            Publishers publishers = popsp.setPublishers();
            // set default set
            TransferSet ts = publishers.setTransferSet();
            // set source file
            ts.sourceFiles.set(sourceFile);
            // exec a command
            ts.execCommand.set("echo 'i was here' >> /tmp/testecho");
        }
    }

    @Test
    public void ssh_key_path_and_no_password_publishing() throws IOException {
        SshdContainer sshd = docker.get();
        Resource cp_file = resource(resourceFilePath);
        File sshFile = sshd.getPrivateKey();

        FreeStyleJob j = jenkins.jobs.create();
        jenkins.configure();
        this.commonConfigKeyFile(sshFile, false);
        InstanceSite is = this.instanceConfig(sshd);
        this.advancedConfigAllowExec(is,sshd);
        // delete button
        //is.delete.click();

        // validate input
        //is.validate.click();
        jenkins.save();
        this.configureJobNoExec(j, cp_file);
        j.save();
        j.startBuild().shouldSucceed();

        sshd.cp(tempCopyFile, tempPath);
        assertThat(FileUtils.readFileToString(new File(tempCopyFile)), CoreMatchers.is(cp_file.asText()));
    }

    @Test
    @Category(SmokeTest.class)
    public void ssh_key_path_and_key_password_publishing() throws IOException {
        SshdContainer sshd = docker.get();
        Resource cp_file = resource(resourceFilePath);
        File sshFile = sshd.getEncryptedPrivateKey();

        FreeStyleJob j = jenkins.jobs.create();

        jenkins.configure();
        this.commonConfigKeyFileAndPassword(sshFile, false);
        InstanceSite is = this.instanceConfig(sshd);
        this.advancedConfigAllowExec(is,sshd);
        jenkins.save();
        this.configureJobNoExec(j, cp_file);
        j.save();
        j.startBuild().shouldSucceed();

        sshd.cp(tempCopyFile, tempPath);
        assertThat(FileUtils.readFileToString(new File(tempCopyFile)), CoreMatchers.is(cp_file.asText()));
    }

    @Test
    public void ssh_key_text_and_no_password_publishing() throws IOException {
        SshdContainer sshd = docker.get();
        Resource cp_file = resource(resourceFilePath);

        FreeStyleJob j = jenkins.jobs.create();

        jenkins.configure();
        this.commonConfigPassword("test",false);
        InstanceSite is = this.instanceConfig(sshd);
        this.advancedConfigAllowExec(is,sshd);
        jenkins.save();
        this.configureJobNoExec(j, cp_file);
        j.save();
        j.startBuild().shouldSucceed();

        sshd.cp(tempCopyFile, tempPath);
        assertThat(FileUtils.readFileToString(new File(tempCopyFile)), CoreMatchers.is(cp_file.asText()));
    }

    @Test
    public void ssh_password_publishing() throws IOException {
        SshdContainer sshd = docker.get();
        Resource cp_file = resource(resourceFilePath);
        File sshFile = sshd.getPrivateKey();

        FreeStyleJob j = jenkins.jobs.create();

        jenkins.configure();
        this.commonConfigKeyText(sshFile,false);
        InstanceSite is = this.instanceConfig(sshd);
        this.advancedConfigAllowExec(is,sshd);
        jenkins.save();
        this.configureJobNoExec(j, cp_file);
        j.save();
        j.startBuild().shouldSucceed();

        sshd.cp(tempCopyFile, tempPath);
        assertThat(FileUtils.readFileToString(new File(tempCopyFile)), CoreMatchers.is(cp_file.asText()));
    }

    @Test
    public void ssh_key_path_and_key_password_and_exec_publishing() throws IOException {
        SshdContainer sshd = docker.get();
        Resource cp_file = resource(resourceFilePath);
        File sshFile = sshd.getEncryptedPrivateKey();

        FreeStyleJob j = jenkins.jobs.create();

        jenkins.configure();
        this.commonConfigKeyFileAndPassword(sshFile, false);
        InstanceSite is = this.instanceConfig(sshd);
        this.advancedConfigAllowExec(is,sshd);
        jenkins.save();
        this.configureJobWithExec(j, cp_file);
        j.save();
        j.startBuild().shouldSucceed();

        sshd.cp(tempCopyFile, tempPath);
        sshd.cp(tempCopyFileEcho, tempPath);
        assertThat(FileUtils.readFileToString(new File(tempCopyFile)), CoreMatchers.is(cp_file.asText()));
        assertThat(FileUtils.readFileToString(new File(tempCopyFileEcho)), CoreMatchers.is("i was here\n"));
    }
}
