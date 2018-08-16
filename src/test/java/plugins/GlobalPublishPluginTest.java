package plugins;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.docker.Docker;
import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.DockerTest;
import org.jenkinsci.test.acceptance.junit.Resource;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.plugins.publish_over.PublishGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.publish_over.PublishGlobalPublisher;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.PageObject;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;

/**
 * Abstract class for the publisher class.
 * This class implements base test which are used by all PublishOver* Classes.
 * <p/>
 * For an concrete implementation of the test, the abstract functions need to be implemented.
 *
 * @author Tobias Meyer
 */
@Category(DockerTest.class)
@WithDocker
public abstract class GlobalPublishPluginTest<T extends DockerContainer> extends AbstractJUnitTest {
    @Inject
    Docker docker;
    @Inject
    SlaveProvider slaves;
    @Inject
    DockerContainerHolder<T> container;

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

    @Test
    public void publish_resources() throws Exception {
        DockerContainer dock = container.get();
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

        Build build = j.startBuild().shouldSucceed();
        assertThat(build.getConsole(), not(containsString("Transferred 0 file(s)")));
        assertEquals(cpFile.asText(), fileContents(dock, "/tmp/odes.txt"));
    }

    @Test
    public void publish_jenkins_variables() throws Exception {
        DockerContainer dock = container.get();
        Resource cpFile = resource("/ftp_plugin/odes.txt");
        String randomName = PageObject.createRandomName();
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

        Build build = j.startBuild().shouldSucceed();
        assertThat(build.getConsole(), not(containsString("Transferred 0 file(s)")));
        assertEquals(cpFile.asText(), fileContents(dock, randomPath + "odes.txt"));
    }

    @Test
    public void publish_resources_and_remove_prefix() throws Exception {
        DockerContainer dock = container.get();
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
        Build build = j.startBuild().shouldSucceed();
        assertThat(build.getConsole(), not(containsString("Transferred 0 file(s)")));
        assertEquals(test.asText(), fileContents(dock, "/tmp/test.txt"));
    }

    @Test
    public void publish_resources_with_excludes() {
        DockerContainer dock = container.get();
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
        Build build = j.startBuild().shouldSucceed();
        assertThat(build.getConsole(), not(containsString("Transferred 0 file(s)")));
        assertThat(dock, fileExists("/tmp/prefix_/"));
        assertThat(dock, not(fileExists("/tmp/prefix_/.exclude")));
    }

    @Test
    public void publish_with_default_pattern() {
        DockerContainer dock = container.get();
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
        Build build = j.startBuild().shouldSucceed();
        assertThat(build.getConsole(), not(containsString("Transferred 0 file(s)")));
        assertThat(dock, fileExists("/tmp/prefix_/test.txt"));
        assertThat(dock, fileExists("/tmp/odes.txt"));
    }

    @Test
    public void publish_with_own_pattern() {
        DockerContainer dock = container.get();
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
        Build build = j.startBuild().shouldSucceed();
        assertThat(build.getConsole(), not(containsString("Transferred 0 file(s)")));
        assertThat(dock, fileExists("/tmp/te,st.txt"));
        assertThat(dock, fileExists("/tmp/odes.txt"));
    }

    @Test
    public void publish_with_default_exclude() {
        DockerContainer dock = container.get();
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
        Build build = j.startBuild().shouldSucceed();
        assertThat(build.getConsole(), not(containsString("Transferred 0 file(s)")));
        assertThat(dock, fileExists("/tmp/odes.txt"));
        assertThat(dock, not(fileExists("/tmp/.svn")));
        assertThat(dock, not(fileExists("/tmp/CVS")));
    }

    @Test
    public void publish_with_no_default_exclude() {
        DockerContainer dock = container.get();
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
        Build build = j.startBuild().shouldSucceed();
        assertThat(build.getConsole(), not(containsString("Transferred 0 file(s)")));
        assertThat(dock, fileExists("/tmp/odes.txt"));
        assertThat(dock, fileExists("/tmp/.svn"));
        assertThat(dock, fileExists("/tmp/CVS"));
    }

