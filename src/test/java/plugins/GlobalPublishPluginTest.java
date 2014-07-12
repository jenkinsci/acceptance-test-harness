package plugins;

import com.google.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.plugins.publish_over.PublishGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.publish_over.PublishGlobalPublisher;
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Abstract class for the publisher class.
 * This class implements base test which are used by all PublishOver* Classes.
 * <p/>
 * For an concrete implementation of the test, the abstract functions need to be implemented.
 *
 * @author Tobias Meyer
 */
@Native("docker")
public abstract class GlobalPublishPluginTest extends AbstractJUnitTest {
    @Inject
    Docker docker;
    @Inject
    SlaveProvider slaves;

    /**
     * Helper method to create a temporary empty directory
     *
     * @return File Descriptor for empty Directory
     * @throws java.io.IOException
     */
    protected static File createTempDirectory()
            throws IOException {
        File temp;
        temp = java.nio.file.Files.createTempDirectory("temp").toFile();
        temp.deleteOnExit();
        return (temp);
    }

    /**
     * Creates & Returns the instance of the corresponding Docker Container for this publisher.
     *
     * @return a DockerContainer
     */
    protected abstract DockerContainer createPublisherContainer();

    /**
     * Creates the global Config for the test.
     *
     * @return concrete instance of the config for the test
     */
    protected abstract PublishGlobalConfig.GlobalSite createGlobalConfig();

    /**
     * Creates the Publisher config for the test
     *
     * @return concrete instance of the config for the test
     */
    protected abstract PublishGlobalPublisher addGlobalPublisher(FreeStyleJob j);

    /**
     * Helper method to configure Jenkins.
     * It adds the DockerContainer with publisher Server with the name
     *
     * @param serverName Name to Access Instance
     * @param dock       Docker Instance of the Server
     */
    protected abstract void configurePublisher(String serverName, DockerContainer dock);

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And a docker fixture "dock"
     * And a job
     * When I configure docker fixture as publisher host
     * And I configure the job with one publisher Transfer Set
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
        DockerContainer dock = createPublisherContainer();
        Resource cpFile = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        configurePublisher("asd", dock);
        j.configure();
        {
            j.copyResource(cpFile);
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();

        j.startBuild().shouldSucceed();

        assertThat(dock.tryCopyFile("/tmp/odes.txt", "/tmp/"), is(true));
        assertThat(FileUtils.readFileToString(new File("/tmp/odes.txt")), CoreMatchers.is(cpFile.asText()));
    }

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And a docker fixture "dock"
     * And a job
     * When I configure docker fixture as publisher host
     * And I configure the job with one publisher Transfer Set
     * And I configure the Transfer Set
     * With Source Files "odes.txt"
     * And With Remote Directory "/tmp/${JOB_NAME}"
     * And I copy resource "odes.txt" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And publisher plugin should have published "odes.txt" on docker fixture
     */

