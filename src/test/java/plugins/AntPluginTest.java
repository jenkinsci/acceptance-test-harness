package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

import com.google.inject.Inject;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.test.acceptance.Matchers;
import org.jenkinsci.test.acceptance.docker.DockerContainerHolder;
import org.jenkinsci.test.acceptance.docker.fixtures.SshAgentContainer;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.ant.AntBuildStep;
import org.jenkinsci.test.acceptance.plugins.ant.AntInstallation;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.DumbSlave;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ToolInstallation;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

@SuppressWarnings("CdiInjectionPointsInspection")
@WithPlugins({"ant", "ssh-slaves"})
public class AntPluginTest extends AbstractJUnitTest {

    private static final String INSTALL_VERSION_1_8 = "1.8.4";
    private static final String INSTALL_NAME_1_8 = "ant_" + INSTALL_VERSION_1_8;

    private static final String INSTALL_VERSION_1_10 = "1.10.5";
    private static final String INSTALL_NAME_1_10 = "ant_" + INSTALL_VERSION_1_10;

    private static final String NATIVE_ANT_NAME = "native_ant";

    private static final String BUILD_FILE = "custom-build-file.xml";

    FreeStyleJob job;
    private AntBuildStep step;

    @Inject
    private DockerContainerHolder<SshAgentContainer> docker;

    private SshAgentContainer sshd;
    private DumbSlave agent;

    @Before
    public void setUp() {
        job = jenkins.jobs.create(FreeStyleJob.class);
        sshd = docker.get();
    }

    private void useCustomAgent() {
        String remote_fs = "/tmp";
        agent = jenkins.slaves.create(DumbSlave.class);
        agent.setExecutors(1);
        agent.remoteFS.set(remote_fs);

        sshd.configureSSHSlaveLauncher(agent).pwdCredentials("test", "test");
        agent.save();

        agent.waitUntilOnline();
        assertTrue(agent.isOnline());

        job.configure();
        job.setLabelExpression(agent.getName());

        job.save();
    }

    @Test
    public void use_default_ant_installation() {
        useCustomAgent();
        buildHelloWorld(null);
    }

    @Test
    public void autoInstallAnt() {
        AntInstallation.install(jenkins, INSTALL_NAME_1_8, INSTALL_VERSION_1_8);

        buildHelloWorld(INSTALL_NAME_1_8)
                .shouldContainsConsoleOutput("Unpacking (http|https)://archive.apache.org/dist/ant/binaries/apache-ant-"
                        + INSTALL_VERSION_1_8 + "-bin.zip");
    }

    @Test
    public void autoInstallMultipleAnt() {
        AntInstallation.install(jenkins, INSTALL_NAME_1_8, INSTALL_VERSION_1_8);
        AntInstallation.install(jenkins, INSTALL_NAME_1_10, INSTALL_VERSION_1_10);

        buildHelloWorld(INSTALL_NAME_1_10)
                .shouldContainsConsoleOutput("Unpacking (http|https)://archive.apache.org/dist/ant/binaries/apache-ant-"
                        + INSTALL_VERSION_1_10 + "-bin.zip");
    }

    @Test
    public void locallyInstalledAnt() {
        useCustomAgent();
        setUpAntInstallation();

        job.configure();
        job.copyResource(resource("ant/echo-helloworld.xml"), "build.xml");
        AntBuildStep step = job.addBuildStep(AntBuildStep.class);
        step.antName.select(NATIVE_ANT_NAME);
        step.targets.set("-version");
        job.save();

        String expectedVersion = "1.10.14"; // this is the version installed in the java container by the ubuntu noble
        job.startBuild().shouldSucceed().shouldContainsConsoleOutput(Pattern.quote(expectedVersion));
    }