    @Test
    public void publish_with_empty_directory() throws Exception {
        DockerContainer dock = container.get();
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
        assertThat(dock, fileExists("/tmp/odes.txt"));
        assertThat(dock, fileExists("/tmp/empty"));
    }

    @Test
    public void publish_without_empty_directory() throws Exception {
        DockerContainer dock = container.get();
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
        Build build = j.startBuild().shouldSucceed();
        assertThat(build.getConsole(), not(containsString("Transferred 0 file(s)")));
        assertThat(dock, fileExists("/tmp/odes.txt"));
        assertThat(dock, not(fileExists("/tmp/dockertmp/empty")));
    }

    @Test  @Ignore
    public void publish_without_flatten_files() {
        DockerContainer dock = container.get();
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
        Build build = j.startBuild().shouldSucceed();
        assertThat(build.getConsole(), not(containsString("Transferred 0 file(s)")));
        assertThat(dock, fileExists("/tmp/flat/odes.txt"));
        assertThat(dock, fileExists("/tmp/odes.txt"));
    }

    @Test
    public void publish_with_flatten_files() {
        DockerContainer dock = container.get();
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

    @Test
    public void publish_with_date_format() {
        DockerContainer dock = container.get();
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
        Build build = j.startBuild().shouldSucceed();
        assertThat(build.getConsole(), not(containsString("Transferred 0 file(s)")));
        assertThat(dock, fileExists("/tmp/" + dateFormat.format(date) + "/odes.txt"));
    }

    @Test
    public void publish_with_clean_remote() {
        DockerContainer dock = container.get();
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
        Build build = j.startBuild().shouldSucceed();
        assertThat(build.getConsole(), not(containsString("Transferred 0 file(s)")));
        assertThat(dock, fileExists("/tmp/odes.txt"));
        assertThat(dock, not(fileExists("/tmp/old.txt")));
    }

    @Test
    public void publish_multiple_sets() {
        DockerContainer dock = container.get();
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
        Build build = j.startBuild().shouldSucceed();
        assertThat(build.getConsole(), not(containsString("Transferred 0 file(s)")));
        assertThat(dock, fileExists("/tmp/odes.txt"));
        assertThat(dock, fileExists("/tmp/odes2.txt"));
        assertThat(dock, fileExists("/tmp/odes3.txt"));
    }

    @Test @Ignore
    public void publish_multiple_servers() {
        DockerContainer dock = container.get();
        DockerContainer dock2 = injector.getInstance(DockerContainerHolder.class).get();
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
        Build build = j.startBuild().shouldSucceed();
        assertThat(build.getConsole(), not(containsString("Transferred 0 file(s)")));
        assertThat(dock, fileExists("/tmp/odes.txt"));
        assertThat(dock2, fileExists("/tmp/odes.txt"));
    }

    @Test
    public void publish_slave_resourses() throws IOException, InterruptedException, ExecutionException {
        DockerContainer dock = container.get();
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
        Build build = j.startBuild().shouldSucceed();
        assertThat(build.getConsole(), not(containsString("Transferred 0 file(s)")));
        assertEquals(cpFile.asText(), fileContents(dock, "/tmp/odes.txt"));
    }

    private Matcher<DockerContainer> fileExists(final String path) {
        return new Matcher<DockerContainer>("container with file/directory '%s' in it", path) {
            @Override public boolean matchesSafely(DockerContainer dock) {
                try {
                    int exit = Docker.cmd("exec", dock.getCid(), "test", "-e", path).system();
                    if (exit == 0) return true;
                    if (exit == 1) return false;
                    throw new Error("Unexpected return code while checking for file existence: " + exit);
                } catch (IOException|InterruptedException e) {
                    throw new Error("Failed testing for file existence in the container", e);
                }
            }
        };

    }

    private String fileContents(DockerContainer dock, String path) throws IOException, InterruptedException {
        return Docker.cmd("exec", dock.getCid(), "cat", path).popen().verifyOrDieWith("Failed reading the file content");
    }
}
