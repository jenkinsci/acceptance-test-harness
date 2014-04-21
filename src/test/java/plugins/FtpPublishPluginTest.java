package plugins;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
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
       And I configure the job with remote directory myfolder/
       And I copy resource "myresources" into workspace
       And I publish "myresources" with FTP plugin
       And I save the job
       And I build the job
       Then the build should succeed
       And FTP plugin should have published "myresources" on docker fixture
     */
    @Native("docker")
    @Test
    public void configure_job_with_ftp_publishing() throws IOException, InterruptedException {


    }

    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job
     And I copy resource "myresources" into workspace with  prefix
     And I publish "prefix/myresources"" with FTP plugin
     And I configure FTP publish plugin with remove prefix
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published "myresources" on docker fixture
     */
    @Test

    public void configure_job_with_remove_prefix_ftp_publishing() throws IOException, InterruptedException {


    }
    //advanced
    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job
     And I copy resources "myresources" and ".exclude"  into workspace
     And I publish"myresources" and ".exclude"  FTP plugin
     And I configure FTP publish plugin with exclude ".exclude"
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published "myresources" without the ".exclude" on docker fixture
     */
    @Test

    public void configure_job_with_exclude_ftp_publishing() throws IOException, InterruptedException {


    }

    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job
     And I copy resources "myresources"  into workspace
     And I publish "myresources" with FTP plugin
     And I configure FTP publish plugin with ';' as pattern separator
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published "myresources" without the ".exclude" on docker fixture
     */
    @Test
    public void configure_job_with_different_pattern_ftp_publishing() throws IOException, InterruptedException {


    }

    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job
     And I copy resources ".svn/,.git" into workspace
     And I publish ".svn/,.git"  with FTP plugin
     And I configure FTP publish plugin with No default excludes
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published  .svn/,.git"
     */
    @Test
    public void configure_job_no_default_exclude_ftp_publishing() throws IOException, InterruptedException {


    }
    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job
     And I create the directory "emptydir" into workspace
     And I publish "emptydir" with FTP plugin
     And I configure FTP publish plugin with Make empty dirs
     And I save the job
     And I build the job
     Then the build should fail
     */
    @Test
    public void configure_job_emtpy_dirs_ftp_publishing() throws IOException, InterruptedException {


    }

    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job
     And I copy resources "myresources" into workspace
     And I copy resources "myresources" into workspace\subdir
     And I publish "myresources" and "subdir\myresources" with FTP plugin
     And I configure FTP publish plugin with Flatten files
     And I save the job
     And I build the job
     Then the build should fail
     */
    @Test
    public void configure_job_flatten_files_ftp_publishing() throws IOException, InterruptedException {


    }

    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job
     And I copy resources "myresources" into workspace
     And I publish "myresources" with FTP plugin
     And I configure FTP publish plugin with Remote directory  is a date format
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published "myresources" under 'yyyy/MM/dd/'build-${BUILD_NUMBER}'
     */
    @Test
    public void configure_job_date_format_ftp_publishing() throws IOException, InterruptedException {


    }

    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I upload "oldfile.txt" into workspace
     And I configure the job
     And I copy resources "myresources" into workspace
     And I publish "myresources" with FTP plugin
     And I configure FTP publish plugin with Clean remote
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published "myresources"
     And the RemoteDIR should not contain "old.txt"
     */
    @Test
    public void configure_job_clean_remote_ftp_publishing() throws IOException, InterruptedException {


    }

    //This test will probably only make sense if we use *nix and a windows system for example!
    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job
     And I copy resources "ascitext" into workspace
     And I publish "ascitext" with FTP plugin
     And I configure FTP publish plugin with ASCII mode
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published "ascitext" with correct lining
     */
    @Test
    public void configure_job_asci_text_ftp_publishing() throws IOException, InterruptedException {


    }

    //more transfer set
    /**
     @native(docker)
     Scenario: Configure a job with FTP publishing
     Given I have installed the "ftp" plugin
     And a docker fixture "ftpd"
     And a job
     When I configure docker fixture as FTP site
     And I configure the job
     And I copy resources myresources into workspace
     And I publish myresourcesSet1 with FTP plugin
     And I add a Transferset
     And I publish myresourcesSet2 with FTP plugin
     And I add a Transferset
     And I publish myresourcesSet3 a with FTP plugin
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published myresources on docker fixture
     */
    @Test

    public void configure_job_with_multiple_transfer_set_ftp_publishing() throws IOException, InterruptedException {


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
     And I configure the job
     And I copy resources myresources  into workspace
     And I publish myresources  with FTP plugin
     And I Add Server
     And I publish myresources  with FTP plugin
     And I save the job
     And I build the job
     Then the build should succeed
     And FTP plugin should have published my set of files at all docker fixtures
     */
    @Test

    public void configure_job_with_multiple_server_ftp_publishing() throws IOException, InterruptedException {


    }
}
