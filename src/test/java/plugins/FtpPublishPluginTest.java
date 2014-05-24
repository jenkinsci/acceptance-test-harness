package plugins;

import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.fixtures.FtpdContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.WinstoneContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.ftp.FtpGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.ftp.FtpGlobalConfig.Site;
import org.jenkinsci.test.acceptance.plugins.ftp.FtpPublisher;
import org.jenkinsci.test.acceptance.plugins.nodelabelparameter.LabelParameter;
import org.jenkinsci.test.acceptance.plugins.nodelabelparameter.NodeParameter;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.LabelAxis;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveProvider;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.singletonMap;

/**
 * Feature: Tests for FTP plugin
 * Warning for a working Test of FTP with docker, we need to disable
 * RemoteVerification in the FTPClient of Java!
 *
 * @author Tobias Meyer
 */
@Native("docker")
@WithPlugins("publish-over-ftp")
public class FtpPublishPluginTest extends AbstractJUnitTest {
    @Inject
    Docker docker;
    @Inject
    SlaveProvider slaves;
    /**
     * Helper method to create a temporary empty directory
     *
     * @return File Descriptor for empty Directory
     * @throws IOException
     */
    private static File createTempDirectory()
            throws IOException {
        File temp;

        temp = File.createTempFile("temp", Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }
        temp = new File(temp.getPath() + "d");

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }
        temp.deleteOnExit();
        return (temp);
    }

    /**
     * Helper method to configure Jenkins.
     * It adds the DockerContainer as FTP Server with the name
     *
     * @param servername Name to Access Instance
     * @param ftpd       Docker Instance of the Server
     */
    private void jenkinsFtpConfigure(String servername, FtpdContainer ftpd) {
        jenkins.configure();
        Site s = new FtpGlobalConfig(jenkins).addSite();
        {
            s.name.set(servername);
            s.hostname.set(ftpd.ipBound(21));
            s.port.set(ftpd.port(21));
            s.username.set(ftpd.getUsername());
            s.password.set(ftpd.getPassword());
            s.remoteDir.set("/tmp");
            s.disableRemoteVerification.check(true);
        }
        jenkins.save();
    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a docker fixture "ftpd"
     * And a job
     * When I configure docker fixture as FTP host
     * And I configure the job with one FTP Transfer Set
     * And I configure the Transfer Set
     * With Source Files "odes.txt"
     * And With Remote Directory myfolder/
     * And I copy resource "odes.txt" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have published "odes.txt" on docker fixture
     */

    @Test
    public void publish_resources() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_file = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        jenkinsFtpConfigure("asd", ftpd);
        j.configure();
        {
            j.copyResource(cp_file);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();

        j.startBuild().shouldSucceed();
        ftpd.cp("/tmp/odes.txt", new File("/tmp"));
        assertThat(FileUtils.readFileToString(new File("/tmp/odes.txt")), CoreMatchers.is(cp_file.asText()));
    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a docker fixture "ftpd"
     * And a job
     * When I configure docker fixture as FTP host
     * And I configure the job with one FTP Transfer Set
     * And I configure the Transfer Set
     * With Source Files "odes.txt"
     * And With Remote Directory "/tmp/${JOB_NAME}"
     * And I copy resource "odes.txt" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have published "odes.txt" on docker fixture
     */

    @Test
    public void publish_jenkins_variables() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_file = resource("/ftp_plugin/odes.txt");
        String randomName = jenkins.jobs.createRandomName();
        String randomPath = "/tmp/" + randomName + "/";
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class, randomName);

        jenkinsFtpConfigure("asd", ftpd);
        j.configure();
        {
            j.copyResource(cp_file);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().remoteDirectory.set("${JOB_NAME}/");
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();

        j.startBuild().shouldSucceed();
        ftpd.cp(randomPath + "odes.txt", new File("/tmp"));
        assertThat(FileUtils.readFileToString(new File("/tmp/odes.txt")), CoreMatchers.is(cp_file.asText()));
    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a docker fixture "ftpd"
     * And a job
     * When I configure docker fixture as FTP site
     * And I configure the job with one FTP Transfer Set
     * And I configure the Transfer Set
     * With Source Files "prefix_/test.txt"
     * And With Remote Directory /tmp/
     * And With remove prefix
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have published "test.txt" on docker fixture
     */

    @Test
    public void publish_resources_and_remove_prefix() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_dir = resource("/ftp_plugin/");
        Resource test = resource("/ftp_plugin/prefix_/test.txt");
        FreeStyleJob j = jenkins.jobs.create();
        jenkinsFtpConfigure("asd", ftpd);
        j.configure();
        {
            j.copyDir(cp_dir);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("prefix_/test.txt");
            fps.getDefaultTransfer().removePrefix.set("prefix_");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(ftpd.pathExist("/tmp/test.txt"));
    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a resources Directory "prefix_/"
     * With the file ".exclude"
     * And a docker fixture "ftpd"
     * And a job
     * When I configure docker fixture as FTP site
     * And I configure the job with one FTP Transfer Set
     * And I configure the Transfer Set
     * With Source Files  "prefix_/"
     * And With exclude ".exclude"
     * And I copy resources "prefix_/" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have published "prefix_/" without the ".exclude" on docker fixture
     */

    @Test
    public void publish_resources_with_excludes() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_dir = resource("/ftp_plugin/");

        FreeStyleJob j = jenkins.jobs.create();
        jenkinsFtpConfigure("asd", ftpd);
        j.configure();
        {
            j.copyDir(cp_dir);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("prefix_/");
            fps.getDefaultTransfer().excludes.set("**/*.exclude");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(ftpd.pathExist("/tmp/prefix_/"));
        assertTrue(!ftpd.pathExist("/tmp/prefix_/.exclude"));
    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a docker fixture "ftpd"
     * And a job
     * When I configure docker fixture as FTP site
     * And I configure the job with one FTP Transfer Set
     * And I configure the Transfer Set
     * With Source Files "prefix_/test.txt,odes.txt"
     * And I copy resources "prefix_/test.txt,odes.txt"  into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have published "prefix_/test.txt" and "odes.txt"
     */

    @Test
    public void publish_with_default_pattern() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_dir = resource("/ftp_plugin/");

        FreeStyleJob j = jenkins.jobs.create();
        jenkinsFtpConfigure("asd", ftpd);
        j.configure();
        {
            j.copyDir(cp_dir);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("prefix_/test.txt,odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(ftpd.pathExist("/tmp/prefix_/test.txt"));
        assertTrue(ftpd.pathExist("/tmp/odes.txt"));
    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a docker fixture "ftpd"
     * And a job
     * When I configure docker fixture as FTP site
     * And I configure the job with one FTP Transfer Set
     * And I configure the Transfer Set
     * With Source Files "te,st.txt;odes.txt"
     * And With Pattern separator [;]+
     * And I copy resources "te,st.txt,odes.txt"  into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have published "te,st.txt" and "odes.txt"
     */

    @Test
    public void publish_with_own_pattern() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_dir = resource("/ftp_plugin/");

        FreeStyleJob j = jenkins.jobs.create();
        jenkinsFtpConfigure("asd", ftpd);
        j.configure();
        {
            j.copyDir(cp_dir);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().patternSeparator.set("[;]+");
            fps.getDefaultTransfer().sourceFile.set("te,st.txt;odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(ftpd.pathExist("/tmp/te,st.txt"));
        assertTrue(ftpd.pathExist("/tmp/odes.txt"));
    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a docker fixture "ftpd"
     * And a job
     * When I configure docker fixture as FTP site
     * And I configure the job with one FTP Transfer Set
     * And I configure the Transfer Set
     * With Source Files ".svn,.git,odes.txt"  with FTP plugin
     * And I copy resources "odes.txt" as "odes.txt,.svn,.git" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have published "odes.txt"
     * And FTP plugin should have not published  ".svn,.git"
     */

    @Test
    public void publish_with_default_exclude() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_txt = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        jenkinsFtpConfigure("asd", ftpd);
        j.configure();
        {
            j.copyResource(cp_txt, ".svn");
            j.copyResource(cp_txt, "CVS");
            j.copyResource(cp_txt);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set(".svn,CVS,odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(ftpd.pathExist("/tmp/odes.txt"));
        assertTrue(!ftpd.pathExist("/tmp/.svn"));
        assertTrue(!ftpd.pathExist("/tmp/CVS"));

    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a docker fixture "ftpd"
     * And a job
     * When I configure docker fixture as FTP site
     * And I configure the job with one FTP Transfer Set
     * And I configure the Transfer Set
     * With Source Files ".svn,.git,odes.txt"  with FTP plugin
     * And With No default excludes checked
     * And I copy resources "odes.txt" as "odes.txt,.svn,.git" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have published  .svn/,.git"
     */

    @Test
    public void publish_with_no_default_exclude() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_txt = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        jenkinsFtpConfigure("asd", ftpd);
        j.configure();
        {
            j.copyResource(cp_txt, ".svn");
            j.copyResource(cp_txt, "CVS");
            j.copyResource(cp_txt);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set(".svn,CVS,odes.txt");
            fps.getDefaultTransfer().noDefaultExcludes.check();
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(ftpd.pathExist("/tmp/odes.txt"));
        assertTrue(ftpd.pathExist("/tmp/.svn"));
        assertTrue(ftpd.pathExist("/tmp/CVS"));
    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a docker fixture "ftpd"
     * And a job
     * When I configure docker fixture as FTP site
     * And I configure the job with one FTP Transfer Set
     * And I configure the Transfer Set
     * With Source Files "empty/,odes.txt" with FTP plugin
     * And With Make empty dirs  Checked
     * And I create the directory "empty/" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have published  "empty/" and "odes.txt"
     */

    @Test
    public void publish_with_empty_directory() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_txt = resource("/ftp_plugin/odes.txt");
        File tmpDir = createTempDirectory();
        File nestedEmptyDir = new File(tmpDir + "/empty");
        nestedEmptyDir.mkdir();

        FreeStyleJob j = jenkins.jobs.create();
        jenkinsFtpConfigure("asd", ftpd);
        j.configure();
        {
            j.copyResource(cp_txt);
            j.copyFile(tmpDir);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("empty/,odes.txt");
            fps.getDefaultTransfer().makeEmptyDirs.check();
        }

        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(ftpd.pathExist("/tmp/empty/"));
        assertTrue(ftpd.pathExist("/tmp/odes.txt"));
    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a docker fixture "ftpd"
     * And a job
     * When I configure docker fixture as FTP site
     * And I configure the job with one FTP Transfer Set
     * And I configure the Transfer Set
     * With Source Files "empty/,odes.txt" with FTP plugin
     * And I create the directory "empty/" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have not published "empty/" and "odes.txt"
     */

    @Test
    public void publish_without_empty_directory() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_txt = resource("/ftp_plugin/odes.txt");
        File tmpDir = createTempDirectory();
        File nestedEmptyDir = new File(tmpDir + "/empty");
        nestedEmptyDir.mkdir();

        FreeStyleJob j = jenkins.jobs.create();
        jenkinsFtpConfigure("asd", ftpd);
        j.configure();
        {
            j.copyResource(cp_txt);
            j.copyFile(tmpDir);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("empty/,odes.txt");

        }

        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(ftpd.pathExist("/tmp/odes.txt"));
        assertTrue(!ftpd.pathExist("/tmp/empty"));
    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a docker fixture "ftpd"
     * And a job
     * When I configure docker fixture as FTP site
     * And I configure the job with one FTP Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles "odes.txt" and "flat\odes.txt" with FTP plugin
     * And I copy resources "ftp_plugin/"
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have published "odes.txt" and "flat/odes.txt"
     */

    @Test
    public void publish_without_flatten_files() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_dir = resource("/ftp_plugin/");

        FreeStyleJob j = jenkins.jobs.create();
        jenkinsFtpConfigure("asd", ftpd);
        j.configure();
        {
            j.copyDir(cp_dir);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("flat/odes.txt,odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(ftpd.pathExist("/tmp/flat/odes.txt"));
        assertTrue(ftpd.pathExist("/tmp/odes.txt"));
    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a docker fixture "ftpd"
     * And a job
     * When I configure docker fixture as FTP site
     * And I configure the job with one FTP Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles "odes.txt" and "flat\odes.txt" with FTP plugin
     * And With Flatten files Checked
     * And I copy resources "ftp_plugin/"
     * And I save the job
     * And I build the job
     * Then the build should fail
     */

    @Test
    public void publish_with_flatten_files() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_dir = resource("/ftp_plugin/");

        FreeStyleJob j = jenkins.jobs.create();
        jenkinsFtpConfigure("asd", ftpd);
        j.configure();
        {
            j.copyDir(cp_dir);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().flatten.check();
            fps.getDefaultTransfer().sourceFile.set("flat/odes.txt,odes.txt");
        }
        j.save();
        j.startBuild().shouldUnstable();
    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a docker fixture "ftpd"
     * And a job
     * When I configure docker fixture as FTP site
     * And I configure the job with one FTP Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles "odes.txt" with FTP plugin
     * And With Remote directory  is a date format Checked "yyyyMMddHH"
     * And I copy resources "odes.txt" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have published "odes.txt" under 'yyyyMMddHH'
     */

    @Test
    public void publish_with_date_format() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_txt = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        jenkinsFtpConfigure("asd", ftpd);
        j.configure();
        {
            j.copyResource(cp_txt);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().remoteDirectorySDF.check();
            fps.getDefaultTransfer().remoteDirectory.set("yyyyMMddHH");
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
        Date date = new Date();
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(ftpd.pathExist("/tmp/" + dateFormat.format(date) + "/odes.txt"));
    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a docker fixture "ftpd"
     * And I Upload "oldfile.txt" to the ftp Server
     * And a job
     * And I configure the job with one FTP Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles "myresources" with FTP plugin
     * And With Clean remote
     * And I copy resources "myresources" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have published "myresources"
     * And the RemoteDIR should not contain "old.txt"
     */

    @Test
    public void publish_with_clean_remote() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_txt = resource("/ftp_plugin/odes.txt");
        Resource old_txt = resource("/ftp_plugin/old.txt");
        ftpd.uploadBinary(old_txt.asFile().getAbsolutePath(), "/tmp/old.txt");

        FreeStyleJob j = jenkins.jobs.create();
        jenkinsFtpConfigure("asd", ftpd);
        j.configure();
        {
            j.copyResource(cp_txt);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().cleanRemote.check();
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(ftpd.pathExist("/tmp/odes.txt"));
        assertTrue(!ftpd.pathExist("/tmp/old.txt"));
    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a docker fixture "ftpd"
     * And a job
     * When I configure docker fixture as FTP site
     * And I configure the job with three FTP Transfer Sets
     * And I configure the Transfer Set 1
     * With SourceFiles  odes.txt
     * And I configure the Transfer Set 2
     * With SourceFiles  odes2.txt
     * And I configure the Transfer Set 3
     * With SourceFiles  odes3.txt
     * And I copy resources odes.txt,odes2.txt,odes3.txt into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have published odes.txt,odes2.txt,odes3.txt on docker fixture
     */
    @Test
    public void publish_multiple_sets() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_txt = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        jenkinsFtpConfigure("asd", ftpd);
        j.configure();
        {
            j.copyResource(cp_txt);
            j.copyResource(cp_txt, "odes2.txt");
            j.copyResource(cp_txt, "odes3.txt");
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
            fps.addTransferSet().sourceFile.set("odes2.txt");
            fps.addTransferSet().sourceFile.set("odes3.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(ftpd.pathExist("/tmp/odes.txt"));
        assertTrue(ftpd.pathExist("/tmp/odes2.txt"));
        assertTrue(ftpd.pathExist("/tmp/odes3.txt"));
    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And 2 docker fixtures "ftpd"
     * And a job
     * When I configure docker docker1 as FTP site
     * And I configure docker  docker2 as FTP site
     * And I configure the job with docker1 Server
     * And one FTP Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles odes.txt
     * And I Add Server docker2
     * And one FTP Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles odes.txt
     * And I copy resources odes.txt  into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have published my set of files at all docker fixtures
     */
    @Test
    public void publish_multiple_servers() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        FtpdContainer ftpd2 = docker.start(FtpdContainer.class);
        Resource cp_txt = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        jenkinsFtpConfigure("docker1", ftpd);
        jenkinsFtpConfigure("docker2", ftpd2);
        j.configure();
        {
            j.copyResource(cp_txt);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.configName.select("docker1");
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
            FtpPublisher.Site fps2 = fp.addServer();
            fps2.configName.select("docker2");
            fps2.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(ftpd.pathExist("/tmp/odes.txt"));
        assertTrue(ftpd2.pathExist("/tmp/odes.txt"));

    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing
     * Given I have installed the "ftp" plugin
     * And a docker fixture "ftpd"
     * And a ssh slave
     * And a job
     * When I configure docker fixture as FTP host
     * And I configure the job with one FTP Transfer Set
     * And I configure the Transfer Set
     * With Source Files "odes.txt"
     * And With Remote Directory myfolder/
     * And I copy resource "odes.txt" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And FTP plugin should have published "odes.txt" on docker fixture
     */

    @Test
    public void publish_slave_resourses() throws IOException, InterruptedException,ExecutionException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_file = resource("/ftp_plugin/odes.txt");

        Slave s = slaves.get().install(jenkins).get();
        s.configure();
        s.save();

        FreeStyleJob j = jenkins.jobs.create();
        jenkinsFtpConfigure("asd", ftpd);
        j.configure();
        {
            j.copyResource(cp_file);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        ftpd.cp("/tmp/odes.txt", new File("/tmp"));
        assertThat(FileUtils.readFileToString(new File("/tmp/odes.txt")), CoreMatchers.is(cp_file.asText()));
    }
}
