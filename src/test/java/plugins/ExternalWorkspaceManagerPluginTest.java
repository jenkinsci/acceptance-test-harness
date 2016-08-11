package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.external_workspace_manager.ExternalGlobalConfig;
import org.jenkinsci.test.acceptance.plugins.external_workspace_manager.ExternalNodeConfig;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.jenkinsci.test.acceptance.slave.LocalSlaveController;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openqa.selenium.By;

import java.io.File;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

/**
 * Acceptance tests for External Workspace Manager Plugin.
 *
 * @author Alexandru Somai
 */
@WithPlugins({"workflow-aggregator", "job-restrictions", "git"})
public class ExternalWorkspaceManagerPluginTest extends AbstractJUnitTest {

    @ClassRule
    public static TemporaryFolder tmp = new TemporaryFolder();

    private static final String DISK_POOL_ID = "diskpool1";

    private String fakeMountingPoint;

    @Before
    public void setUp() throws Exception {
        // temporary, until the plugin is released!
        jenkins.getPluginManager().installPlugin(new File("/Users/alexsomai/workspace/external-workspace-manager/target/external-workspace-manager.hpi"));

        MavenInstallation.installMaven(jenkins, "M3", "3.1.0");

        setUpGlobalConfig();

        fakeMountingPoint = tmp.newFolder().getAbsolutePath();
        setUpNode("linux", fakeMountingPoint);
        setUpNode("test", fakeMountingPoint);
    }

    @Test
    public void shareWorkspaceOneJobTwoNodes() throws Exception {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(String.format("" +
                        "def extWorkspace = exwsAllocate diskPoolId: '%s' \n" +
                        "node ('linux') { \n" +
                        "   exws (extWorkspace) { \n" +
                        "       git 'https://github.com/alexsomai/dummy-hello-world.git' \n" +
                        "       def mvnHome = tool 'M3'\n" +
                        "       sh \"${mvnHome}/bin/mvn clean install -DskipTests\" \n" +
                        "   } \n" +
                        "} \n" +
                        "node ('test') { \n" +
                        "   exws (extWorkspace) { \n" +
                        "       def mvnHome = tool 'M3' \n" +
                        "       sh \"${mvnHome}/bin/mvn test\" \n" +
                        "   } \n" +
                        "}"
                , DISK_POOL_ID));
        job.sandbox.uncheck();
        job.save();

        Build build = job.startBuild();
        build.shouldSucceed();
        assertThat(build.getConsole(), containsString(String.format("Running in %s/%s/%s", fakeMountingPoint, job.name, build.getNumber())));

        build.visit("exwsAllocate");
        String exwsAllocateText = driver.findElement(By.id("main-panel")).getText();
        assertThat(exwsAllocateText, containsString(String.format("Disk Pool ID: %s", DISK_POOL_ID)));
        assertThat(exwsAllocateText, containsString("Disk ID: disk1"));
        assertThat(exwsAllocateText, containsString(String.format("Complete Path on Disk: %s/%s", job.name, build.getNumber())));
    }

    private void setUpGlobalConfig() {
        jenkins.configure();
        ExternalGlobalConfig globalConfig = new ExternalGlobalConfig(jenkins.getConfigPage());
        globalConfig.addDiskPool(DISK_POOL_ID, "disk1", "disk2");
        jenkins.save();
    }

    private void setUpNode(String label, String fakeMountingPoint) throws ExecutionException, InterruptedException {
        SlaveController controller = new LocalSlaveController();
        Slave linuxSlave = controller.install(jenkins).get();
        linuxSlave.configure();
        linuxSlave.setLabels(label);

        ExternalNodeConfig nodeConfig = new ExternalNodeConfig(linuxSlave);
        nodeConfig.setConfig(DISK_POOL_ID, "disk1", "disk2", fakeMountingPoint);
        linuxSlave.save();
    }
}
