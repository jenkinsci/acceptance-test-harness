package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;

import java.util.concurrent.ExecutionException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
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

/**
 * Acceptance tests for External Workspace Manager Plugin.
 *
 * @author Alexandru Somai
 */
@WithPlugins({
    "external-workspace-manager",
    "git",
    "run-selector",
    "workflow-basic-steps",
    "workflow-cps",
    "workflow-durable-task-step",
    "workflow-job",
    "ws-cleanup",
})
public class ExternalWorkspaceManagerPluginTest extends AbstractJUnitTest {

    @ClassRule
    public static TemporaryFolder tmp = new TemporaryFolder();

    private static final String DISK_POOL_ID = "diskpool1";
    private static final String DISK_ONE = "disk1";
    private static final String DISK_TWO = "disk2";
    private static final String MOUNT_FROM_MASTER_TO_DISK_ONE = "/fake-mount-point-1";
    private static final String MOUNT_FROM_MASTER_TO_DISK_TWO = "/fake-mount-point-2";

    private String fakeNodeMountingPoint;

    @Before
    public void setUp() throws Exception {
        MavenInstallation.installMaven(jenkins, "M3", "3.9.4");

        setUpGlobalConfig();

        fakeNodeMountingPoint = tmp.newFolder().getAbsolutePath();
        setUpNode("linux", fakeNodeMountingPoint);
        setUpNode("test", fakeNodeMountingPoint);
    }

    @Test
    public void shareWorkspaceOneJobTwoNodes() {
        WorkflowJob job = createWorkflowJob(String.format("""
                def extWorkspace = exwsAllocate '%s'
                node ('linux') {
                   exws (extWorkspace) {
                      writeFile file: 'marker', text: 'content'
                   }
                }
                node ('test') {
                   exws (extWorkspace) {
                       def content = readFile(file: 'marker')
                       if (content != 'content') error('Content mismatch: ' + content)
                   }
                }""", DISK_POOL_ID));

        Build build = job.startBuild();
        build.shouldSucceed();
        assertThat(
                build.getConsole(),
                containsString(
                        String.format("Running in %s/%s/%s", fakeNodeMountingPoint, job.name, build.getNumber())));

        verifyExternalWorkspacesAction(job.name, build);
    }

    @Test
    public void shareWorkspaceTwoJobsTwoNodes() {
        WorkflowJob upstreamJob = createWorkflowJob(String.format("""
                def extWorkspace = exwsAllocate '%s'
                node ('linux') {
                   exws (extWorkspace) {
                      writeFile file: 'marker', text: 'content'
                   }
                }""", DISK_POOL_ID));

        Build upstreamBuild = upstreamJob.startBuild();
        upstreamBuild.shouldSucceed();
        assertThat(
                upstreamBuild.getConsole(),
                containsString(String.format(
                        "Running in %s/%s/%s", fakeNodeMountingPoint, upstreamJob.name, upstreamBuild.getNumber())));
        verifyExternalWorkspacesAction(upstreamJob.name, upstreamBuild);

        WorkflowJob downstreamJob = createWorkflowJob(String.format("""
                def run = selectRun '%s'
                def extWorkspace = exwsAllocate selectedRun: run
                node ('test') {
                   exws (extWorkspace) {
                       def content = readFile(file: 'marker')
                       if (content != 'content') error('Content mismatch: ' + content)
                   }
                }""", upstreamJob.name));

        Build downstreamBuild = downstreamJob.startBuild();
        downstreamBuild.shouldSucceed();
        assertThat(
                downstreamBuild.getConsole(),
                containsString(String.format(
                        "Running in %s/%s/%s", fakeNodeMountingPoint, upstreamJob.name, upstreamBuild.getNumber())));
        verifyExternalWorkspacesAction(upstreamJob.name, downstreamBuild);
    }

    @Test
    public void externalWorkspaceCleanup() {
        WorkflowJob job = createWorkflowJob(String.format("""
                def extWorkspace = exwsAllocate '%s'
                node ('linux') {
                    exws (extWorkspace) {
                        try {
                            writeFile file: 'foobar.txt', text: 'any'
                        } finally {
                            step ([$class: 'WsCleanup'])
                        }
                    }
                }""", DISK_POOL_ID));

        Build build = job.startBuild();
        build.shouldSucceed();
        String console = build.getConsole();
        assertThat(
                console,
                containsString(
                        String.format("Running in %s/%s/%s", fakeNodeMountingPoint, job.name, build.getNumber())));
        assertThat(console, containsString("[WS-CLEANUP] Deleting project workspace"));
        assertThat(console, containsString("[WS-CLEANUP] done"));
        assertThat(
                FileUtils.listFiles(
                        tmp.getRoot(),
                        FileFilterUtils.nameFileFilter("foobar.txt"),
                        FileFilterUtils.directoryFileFilter()),
                hasSize(0));
    }

    private void setUpGlobalConfig() {
        jenkins.configure();
        ExternalGlobalConfig globalConfig = new ExternalGlobalConfig(jenkins.getConfigPage());
        globalConfig.addDiskPool(
                DISK_POOL_ID, DISK_ONE, DISK_TWO, MOUNT_FROM_MASTER_TO_DISK_ONE, MOUNT_FROM_MASTER_TO_DISK_TWO);
        jenkins.save();
    }

    private void setUpNode(String label, String fakeMountingPoint) throws ExecutionException, InterruptedException {
        SlaveController controller = new LocalSlaveController();
        Slave linuxSlave = controller.install(jenkins).get();
        linuxSlave.configure();
        linuxSlave.setLabels(label);

        ExternalNodeConfig nodeConfig = new ExternalNodeConfig(linuxSlave);
        nodeConfig.setConfig(DISK_POOL_ID, DISK_ONE, DISK_TWO, fakeMountingPoint);
        linuxSlave.save();
    }

    private WorkflowJob createWorkflowJob(String script) {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(script);
        job.save();

        return job;
    }

    private void verifyExternalWorkspacesAction(String jobName, Build build) {
        build.visit("exwsAllocate");
        String exwsAllocateText = driver.findElement(By.id("main-panel")).getText();
        assertThat(exwsAllocateText, containsString(String.format("Disk Pool ID: %s", DISK_POOL_ID)));
        assertThat(exwsAllocateText, containsString(String.format("Disk ID: %s", DISK_ONE)));
        assertThat(
                exwsAllocateText,
                containsString(String.format("Workspace path on %s: %s/%s", DISK_ONE, jobName, build.getNumber())));
        assertThat(
                exwsAllocateText,
                containsString(String.format(
                        "Complete workspace path on %s (from Jenkins master): %s/%s/%s",
                        DISK_ONE, MOUNT_FROM_MASTER_TO_DISK_ONE, jobName, build.getNumber())));
    }
}
