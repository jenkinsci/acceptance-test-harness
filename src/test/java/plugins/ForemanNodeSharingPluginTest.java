package plugins;

import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.net.URISyntaxException;
import java.net.URL;

import org.codehaus.plexus.util.FileUtils;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.ForemanContainer;
import org.jenkinsci.test.acceptance.docker.fixtures.JavaContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithDocker;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.credentials.CredentialsPage;
import org.jenkinsci.test.acceptance.plugins.credentials.ManagedCredentials;
import org.jenkinsci.test.acceptance.plugins.foreman_node_sharing.ForemanSharedNodeCloudPageArea;
import org.jenkinsci.test.acceptance.plugins.ssh_credentials.SshPrivateKeyCredential;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.LabelAxis;
import org.jenkinsci.test.acceptance.po.LabelExpressionAxis;
import org.jenkinsci.test.acceptance.po.MatrixBuild;
import org.jenkinsci.test.acceptance.po.MatrixProject;
import org.jenkinsci.test.acceptance.po.MatrixRun;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.Inject;

/**
 * Acceptance Test Harness Test for Foreman Node Sharing Plugin.
 *
 */
@WithPlugins("foreman-node-sharing")
@WithDocker
public class ForemanNodeSharingPluginTest extends AbstractJUnitTest {
    @Inject private DockerContainerHolder<ForemanContainer> docker;
    @Inject private DockerContainerHolder<JavaContainer> docker2;

    private ForemanContainer foreman = null;
    private JavaContainer sshslave = null;
    private ForemanSharedNodeCloudPageArea cloud = null;
    private String labelExpression = "label1 aix";
    private String jobLabelExpression = "label1 && aix";

    private static final int FOREMAN_CLOUD_INIT_WAIT = 180;
    private static final int PROVISION_TIMEOUT = 240;

    /**
     * Setup instance before each test.
     * @throws Exception if occurs.
     */
    @Before public void setUp() throws Exception {
        foreman = docker.get();
        sshslave = docker2.get();

        CredentialsPage c = new CredentialsPage(jenkins, "_");
        c.open();

        final SshPrivateKeyCredential sc = c.add(SshPrivateKeyCredential.class);
        sc.username.set("test");
        sc.selectEnterDirectly().privateKey.set(sshslave.getPrivateKeyString());
        c.create();

        //CS IGNORE MagicNumber FOR NEXT 2 LINES. REASON: Mock object.
        elasticSleep(6000);

        if (populateForeman(foreman.getUrl().toString()+"/api/v2", sshslave.getCid(), labelExpression) != 0) {
            throw new Exception("failed to populate foreman");
        }

        jenkins.configure();
        cloud = addCloud(jenkins.getConfigPage());
        //CS IGNORE MagicNumber FOR NEXT 2 LINES. REASON: Mock object.
        elasticSleep(10000);

    }

    /**
     * Test a matrix job
     */
    @Test
    @WithPlugins("matrix-project")
    public void testMatrixJob() {
        jenkins.save();
        MatrixProject job = jenkins.jobs.create(MatrixProject.class);
        job.configure();
        LabelExpressionAxis a = job.addAxis(LabelExpressionAxis.class);
        a.values.set("label1");

        job.addUserAxis("X", "1 2 3 4 5");
        job.save();

        MatrixBuild b = job.startBuild().waitUntilFinished(240).as(MatrixBuild.class);
        for (MatrixRun config: b.getConfigurations()) {
        	config.shouldSucceed();
        }

    }

    /**
     * Test the connection and check version.
     * @throws IOException if occurs.
     */
    @Test
    public void testConnection() throws IOException {
        System.out.println(foreman.getIpAddress());
        cloud.testConnection();
        waitFor(driver, hasContent("Foreman version is"), FOREMAN_CLOUD_INIT_WAIT);
    }

    /**
     * Verify that compatible host checker works.
     * @throws IOException if occurs.
     */
    @Test
    public void testCheckForCompatible() throws IOException {
        cloud.checkForCompatibleHosts();
        waitFor(driver, hasContent(sshslave.getCid()), FOREMAN_CLOUD_INIT_WAIT);
    }

    /**
     * Test that we can provision, build and release.
     * @throws Exception if occurs.
     */
    @Test
    public void testProvision() throws Exception {
        jenkins.save();

        DumbSlave slave = jenkins.slaves.create(DumbSlave.class, "ignore-this-slave++needed-to-enable-job-labels");
        slave.setExecutors(1);
        slave.save();

        FreeStyleJob job = createAndConfigureJob();

        Build b = job.scheduleBuild();
        b.waitUntilFinished(PROVISION_TIMEOUT);

        jenkins.runScript("Jenkins.instance.nodes.each { it.terminate() }");

    }

    /**
     * Test that we can provision after a restart, build and release.
     * @throws Exception if occurs.
     */
    @Test
    public void testProvisionAfterRestart() throws Exception {
        jenkins.save();

        DumbSlave slave = jenkins.slaves.create(DumbSlave.class, "ignore-this-slave++needed-to-enable-job-labels");
        slave.setExecutors(1);
        slave.save();

        FreeStyleJob job = createAndConfigureJob();

        Build b = job.scheduleBuild();
        jenkins.restart();
        b.waitUntilFinished(PROVISION_TIMEOUT);

        jenkins.runScript("Jenkins.instance.nodes.each { it.terminate() }");

    }

    /**
     * Create and configure Test job.
     * @return FreeStyleJob.
     */
    private FreeStyleJob createAndConfigureJob() {
        FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class);
        job.setLabelExpression(jobLabelExpression);
        job.addShellStep("sleep 15");
        job.save();
        return job;
    }

    /**
     * Populate Foreman using hammer script.
     * @param server Foreman server url.
     * @param hostToCreate host name for creation.
     * @param labels list of labels to add to host.
     * @return exit code of script execution.
     * @throws URISyntaxException if occurs.
     * @throws IOException if occurs.
     * @throws InterruptedException if occurs.
     */
    private int populateForeman(String server, String hostToCreate, String labels) throws
        URISyntaxException, IOException, InterruptedException {

        URL script =
                ForemanNodeSharingPluginTest.class.getClassLoader()
                .getResource("foreman_node_sharing_plugin/setup.sh");
        File tempScriptFile = File.createTempFile("setup", ".sh");
        tempScriptFile.setExecutable(true);
        FileUtils.copyURLToFile(script, tempScriptFile);

        ProcessBuilder pb = new ProcessBuilder("/bin/bash", tempScriptFile.getAbsolutePath(),
                server,
                hostToCreate,
                sshslave.getIpAddress(),
                labels);

        pb.directory(tempScriptFile.getParentFile());
        pb.redirectOutput(Redirect.INHERIT);
        pb.redirectError(Redirect.INHERIT);
        pb.redirectInput(Redirect.INHERIT);
        Process p = pb.start();
        return p.waitFor();
    }

    /**
     * Add cloud to Jenkins Config.
     * @param config Jenkins Configuration Page.
     * @return a ForemanCloudPageArea.
     * @throws IOException if occurs.
     */
    private ForemanSharedNodeCloudPageArea addCloud(JenkinsConfig config) throws IOException {
        return config.addCloud(ForemanSharedNodeCloudPageArea.class)
                .name(Jenkins.createRandomName())
                .url(foreman.getUrl().toString() + "/api/")
                .user("admin")
                .password("changeme")
                .setCredentials("test");
    }

}
