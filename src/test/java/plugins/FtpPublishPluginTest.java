package plugins;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.fixtures.SshdContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.scp.ScpGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.scp.ScpGlobalConfig.Site;
import org.jenkinsci.test.acceptance.plugins.scp.ScpPublisher;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

import com.google.inject.Inject;

/**
 * Feature: Tests for SCP plugin
 *
 * @author Tobias Meyer
 */
@WithPlugins("scp")
public class FtpPublishPluginTest extends AbstractJUnitTest {
    @Inject
    Docker docker;

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing Given I have installed the "ftp" plugin And a
     * docker fixture "ftpd" And a job When I configure docker fixture as FTP site And I configure the job with remote
     * directory myfolder/ And I copy resource "myresource" into workspace And I publish "myresource" with FTP plugin
     * And I save the job And I build the job Then the build should succeed And FTP plugin should have published
     * "pmd.xml" on docker fixture
     */
    @Test

    public void configure_job_with_ftp_publishing() throws IOException, InterruptedException {


    }

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing Given I have installed the "ftp" plugin And a
     * docker fixture "ftpd" And a job When I configure docker fixture as FTP site And I configure the job And I
     * configure ftp publish with remove prefix And I copy resource "mytestfile" into workspace/prefix And I publish
     * "prefix/mytestfile" with FTP plugin And I save the job And I build the job Then the build should succeed And FTP
     * plugin should have published "pmd.xml" on docker fixture
     */
    @Test

    public void configure_job_with_remove_prefix_ftp_publishing() throws IOException, InterruptedException {


    }
    //advanced

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing Given I have installed the "ftp" plugin And a
     * docker fixture "ftpd" And a job When I configure docker fixture as FTP site And I configure the job And I copy
     * resources ...  into workspace And I publish a set of files but exclude ... And I save the job And I build the job
     * Then the build should succeed And FTP plugin should have published my set of files without the excludes on docker
     * fixture
     */
    @Test

    public void configure_job_with_exclude_ftp_publishing() throws IOException, InterruptedException {


    }
    //more transfer set

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing Given I have installed the "ftp" plugin And a
     * docker fixture "ftpd" And a job When I configure docker fixture as FTP site And I configure the job And I copy
     * resources ...  into workspace And I publish a set of files as transfer set 1 And I publish a set of files as
     * transfer set 2 And I publish a set of files as transfer set 3 And I save the job And I build the job Then the
     * build should succeed And FTP plugin should have published my set of files  on docker fixture
     */
    @Test

    public void configure_job_with_multiple_transfer_set_ftp_publishing() throws IOException, InterruptedException {


    }

//Multiple Server

    /**
     * @native(docker) Scenario: Configure a job with FTP publishing Given I have installed the "ftp" plugin And a
     * docker fixture "ftpd" And a job When I configure docker fixture as FTP site And I configure the job And I copy
     * resources ...  into workspace And I publish a set of files at server 1 And I publish a set of files at server 2
     * And I save the job And I build the job Then the build should succeed And FTP plugin should have published my set
     * of files at both docker fixtures
     */
    @Test

    public void configure_job_with_multiple_server_ftp_publishing() throws IOException, InterruptedException {


    }
}