    @Test
    @WithPlugins({"workflow-job", "workflow-cps", "workflow-basic-steps", "workflow-durable-task-step"})
    public void testWithAntPipelineBlock() {
        useCustomAgent();
        String script_pipeline_ant = "node (\"" + agent.getName() + "\") {\n" + "    withAnt(installation: '"
                + NATIVE_ANT_NAME + "') {\n" + "       sh \"ant -version\"\n"
                + "    }\n"
                + "}";
        String antHome = setUpAntInstallation();

        String expectedVersion = "1.10.14"; // this is the version installed in the java container by the ubuntu noble;

        WorkflowJob workflowJob = jenkins.jobs.create(WorkflowJob.class);
        workflowJob.script.set(script_pipeline_ant);

        workflowJob.save();

        workflowJob.startBuild().shouldSucceed();

        String console = workflowJob.getLastBuild().getConsole();
        assertThat(console, containsString("withAnt"));
        assertThat(console, containsString("ant -version"));
        assertThat(console, containsString(expectedVersion));
    }

    @Test
    public void testAdvancedConfiguration() {
        String ok_prop1 = "okPROP1=foo_bar_ok_1";
        String ok_prop2 = "okPROP2=foo_bar_ok_2";
        String nok_prop1 = "nokPROP1=foo_bar_nok_1";
        String nok_prop2 = "nokPROP2=foo_bar_nok_2";
        String properties = ok_prop1 + "\n" + ok_prop2 + "\n" + nok_prop1 + "\n" + nok_prop2;

        useCustomAgent();
        setUpAnt();

        antBuildStepAdvancedConfiguration(step, BUILD_FILE, properties);

        job.save();

        job.startBuild().shouldSucceed();

        String console = job.getLastBuild().getConsole();
        assertThat(console, containsString("-D" + ok_prop1));
        assertThat(console, containsString("-D" + ok_prop2));
        assertThat(console, containsString("-D" + nok_prop1));
        assertThat(console, containsString("-D" + nok_prop2));
        assertThat(console, containsString("[echoproperties] " + ok_prop1));
        assertThat(console, containsString("[echoproperties] " + ok_prop2));
        assertThat(console, not(Matchers.containsRegexp("[echoproperties] " + nok_prop1, Pattern.MULTILINE)));
        assertThat(console, not(Matchers.containsRegexp("[echoproperties] " + nok_prop2, Pattern.MULTILINE)));
    }

    @Test
    public void testCustomBuildFailDoesNotExist() {
        String fake_build_file = "fake.xml";

        useCustomAgent();
        setUpAnt();

        antBuildStepAdvancedConfiguration(step, fake_build_file, null);

        job.save();

        job.startBuild().shouldFail();

        String console = job.getLastBuild().getConsole();
        assertThat(
                console,
                Matchers.containsRegexp(
                        "ERROR: Unable to find build script at .*/" + fake_build_file, Pattern.MULTILINE));
    }

    private Build buildHelloWorld(final String name) {
        job.configure(() -> {
            job.copyResource(resource("ant/echo-helloworld.xml"), "build.xml");
            AntBuildStep ant = job.addBuildStep(AntBuildStep.class);
            if (name != null) {
                ant.antName.select(name);
            }
            ant.targets.set("hello");
            return null;
        });

        return job.startBuild().shouldSucceed().shouldContainsConsoleOutput("Hello World");
    }

    private void setUpAnt() {
        setUpAntInstallation();

        job.configure();
        job.copyResource(resource("ant/" + BUILD_FILE), BUILD_FILE);
        step = job.addBuildStep(AntBuildStep.class);
        step.antName.select(NATIVE_ANT_NAME);
        step.targets.set("");
    }

    private String setUpAntInstallation() {
        AntInstallation ant = ToolInstallation.addTool(jenkins, AntInstallation.class);
        ant.name.set(NATIVE_ANT_NAME);
        String antHome = ant.useNative();
        ant.getPage().save();

        return antHome;
    }

    private void antBuildStepAdvancedConfiguration(AntBuildStep step, String buildFile, String properties) {
        step.control("advanced-button").click();
        step.control(
                        By.xpath(
                                "(//div[contains(@descriptorid, 'Ant')]/div/div[contains(@class, 'dropdownList-container')]//*[@type = 'button'])[2]"))
                .click();
        step.control("properties").set(StringUtils.defaultString(properties));
        step.control(
                        By.xpath(
                                "(//div[contains(@descriptorid, 'Ant')]/div/div[contains(@class, 'dropdownList-container')]//*[@type = 'button'])[1]"))
                .click();
        step.control("buildFile").set(StringUtils.defaultString(buildFile));
    }
}