    @Test
    public void publish_jenkins_variables() throws IOException, InterruptedException {
        DockerContainer dock = createPublisherContainer();
        Resource cpFile = resource("/ftp_plugin/odes.txt");
        String randomName = jenkins.jobs.createRandomName();
        String randomPath = "/tmp/" + randomName + "/";
        FreeStyleJob j = jenkins.jobs.create(FreeStyleJob.class, randomName);

        configurePublisher("asd", dock);
        j.configure();
        {
            j.copyResource(cpFile);
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.getDefaultTransfer().remoteDirectory.set("${JOB_NAME}/");
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();

        j.startBuild().shouldSucceed();
        assertThat(dock.tryCopyFile(randomPath + "odes.txt", "/tmp/"), is(true));
        assertThat(FileUtils.readFileToString(new File("/tmp/odes.txt")), CoreMatchers.is(cpFile.asText()));
    }

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And a docker fixture "dock"
     * And a job
     * When I configure docker fixture as publisher site
     * And I configure the job with one publisher Transfer Set
     * And I configure the Transfer Set
     * With Source Files "prefix_/test.txt"
     * And With Remote Directory /tmp/
     * And With remove prefix
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And publisher plugin should have published "test.txt" on docker fixture
     */

    @Test
    public void publish_resources_and_remove_prefix() throws IOException, InterruptedException {
        DockerContainer dock = createPublisherContainer();
        Resource cpDir = resource("/ftp_plugin/");
        Resource test = resource("/ftp_plugin/prefix_/test.txt");
        FreeStyleJob j = jenkins.jobs.create();
        configurePublisher("asd", dock);
        j.configure();
        {
            j.copyDir(cpDir);
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("prefix_/test.txt");
            fps.getDefaultTransfer().removePrefix.set("prefix_");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertThat(dock.tryCopyFile("/tmp/test.txt", "/tmp"), is(true));
        assertThat(FileUtils.readFileToString(new File("/tmp/test.txt")), CoreMatchers.is(test.asText()));
    }

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And a resources Directory "prefix_/"
     * With the file ".exclude"
     * And a docker fixture "dock"
     * And a job
     * When I configure docker fixture as publisher site
     * And I configure the job with one publisher Transfer Set
     * And I configure the Transfer Set
     * With Source Files  "prefix_/"
     * And With exclude ".exclude"
     * And I copy resources "prefix_/" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And publisher plugin should have published "prefix_/" without the ".exclude" on docker fixture
     */

    @Test
    public void publish_resources_with_excludes() throws IOException, InterruptedException {
        DockerContainer dock = createPublisherContainer();
        Resource cpDir = resource("/ftp_plugin/");

        FreeStyleJob j = jenkins.jobs.create();
        configurePublisher("asd", dock);
        j.configure();
        {
            j.copyDir(cpDir);
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("prefix_/");
            fps.getDefaultTransfer().excludes.set("**/*.exclude");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertThat(dock.tryCopyFile("/tmp/prefix_/", "/tmp"), is(true));
        assertThat(!dock.tryCopyFile("/tmp/prefix_/.exclude", "/tmp"), is(true));
    }

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And a docker fixture "dock"
     * And a job
     * When I configure docker fixture as publisher site
     * And I configure the job with one publisher Transfer Set
     * And I configure the Transfer Set
     * With Source Files "prefix_/test.txt,odes.txt"
     * And I copy resources "prefix_/test.txt,odes.txt"  into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And publisher plugin should have published "prefix_/test.txt" and "odes.txt"
     */

    @Test
    public void publish_with_default_pattern() throws IOException, InterruptedException {
        DockerContainer dock = createPublisherContainer();
        Resource cpDir = resource("/ftp_plugin/");

        FreeStyleJob j = jenkins.jobs.create();
        configurePublisher("asd", dock);
        j.configure();
        {
            j.copyDir(cpDir);
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("prefix_/test.txt,odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertThat(dock.tryCopyFile("/tmp/prefix_/test.txt", "/tmp"), is(true));
        assertThat(dock.tryCopyFile("/tmp/odes.txt", "/tmp"), is(true));
        assertThat(new File("/tmp/test.txt").exists(), is(true));
        assertThat(new File("/tmp/odes.txt").exists(), is(true));
    }

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And a docker fixture "dock"
     * And a job
     * When I configure docker fixture as publisher site
     * And I configure the job with one publisher Transfer Set
     * And I configure the Transfer Set
     * With Source Files "te,st.txt;odes.txt"
     * And With Pattern separator [;]+
     * And I copy resources "te,st.txt,odes.txt"  into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And publisher plugin should have published "te,st.txt" and "odes.txt"
     */

    @Test
    public void publish_with_own_pattern() throws IOException, InterruptedException {
        DockerContainer dock = createPublisherContainer();
        Resource cpDir = resource("/ftp_plugin/");

        FreeStyleJob j = jenkins.jobs.create();
        configurePublisher("asd", dock);
        j.configure();
        {
            j.copyDir(cpDir);
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.getDefaultTransfer().patternSeparator.set("[;]+");
            fps.getDefaultTransfer().sourceFile.set("te,st.txt;odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertThat(dock.tryCopyFile("/tmp/te,st.txt", "/tmp"), is(true));
        assertThat(dock.tryCopyFile("/tmp/odes.txt", "/tmp"), is(true));
        assertThat(new File("/tmp/te,st.txt").exists(), is(true));
        assertThat(new File("/tmp/odes.txt").exists(), is(true));
    }

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And a docker fixture "dock"
     * And a job
     * When I configure docker fixture as publisher site
     * And I configure the job with one publisher Transfer Set
     * And I configure the Transfer Set
     * With Source Files ".svn,.git,odes.txt"  with publisher plugin
     * And I copy resources "odes.txt" as "odes.txt,.svn,.git" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And publisher plugin should have published "odes.txt"
     * And publisher plugin should have not published  ".svn,.git"
     */

    @Test
    public void publish_with_default_exclude() throws IOException, InterruptedException {
        DockerContainer dock = createPublisherContainer();
        Resource cpTxt = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        configurePublisher("asd", dock);
        j.configure();
        {
            j.copyResource(cpTxt, ".svn");
            j.copyResource(cpTxt, "CVS");
            j.copyResource(cpTxt);
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set(".svn,CVS,odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertThat(dock.tryCopyFile("/tmp/odes.txt", "/tmp/"), is(true));
        assertThat(!dock.tryCopyFile("/tmp/.svn", "/tmp/"), is(true));
        assertThat(!dock.tryCopyFile("/tmp/CVS", "/tmp/"), is(true));
    }

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And a docker fixture "dock"
     * And a job
     * When I configure docker fixture as publisher site
     * And I configure the job with one publisher Transfer Set
     * And I configure the Transfer Set
     * With Source Files ".svn,.git,odes.txt"  with publisher plugin
     * And With No default excludes checked
     * And I copy resources "odes.txt" as "odes.txt,.svn,.git" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And publisher plugin should have published  .svn/,.git"
     */

    @Test
    public void publish_with_no_default_exclude() throws IOException, InterruptedException {
        DockerContainer dock = createPublisherContainer();
        Resource cpTxt = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        configurePublisher("asd", dock);
        j.configure();
        {
            j.copyResource(cpTxt, ".svn");
            j.copyResource(cpTxt, "CVS");
            j.copyResource(cpTxt);
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set(".svn,CVS,odes.txt");
            fps.getDefaultTransfer().noDefaultExcludes.check();
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertThat(dock.tryCopyFile("/tmp/odes.txt", "/tmp/"), is(true));
        assertThat(dock.tryCopyFile("/tmp/.svn", "/tmp/"), is(true));
        assertThat(dock.tryCopyFile("/tmp/CVS", "/tmp/"), is(true));
    }

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And a docker fixture "dock"
     * And a job
     * When I configure docker fixture as publisher site
     * And I configure the job with one publisher Transfer Set
     * And I configure the Transfer Set
     * With Source Files "empty/,odes.txt" with publisher plugin
     * And With Make empty dirs  Checked
     * And I create the directory "empty/" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And publisher plugin should have published  "empty/" and "odes.txt"
     */

    @Test
    public void publish_with_empty_directory() throws IOException, InterruptedException {
        DockerContainer dock = createPublisherContainer();
        Resource cpTxt = resource("/ftp_plugin/odes.txt");
        File tmpDir = createTempDirectory();
        File nestedEmptyDir = new File(tmpDir + "/empty");
        nestedEmptyDir.mkdir();

        FreeStyleJob j = jenkins.jobs.create();
        configurePublisher("asd", dock);
        j.configure();
        {
            j.copyResource(cpTxt);
            j.copyFile(tmpDir);
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("empty/,odes.txt");
            fps.getDefaultTransfer().makeEmptyDirs.check();
        }

        j.save();
        j.startBuild().shouldSucceed();
        assertThat(dock.tryCopyFile("/tmp/odes.txt", "/tmp/"), is(true));
        assertThat(dock.tryCopyFile("/tmp/empty", "/tmp/"), is(true));
    }

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And a docker fixture "dock"
     * And a job
     * When I configure docker fixture as publisher site
     * And I configure the job with one publisher Transfer Set
     * And I configure the Transfer Set
     * With Source Files "empty/,odes.txt" with publisher plugin
     * And I create the directory "empty/" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And publisher plugin should have not published "empty/" and "odes.txt"
     */

    @Test
    public void publish_without_empty_directory() throws IOException, InterruptedException {
        DockerContainer dock = createPublisherContainer();
        Resource cpTxt = resource("/ftp_plugin/odes.txt");
        File tmpDir = createTempDirectory();
        File nestedEmptyDir = new File(tmpDir + "/empty");
        nestedEmptyDir.mkdir();

        FreeStyleJob j = jenkins.jobs.create();
        configurePublisher("asd", dock);
        j.configure();
        {
            j.copyResource(cpTxt);
            j.copyFile(tmpDir);
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("empty/,odes.txt");

        }

        j.save();
        j.startBuild().shouldSucceed();
        assertThat(dock.tryCopyFile("/tmp/odes.txt", "/tmp/"), is(true));
        assertThat(!dock.tryCopyFile("/tmp/dockertmp/empty", "/tmp/"), is(true));
    }

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And a docker fixture "dock"
     * And a job
     * When I configure docker fixture as publisher site
     * And I configure the job with one publisher Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles "odes.txt" and "flat\odes.txt" with publisher plugin
     * And I copy resources "ftp_plugin/"
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And publisher plugin should have published "odes.txt" and "flat/odes.txt"
     */

    @Test
    public void publish_without_flatten_files() throws IOException, InterruptedException {
        DockerContainer dock = createPublisherContainer();
        Resource cpDir = resource("/ftp_plugin/");

        FreeStyleJob j = jenkins.jobs.create();
        configurePublisher("asd", dock);
        j.configure();
        {
            j.copyDir(cpDir);
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("flat/odes.txt,odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertThat(dock.tryCopyFile("/tmp/flat/odes.txt", "/tmp/flat"), is(true));
        assertThat(dock.tryCopyFile("/tmp/odes.txt", "/tmp/"), is(true));
    }

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And a docker fixture "dock"
     * And a job
     * When I configure docker fixture as publisher site
     * And I configure the job with one publisher Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles "odes.txt" and "flat\odes.txt" with publisher plugin
     * And With Flatten files Checked
     * And I copy resources "ftp_plugin/"
     * And I save the job
     * And I build the job
     * Then the build should fail
     */

    @Test
    public void publish_with_flatten_files() throws IOException, InterruptedException {
        DockerContainer dock = createPublisherContainer();
        Resource cpDir = resource("/ftp_plugin/");

        FreeStyleJob j = jenkins.jobs.create();
        configurePublisher("asd", dock);
        j.configure();
        {
            j.copyDir(cpDir);
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.getDefaultTransfer().flatten.check();
            fps.getDefaultTransfer().sourceFile.set("flat/odes.txt,odes.txt");
        }
        j.save();
        j.startBuild().shouldBeUnstable();
    }

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And a docker fixture "dock"
     * And a job
     * When I configure docker fixture as publisher site
     * And I configure the job with one publisher Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles "odes.txt" with publisher plugin
     * And With Remote directory  is a date format Checked "yyyyMMddHH"
     * And I copy resources "odes.txt" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And publisher plugin should have published "odes.txt" under 'yyyyMMddHH'
     */

    @Test
    public void publish_with_date_format() throws IOException, InterruptedException {
        DockerContainer dock = createPublisherContainer();
        Resource cpTxt = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        configurePublisher("asd", dock);
        j.configure();
        {
            j.copyResource(cpTxt);
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.getDefaultTransfer().remoteDirectorySDF.check();
            fps.getDefaultTransfer().remoteDirectory.set("yyyyMMddHH");
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
        Date date = new Date();
        j.save();
        j.startBuild().shouldSucceed();
        assertThat(dock.tryCopyFile("/tmp/" + dateFormat.format(date) + "/odes.txt", "/tmp/"), is(true));
        assertThat(new File("/tmp/odes.txt").exists(), is(true));
    }

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And a docker fixture "dock"
     * And I Upload "oldfile.txt" to the publisher Server
     * And a job
     * And I configure the job with one publisher Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles "myresources" with publisher plugin
     * And With Clean remote
     * And I copy resources "myresources" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And publisher plugin should have published "myresources"
     * And the RemoteDIR should not contain "old.txt"
     */

    @Test
    public void publish_with_clean_remote() throws IOException, InterruptedException {
        DockerContainer dock = createPublisherContainer();
        Resource cpTxt = resource("/ftp_plugin/odes.txt");
        FreeStyleJob j = jenkins.jobs.create();
        configurePublisher("asd", dock);
        j.configure();
        {
            j.copyResource(cpTxt);
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.getDefaultTransfer().cleanRemote.check();
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertThat(dock.tryCopyFile("/tmp/odes.txt", "/tmp/"), is(true));
        assertThat(!dock.tryCopyFile("/tmp/old.txt", "/tmp/"), is(true));
    }

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And a docker fixture "dock"
     * And a job
     * When I configure docker fixture as publisher site
     * And I configure the job with three publisher Transfer Sets
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
     * And publisher plugin should have published odes.txt,odes2.txt,odes3.txt on docker fixture
     */
    @Test
    public void publish_multiple_sets() throws IOException, InterruptedException {
        DockerContainer dock = createPublisherContainer();
        Resource cpTxt = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        configurePublisher("asd", dock);
        j.configure();
        {
            j.copyResource(cpTxt);
            j.copyResource(cpTxt, "odes2.txt");
            j.copyResource(cpTxt, "odes3.txt");
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
            fps.addTransferSet().sourceFile.set("odes2.txt");
            fps.addTransferSet().sourceFile.set("odes3.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertThat(dock.tryCopyFile("/tmp/odes.txt", "/tmp/"), is(true));
        assertThat(dock.tryCopyFile("/tmp/odes2.txt", "/tmp/"), is(true));
        assertThat(dock.tryCopyFile("/tmp/odes3.txt", "/tmp/"), is(true));
    }

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And 2 docker fixtures "dock"
     * And a job
     * When I configure docker docker1 as publisher site
     * And I configure docker  docker2 as publisher site
     * And I configure the job with docker1 Server
     * And one publisher Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles odes.txt
     * And I Add Server docker2
     * And one publisher Transfer Set
     * And I configure the Transfer Set
     * With SourceFiles odes.txt
     * And I copy resources odes.txt  into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And publisher plugin should have published my set of files at all docker fixtures
     */
    @Test
    public void publish_multiple_servers() throws IOException, InterruptedException {
        DockerContainer dock = createPublisherContainer();
        DockerContainer dock2 = createPublisherContainer();
        Resource cpTxt = resource("/ftp_plugin/odes.txt");

        FreeStyleJob j = jenkins.jobs.create();
        configurePublisher("docker1", dock);
        configurePublisher("docker2", dock2);
        j.configure();
        {
            j.copyResource(cpTxt);
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.configName.select("docker1");
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
            PublishGlobalPublisher.GlobalPublishSite fps2 = fp.addServer();
            fps2.configName.select("docker2");
            fps2.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertThat(dock.tryCopyFile("/tmp/odes.txt", "/tmp/dockertmp"), is(true));
        assertThat(dock.tryCopyFile("/tmp/odes.txt", "/tmp/dockertmp2"), is(true));

    }

    /**
     * @native(docker) Scenario: Configure a job with publisher publishing
     * Given I have installed the "publisher" plugin
     * And a docker fixture "dock"
     * And a ssh slave
     * And a job
     * When I configure docker fixture as publisher host
     * And I configure the job with one publisher Transfer Set
     * And I configure the Transfer Set
     * With Source Files "odes.txt"
     * And With Remote Directory myfolder/
     * And I copy resource "odes.txt" into workspace
     * And I save the job
     * And I build the job
     * Then the build should succeed
     * And publisher plugin should have published "odes.txt" on docker fixture
     */

    @Test
    public void publish_slave_resourses() throws IOException, InterruptedException, ExecutionException {
        DockerContainer dock = createPublisherContainer();
        Resource cpFile = resource("/ftp_plugin/odes.txt");

        Slave s = slaves.get().install(jenkins).get();
        s.configure();
        s.save();

        FreeStyleJob j = jenkins.jobs.create();
        configurePublisher("asd", dock);
        j.configure();
        {
            j.copyResource(cpFile);
            PublishGlobalPublisher fp = addGlobalPublisher(j);
            PublishGlobalPublisher.GlobalPublishSite fps = fp.getDefault();
            fps.getDefaultTransfer().sourceFile.set("odes.txt");
        }
        j.save();
        j.startBuild().shouldSucceed();
        assertThat(dock.tryCopyFile("/tmp/odes.txt", "/tmp"), is(true));
        assertThat(FileUtils.readFileToString(new File("/tmp/odes.txt")), CoreMatchers.is(cpFile.asText()));
    }
}
