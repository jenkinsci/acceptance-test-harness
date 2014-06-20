package plugins;

import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.fixtures.SMBContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.publish_over_cifs.CifsGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.publish_over_cifs.CifsGlobalConfig.Site;
import org.jenkinsci.test.acceptance.plugins.publish_over_cifs.CifsPublisher;
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;

/**
 * Feature: Tests for CIFS plugin
 *
 * @author Tobias Meyer
 */
@Native("docker")
@WithPlugins("publish-over-cifs")
public class CIFSPublishPluginTest extends AbstractJUnitTest {
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
        temp = java.nio.file.Files.createTempDirectory("temp").toFile();
        temp.deleteOnExit();
        return (temp);
    }

    /**
     * Helper method to configure Jenkins.
     * It adds the DockerContainer with smbsmb Server with the name
     *
     * @param servername Name to Access Instance
     * @param smb       Docker Instance of the Server
     */
    private void configureCifs(String servername, SMBContainer smb) {
        jenkins.configure();
        Site s = new CifsGlobalConfig(jenkins).addSite();
        {
            s.name.set(servername);
            s.hostname.set(smb.ipBound(139));
            s.port.set(smb.port(139));
            s.username.set(smb.getUsername());
            s.password.set(smb.getPassword());
            s.share.set("/tmp");
        }
        jenkins.save();
    }

    /**
     * @native(docker) Scenario: Configure a job with smbsmb publishing
     * Given I have installed the "smbsmb" plugin
     * And a docker fixture "smbd"
     * And a job
     * When I configure docker fixture as smbsmb host
     * And I configure the job with one smbsmb Transfer Set
     * And I configure the Transfer Set
     * With Source Files "odes.txt"
     * And With Remote Directory myfolder/
     * And I copy resource "odes.txt" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And CIFS plugin should have published "odes.txt" on docker fixture
     */

    @Test
    public void publish_resources() throws IOException, InterruptedException {
        SMBContainer smbd = docker.start(SMBContainer.class);
        Resource cpFile = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        configureCifs("asd", smbd);
        j.configure();
        {
            j.copyResource(cpFile);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();

        j.startBuild().shouldSucceed();

        assertTrue(smbd.tryCopyFile("/tmp/odes.txt","/tmp/"));
        assertThat(FileUtils.readFileToString(new File("/tmp/odes.txt")), CoreMatchers.is(cpFile.asText()));
    }

    /**
     * @native(docker) Scenario: Configure a job with smbsmb publishing
     * Given I have installed the "smbsmb" plugin
     * And a docker fixture "smb"
     * And a job
     * When I configure docker fixture as smbsmb host
     * And I configure the job with one smbsmb Transfer Set
     * And I configure the Transfer Set
     * With Source Files "odes.txt"
     * And With Remote Directory "/tmp/${JOB_NAME}"
     * And I copy resource "odes.txt" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And smbsmb plugin should have published "odes.txt" on docker fixture
     */

    @Test
    public void publish_jenkins_variables() throws IOException, InterruptedException {
        SMBContainer smb = docker.start(SMBContainer.class);
        Resource cpFile = resource("/ftp_plugin/odes.txt");
        String randomName = jenkins.jobs.createRandomName();
        String randomPath = "/tmp/" + randomName + "/";
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class, randomName);

        configureCifs("asd", smb);
        j.configure();
        {
            j.copyResource(cpFile);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().remoteDirectory.set("${JOB_NAME}/");
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();

        j.startBuild().shouldSucceed();
        assertTrue(smb.tryCopyFile(randomPath + "odes.txt","/tmp/"));
        assertThat(FileUtils.readFileToString(new File("/tmp/odes.txt")), CoreMatchers.is(cpFile.asText()));
    }

    /**
     * @native(docker) Scenario: Configure a job with smb publishing
     * Given I have installed the "smb" plugin
     * And a docker fixture "smb"
     * And a job
     * When I configure docker fixture as smb site
     * And I configure the job with one smbs Transfer Set
     * And I configure the Transfer Set
     * With Source Files "prefix_/test.txt"
     * And With Remote Directory /tmp/
     * And With remove prefix
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And smb plugin should have published "test.txt" on docker fixture
     */

    @Test
    public void publish_resources_and_remove_prefix() throws IOException, InterruptedException {
        SMBContainer smb = docker.start(SMBContainer.class);
        Resource cpDir = resource("/ftp_plugin/");
        Resource test = resource("/ftp_plugin/prefix_/test.txt");
        FreeStyleJob j = jenkins.jobs.create();
        configureCifs("asd", smb);
        j.configure();
        {
            j.copyDir(cpDir);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("prefix_/test.txt");
            fps.getDefaultTransfer().removePrefix.set("prefix_");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(smb.tryCopyFile("/tmp/test.txt","/tmp"));
        assertThat(FileUtils.readFileToString(new File("/tmp/test.txt")), CoreMatchers.is(test.asText()));
    }

    /**
     * @native(docker) Scenario: Configure a job with smbsmb publishing
     * Given I have installed the "smbsmb" plugin
     * And a resources Directory "prefix_/"
     * With the file ".exclude"
     * And a docker fixture "smb"
     * And a job
     * When I configure docker fixture as smbsmb site
     * And I configure the job with one smbsmb Transfer Set
     * And I configure the Transfer Set
     * With Source Files  "prefix_/"
     * And With exclude ".exclude"
     * And I copy resources "prefix_/" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And smbsmb plugin should have published "prefix_/" without the ".exclude" on docker fixture
     */

    @Test
    public void publish_resources_with_excludes() throws IOException, InterruptedException {
        SMBContainer smb = docker.start(SMBContainer.class);
        Resource cpDir = resource("/ftp_plugin/");

        FreeStyleJob j = jenkins.jobs.create();
        configureCifs("asd", smb);
        j.configure();
        {
            j.copyDir(cpDir);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("prefix_/");
            fps.getDefaultTransfer().excludes.set("**/*.exclude");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(smb.tryCopyFile("/tmp/prefix_/", "/tmp"));
        assertTrue(!smb.tryCopyFile("/tmp/prefix_/.exclude", "/tmp"));
    }

    /**
     * @native(docker) Scenario: Configure a job with smbsmb publishing
     * Given I have installed the "smbsmb" plugin
     * And a docker fixture "smb"
     * And a job
     * When I configure docker fixture as smbsmb site
     * And I configure the job with one smbsmb Transfer Set
     * And I configure the Transfer Set
     * With Source Files "prefix_/test.txt,odes.txt"
     * And I copy resources "prefix_/test.txt,odes.txt"  into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And smbsmb plugin should have published "prefix_/test.txt" and "odes.txt"
     */

    @Test
    public void publish_with_default_pattern() throws IOException, InterruptedException {
        SMBContainer smb = docker.start(SMBContainer.class);
        Resource cpDir = resource("/ftp_plugin/");

        FreeStyleJob j = jenkins.jobs.create();
        configureCifs("asd", smb);
        j.configure();
        {
            j.copyDir(cpDir);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("prefix_/test.txt,odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(smb.tryCopyFile("/tmp/prefix_/test.txt", "/tmp"));
        assertTrue( smb.tryCopyFile("/tmp/odes.txt", "/tmp"));
        assertTrue(new File("/tmp/test.txt").exists());
        assertTrue(new File("/tmp/odes.txt").exists());
    }

    /**
     * @native(docker) Scenario: Configure a job with smbsmb publishing
     * Given I have installed the "smbsmb" plugin
     * And a docker fixture "smb"
     * And a job
     * When I configure docker fixture as smbsmb site
     * And I configure the job with one smbsmb Transfer Set
     * And I configure the Transfer Set
     * With Source Files "te,st.txt;odes.txt"
     * And With Pattern separator [;]+
     * And I copy resources "te,st.txt,odes.txt"  into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And smbsmb plugin should have published "te,st.txt" and "odes.txt"
     */

    @Test
    public void publish_with_own_pattern() throws IOException, InterruptedException {
        SMBContainer smb = docker.start(SMBContainer.class);
        Resource cpDir = resource("/ftp_plugin/");

        FreeStyleJob j = jenkins.jobs.create();
        configureCifs("asd", smb);
        j.configure();
        {
            j.copyDir(cpDir);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().patternSeparator.set("[;]+");
            fps.getDefaultTransfer().sourceFile.set("te,st.txt;odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(smb.tryCopyFile("/tmp/te,st.txt","/tmp"));
        assertTrue(smb.tryCopyFile("/tmp/odes.txt", "/tmp"));
        assertTrue(new File("/tmp/te,st.txt").exists());
        assertTrue(new File("/tmp/odes.txt").exists());
    }

    /**
     * @native(docker) Scenario: Configure a job with smbsmb publishing
     * Given I have installed the "smbsmb" plugin
     * And a docker fixture "smb"
     * And a job
     * When I configure docker fixture as smbsmb site
     * And I configure the job with one smbsmb Transfer Set
     * And I configure the Transfer Set
     * With Source Files ".svn,.git,odes.txt"  with smbsmb plugin
     * And I copy resources "odes.txt" as "odes.txt,.svn,.git" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And smbsmb plugin should have published "odes.txt"
     * And smbsmb plugin should have not published  ".svn,.git"
     */

    @Test
    public void publish_with_default_exclude() throws IOException, InterruptedException {
        SMBContainer smb = docker.start(SMBContainer.class);
        Resource cpTxt = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        configureCifs("asd", smb);
        j.configure();
        {
            j.copyResource(cpTxt, ".svn");
            j.copyResource(cpTxt, "CVS");
            j.copyResource(cpTxt);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set(".svn,CVS,odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(smb.tryCopyFile("/tmp/odes.txt", "/tmp/"));
        assertTrue(!smb.tryCopyFile("/tmp/.svn", "/tmp/"));
        assertTrue(!smb.tryCopyFile("/tmp/CVS", "/tmp/"));
    }

    /**
     * @native(docker) Scenario: Configure a job with smbsmb publishing
     * Given I have installed the "smbsmb" plugin
     * And a docker fixture "smb"
     * And a job
     * When I configure docker fixture as smbsmb site
     * And I configure the job with one smbsmb Transfer Set
     * And I configure the Transfer Set
     * With Source Files ".svn,.git,odes.txt"  with smbsmb plugin
     * And With No default excludes checked
     * And I copy resources "odes.txt" as "odes.txt,.svn,.git" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And smbsmb plugin should have published  .svn/,.git"
     */

    @Test
    public void publish_with_no_default_exclude() throws IOException, InterruptedException {
        SMBContainer smb = docker.start(SMBContainer.class);
        Resource cpTxt = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        configureCifs("asd", smb);
        j.configure();
        {
            j.copyResource(cpTxt, ".svn");
            j.copyResource(cpTxt, "CVS");
            j.copyResource(cpTxt);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set(".svn,CVS,odes.txt");
            fps.getDefaultTransfer().noDefaultExcludes.check();
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(smb.tryCopyFile("/tmp/odes.txt", "/tmp/"));
        assertTrue(smb.tryCopyFile("/tmp/.svn", "/tmp/"));
        assertTrue(smb.tryCopyFile("/tmp/CVS", "/tmp/"));
    }

    /**
     * @native(docker) Scenario: Configure a job with smbsmb publishing
     * Given I have installed the "smbsmb" plugin
     * And a docker fixture "smb"
     * And a job
     * When I configure docker fixture as smbsmb site
     * And I configure the job with one smbsmb Transfer Set
     * And I configure the Transfer Set
     * With Source Files "empty/,odes.txt" with smbsmb plugin
     * And With Make empty dirs  Checked
     * And I create the directory "empty/" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And smbsmb plugin should have published  "empty/" and "odes.txt"
     */

    @Test
    public void publish_with_empty_directory() throws IOException, InterruptedException {
        SMBContainer smb = docker.start(SMBContainer.class);
        Resource cpTxt = resource("/ftp_plugin/odes.txt");
        File tmpDir = createTempDirectory();
        File nestedEmptyDir = new File(tmpDir + "/empty");
        nestedEmptyDir.mkdir();

        FreeStyleJob j = jenkins.jobs.create();
        configureCifs("asd", smb);
        j.configure();
        {
            j.copyResource(cpTxt);
            j.copyFile(tmpDir);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("empty/,odes.txt");
            fps.getDefaultTransfer().makeEmptyDirs.check();
        }

        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(smb.tryCopyFile("/tmp/odes.txt", "/tmp/"));
        assertTrue(smb.tryCopyFile("/tmp/empty", "/tmp/"));
    }

    /**
     * @native(docker) Scenario: Configure a job with smbsmb publishing
     * Given I have installed the "smbsmb" plugin
     * And a docker fixture "smb"
     * And a job
     * When I configure docker fixture as smbsmb site
     * And I configure the job with one smbsmb Transfer Set
     * And I configure the Transfer Set
     * With Source Files "empty/,odes.txt" with smbsmb plugin
     * And I create the directory "empty/" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And smbsmb plugin should have not published "empty/" and "odes.txt"
     */

    @Test
    public void publish_without_empty_directory() throws IOException, InterruptedException {
        SMBContainer smb = docker.start(SMBContainer.class);
        Resource cpTxt = resource("/ftp_plugin/odes.txt");
        File tmpDir = createTempDirectory();
        File nestedEmptyDir = new File(tmpDir + "/empty");
        nestedEmptyDir.mkdir();

        FreeStyleJob j = jenkins.jobs.create();
        configureCifs("asd", smb);
        j.configure();
        {
            j.copyResource(cpTxt);
            j.copyFile(tmpDir);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("empty/,odes.txt");

        }

        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(smb.tryCopyFile("/tmp/odes.txt", "/tmp/"));
        assertTrue(!smb.tryCopyFile("/tmp/dockertmp/empty", "/tmp/"));
    }

    /**
     * @native(docker) Scenario: Configure a job with smbsmb publishing
     * Given I have installed the "smbsmb" plugin
     * And a docker fixture "smb"
     * And a job
     * When I configure docker fixture as smbsmb site
     * And I configure the job with one smbsmb Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles "odes.txt" and "flat\odes.txt" with smbsmb plugin
     * And I copy resources "ftp_plugin/"
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And smbsmb plugin should have published "odes.txt" and "flat/odes.txt"
     */

    @Test
    public void publish_without_flatten_files() throws IOException, InterruptedException {
        SMBContainer smb = docker.start(SMBContainer.class);
        Resource cpDir = resource("/ftp_plugin/");

        FreeStyleJob j = jenkins.jobs.create();
        configureCifs("asd", smb);
        j.configure();
        {
            j.copyDir(cpDir);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("flat/odes.txt,odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(smb.tryCopyFile("/tmp/flat/odes.txt", "/tmp/flat"));
        assertTrue(smb.tryCopyFile("/tmp/odes.txt", "/tmp/"));
    }

    /**
     * @native(docker) Scenario: Configure a job with smbsmb publishing
     * Given I have installed the "smbsmb" plugin
     * And a docker fixture "smb"
     * And a job
     * When I configure docker fixture as smbsmb site
     * And I configure the job with one smbsmb Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles "odes.txt" and "flat\odes.txt" with smbsmb plugin
     * And With Flatten files Checked
     * And I copy resources "ftp_plugin/"
     * And I save the job
     * And I build the job
     * Then the build should fail
     */

    @Test
    public void publish_with_flatten_files() throws IOException, InterruptedException {
        SMBContainer smb = docker.start(SMBContainer.class);
        Resource cpDir = resource("/ftp_plugin/");

        FreeStyleJob j = jenkins.jobs.create();
        configureCifs("asd", smb);
        j.configure();
        {
            j.copyDir(cpDir);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().flatten.check();
            fps.getDefaultTransfer().sourceFile.set("flat/odes.txt,odes.txt");
        }
        j.save();
        j.startBuild().shouldUnstable();
    }

    /**
     * @native(docker) Scenario: Configure a job with smbsmb publishing
     * Given I have installed the "smbsmb" plugin
     * And a docker fixture "smb"
     * And a job
     * When I configure docker fixture as smbsmb site
     * And I configure the job with one smbsmb Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles "odes.txt" with smbsmb plugin
     * And With Remote directory  is a date format Checked "yyyyMMddHH"
     * And I copy resources "odes.txt" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And smbsmb plugin should have published "odes.txt" under 'yyyyMMddHH'
     */

    @Test
    public void publish_with_date_format() throws IOException, InterruptedException {
        SMBContainer smb = docker.start(SMBContainer.class);
        Resource cpTxt = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        configureCifs("asd", smb);
        j.configure();
        {
            j.copyResource(cpTxt);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().remoteDirectorySDF.check();
            fps.getDefaultTransfer().remoteDirectory.set("yyyyMMddHH");
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
        Date date = new Date();
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(smb.tryCopyFile("/tmp/" + dateFormat.format(date) + "/odes.txt","/tmp/"));
        assertTrue(new File("/tmp/odes.txt").exists());
    }

    /**
     * @native(docker) Scenario: Configure a job with smbsmb publishing
     * Given I have installed the "smbsmb" plugin
     * And a docker fixture "smb"
     * And I Upload "oldfile.txt" to the smbsmb Server
     * And a job
     * And I configure the job with one smbsmb Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles "myresources" with smbsmb plugin
     * And With Clean remote
     * And I copy resources "myresources" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And smbsmb plugin should have published "myresources"
     * And the RemoteDIR should not contain "old.txt"
     */

    @Test
    public void publish_with_clean_remote() throws IOException, InterruptedException {
        SMBContainer smb = docker.start(SMBContainer.class);
        Resource cpTxt = resource("/ftp_plugin/odes.txt");
        FreeStyleJob j = jenkins.jobs.create();
        configureCifs("asd", smb);
        j.configure();
        {
            j.copyResource(cpTxt);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().cleanRemote.check();
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(smb.tryCopyFile("/tmp/odes.txt","/tmp/"));
        assertTrue(!smb.tryCopyFile("/tmp/old.txt","/tmp/"));
    }

    /**
     * @native(docker) Scenario: Configure a job with smbsmb publishing
     * Given I have installed the "smbsmb" plugin
     * And a docker fixture "smb"
     * And a job
     * When I configure docker fixture as smbsmb site
     * And I configure the job with three smbsmb Transfer Sets
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
     * And smbsmb plugin should have published odes.txt,odes2.txt,odes3.txt on docker fixture
     */
    @Test
    public void publish_multiple_sets() throws IOException, InterruptedException {
        SMBContainer smb = docker.start(SMBContainer.class);
        Resource cpTxt = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        configureCifs("asd", smb);
        j.configure();
        {
            j.copyResource(cpTxt);
            j.copyResource(cpTxt, "odes2.txt");
            j.copyResource(cpTxt, "odes3.txt");
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
            fps.addTransferSet().sourceFile.set("odes2.txt");
            fps.addTransferSet().sourceFile.set("odes3.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(smb.tryCopyFile("/tmp/odes.txt","/tmp/"));
        assertTrue(smb.tryCopyFile("/tmp/odes.txt2","/tmp/"));
        assertTrue(smb.tryCopyFile("/tmp/odes.txt3","/tmp/"));
    }

    /**
     * @native(docker) Scenario: Configure a job with smbsmb publishing
     * Given I have installed the "smbsmb" plugin
     * And 2 docker fixtures "smb"
     * And a job
     * When I configure docker docker1 as smbsmb site
     * And I configure docker  docker2 as smbsmb site
     * And I configure the job with docker1 Server
     * And one smbsmb Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles odes.txt
     * And I Add Server docker2
     * And one smbsmb Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles odes.txt
     * And I copy resources odes.txt  into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And smbsmb plugin should have published my set of files at all docker fixtures
     */
    @Test
    public void publish_multiple_servers() throws IOException, InterruptedException {
        SMBContainer smb = docker.start(SMBContainer.class);
        SMBContainer smb2 = docker.start(SMBContainer.class);
        Resource cpTxt = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        configureCifs("docker1", smb);
        configureCifs("docker2", smb2);
        j.configure();
        {
            j.copyResource(cpTxt);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.configName.select("docker1");
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
            CifsPublisher.Site fps2 = fp.addServer();
            fps2.configName.select("docker2");
            fps2.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(smb.tryCopyFile("/tmp/odes.txt","/tmp/dockertmp"));
        assertTrue(smb.tryCopyFile("/tmp/odes.txt","/tmp/dockertmp2"));

    }

    /**
     * @native(docker) Scenario: Configure a job with smbsmb publishing
     * Given I have installed the "smbsmb" plugin
     * And a docker fixture "smb"
     * And a ssh slave
     * And a job
     * When I configure docker fixture as smbsmb host
     * And I configure the job with one smbsmb Transfer Set
     * And I configure the Transfer Set
     * With Source Files "odes.txt"
     * And With Remote Directory myfolder/
     * And I copy resource "odes.txt" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And smbsmb plugin should have published "odes.txt" on docker fixture
     */

    @Test
    public void publish_slave_resourses() throws IOException, InterruptedException,ExecutionException {
        SMBContainer smb = docker.start(SMBContainer.class);
        Resource cpFile = resource("/ftp_plugin/odes.txt");

        Slave s = slaves.get().install(jenkins).get();
        s.configure();
        s.save();

        FreeStyleJob j = jenkins.jobs.create();
        configureCifs("asd", smb);
        j.configure();
        {
            j.copyResource(cpFile);
            CifsPublisher fp = j.addPublisher(CifsPublisher.class);
            CifsPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertTrue(smb.tryCopyFile("/tmp/odes.txt","/tmp"));
        assertThat(FileUtils.readFileToString(new File("/tmp/odes.txt")), CoreMatchers.is(cpFile.asText()));
    }
}
