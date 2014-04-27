package plugins;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.fixtures.FtpdContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.ftp.FtpGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.ftp.FtpGlobalConfig.Site;
import org.jenkinsci.test.acceptance.plugins.ftp.FtpPublisher;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import com.google.inject.Inject;

/**
 Feature: Tests for SCP plugin
 @author Tobias Meyer
 */
@WithPlugins("scp")
public class FtpPublishPluginTest extends AbstractJUnitTest {
    @Inject
    Docker docker;

    /**
     @native(docker)
    Scenario: Configure a job with FTP publishing
    Given I have installed the "ftp" plugin
    And a docker fixture "ftpd"
    And a job
    When I configure docker fixture as FTP host
    And I configure the job with one FTP Transfer Set
    And I configure the Transfer Set
        With Source Files "odes.txt"
        And With Remote Directory myfolder/
    And I copy resource "odes.txt" into workspace
    And I save the job
    And I build the job
    Then the build should succeed
    And FTP plugin should have published "odes.txt" on docker fixture
     */
    @Native("docker")
    @Test
    public void publish_resources() throws IOException, InterruptedException {
        FtpdContainer ftpd = docker.start(FtpdContainer.class);
        Resource cp_file = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();

        jenkins.configure();
        Site s = new FtpGlobalConfig(jenkins).addSite();
        {
            s.name.set(ftpd.ipBound(21));
            s.hostname.set(ftpd.ipBound(21));
            s.port.set(ftpd.port(21));
            s.username.set("test");
            s.password.set("test");
            s.remoteDir.set("/tmp");
            s.disableRemoteVerification.check(true);
        }
        jenkins.save();
        j.configure();
        {
            j.copyResource(cp_file);
            FtpPublisher fp = j.addPublisher(FtpPublisher.class);
            FtpPublisher.Site fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();

        //Publish Over ftp fails, cause we are using pasive mode compare with: http://www.coderanch.com/t/207085/sockets/java/FTP-connection-Proxy
        j.startBuild().shouldSucceed();
        ftpd.cp("/tmp/odes.txt", new File("/tmp"));
        assertThat(FileUtils.readFileToString(new File("/tmp/odes.txt")), CoreMatchers.is(cp_file.asText()));
    }

    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job with one FTP Transfer Set
     And I configure the Transfer Set
         With Source Files "prefix_myresources"
         And With Remote Directory myfolder/
         And With remove prefix
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published "myresources" on docker fixture
     */
    @Test

    public void publish_resources_and_remove_prefix() throws IOException, InterruptedException {


    }
    //advanced
    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a resources Directory "myresources"
        With the file ".exclude"
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job with one FTP Transfer Set
     And I configure the Transfer Set
        With Source Files  "myresources/"
        And With exclude ".exclude"
     And I copy resources "myresources/" into workspace
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published "myresources" without the ".exclude" on docker fixture
     */
    @Test

    public void publish_resources_with_excludes() throws IOException, InterruptedException {


    }

    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job with one FTP Transfer Set
     And I configure the Transfer Set
        With Source Files "myresource1,myresource2"
     And I copy resources "myresource1,myresource2"  into workspace
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published "myresources1" and "myresource2"
     */
    @Test
    public void publish_with_default_pattern() throws IOException, InterruptedException {


    }
    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job with one FTP Transfer Set
     And I configure the Transfer Set
        With Source Files "my,resource;myresource2"
        And With Pattern separator [;]+
     And I copy resources "my,resource,myresource2"  into workspace
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published "my,resource" and "myresource2"
     */
    @Test
    public void publish_with_own_pattern() throws IOException, InterruptedException {


    }
    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job with one FTP Transfer Set
     And I configure the Transfer Set
         With Source Files ".svn/,.git"  with FTP plugin
     And I copy resources ".svn/,.git" into workspace
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have not published  .svn/,.git"
     */
    @Test
    public void publish_with_default_exclude() throws IOException, InterruptedException {


    }
    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job with one FTP Transfer Set
     And I configure the Transfer Set
        With Source Files ".svn/,.git"  with FTP plugin
        And With No default excludes checked
     And I copy resources ".svn/,.git" into workspace
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published  .svn/,.git"
     */
    @Test
    public void publish_with_no_default_exclude() throws IOException, InterruptedException {


    }
    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job with one FTP Transfer Set
     And I configure the Transfer Set
        With Source Files "emptydir" with FTP plugin
        And With Make empty dirs  Checked
     And I create the directory "emptydir" into workspace
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published  "emptydir"
     */
    @Test
    public void publish_with_empty_directory() throws IOException, InterruptedException {


    }
    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job with one FTP Transfer Set
     And I configure the Transfer Set
        With Source Files "emptydir" with FTP plugin
     And I create the directory "emptydir" into workspace
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have not published  "emptydir"
     */
    @Test
    public void publish_without_empty_directory() throws IOException, InterruptedException {


    }
    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job with one FTP Transfer Set
     And I configure the Transfer Set
        With SourceFiles "myresources" and "subdir\myresources" with FTP plugin
     And I copy resources "myresources" into workspace
     And I copy resources "myresources" into workspace\subdir
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published "myresources" and "subdir\myresources"
     */
    @Test
    public void publish_without_flatten_files() throws IOException, InterruptedException {


    }

    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job with one FTP Transfer Set
     And I configure the Transfer Set
        With SourceFiles "myresources" and "subdir\myresources" with FTP plugin
        And With Flatten files Checked
     And I copy resources "myresources" into workspace
     And I copy resources "myresources" into workspace\subdir
     And I save the job
     And I build the job
     Then the build should fail
     */
    @Test
    public void publish_with_flatten_files() throws IOException, InterruptedException {


    }

    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job with one FTP Transfer Set
     And I configure the Transfer Set
         With SourceFiles "myresources" with FTP plugin
         And With Remote directory  is a date format Checked
     And I copy resources "myresources" into workspace
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published "myresources" under 'yyyy/MM/dd/'build-${BUILD_NUMBER}'
     */
    @Test
    public void publish_with_date_format() throws IOException, InterruptedException {


    }

    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
        And I Upload "oldfile.txt" to the ftp Server
     And a job
     And I configure the job with one FTP Transfer Set
     And I configure the Transfer Set
        With SourceFiles "myresources" with FTP plugin
        And With Clean remote
     And I copy resources "myresources" into workspace
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published "myresources"
     And the RemoteDIR should not contain "old.txt"
     */
    @Test
    public void publish_with_clean_remote() throws IOException, InterruptedException {


    }

    //This test will probably only make sense if we use *nix and a windows system for example!
    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job with one FTP Transfer Set
     And I configure the Transfer Set
         With SourceFiles "mac-ascii.txt,windows-ascii.txt,linux-ascii.txt" with FTP plugin
         And With ASCII mode
     And I copy resources "mac-ascii.txt,windows-ascii.txt,linux-ascii.txt" into workspace
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published "mac-ascii.txt,windows-ascii.txt,linux-ascii.txt" with correct lining
     */
    @Test
    public void publish_asci_text() throws IOException, InterruptedException {


    }

    //more transfer set
    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job with three FTP Transfer Sets
     And I configure the Transfer Set 1
        With SourceFiles  myresourcesSet1
     And I configure the Transfer Set 2
        With SourceFiles  myresourcesSet2
     And I configure the Transfer Set 3
        With SourceFiles  myresourcesSet3
     And I copy resources myresources into workspace
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published myresources on docker fixture
     */
    @Test

    public void publish_multiple_sets() throws IOException, InterruptedException {


    }

//Multiple Server
    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And 2 docker fixtures "ftpd"
     And a job
     When I configure docker fixture 1 as FTP site
     And I configure docker fixture 2 as FTP site
     And I configure the job with fixture1 Server
     And one FTP Transfer Set
     And I configure the Transfer Set
        With SourceFiles myresources
     And I Add Server fixture2
     And one FTP Transfer Set
         And I configure the Transfer Set
         With SourceFiles myresources
     And I copy resources myresources  into workspace
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published my set of files at all docker fixtures
     */
    @Test

    public void publish_multiple_servers() throws IOException, InterruptedException {


    }
}
