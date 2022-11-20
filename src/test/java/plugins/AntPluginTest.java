package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.ant.AntBuildStep;
import org.jenkinsci.test.acceptance.plugins.ant.AntInstallation;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ToolInstallation;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

@SuppressWarnings("CdiInjectionPointsInspection")
@WithPlugins("ant")
public class AntPluginTest extends AbstractJUnitTest {

    private static final String INSTALL_VERSION_1_8 = "1.8.4";
    private static final String INSTALL_NAME_1_8 = "ant_" + INSTALL_VERSION_1_8;

    private static final String INSTALL_VERSION_1_10 = "1.10.5";
    private static final String INSTALL_NAME_1_10 = "ant_" + INSTALL_VERSION_1_10;

    FreeStyleJob job;

    @Before
    public void setUp() {
        job = jenkins.jobs.create(FreeStyleJob.class);
    }

    @Test @Native("ant")
    public void use_default_ant_installation() {
        buildHelloWorld(null);
    }

    @Test
    public void autoInstallAnt() {
        AntInstallation.install(jenkins, INSTALL_NAME_1_8, INSTALL_VERSION_1_8);

        buildHelloWorld(INSTALL_NAME_1_8).shouldContainsConsoleOutput(
            "Unpacking (http|https)://archive.apache.org/dist/ant/binaries/apache-ant-" + INSTALL_VERSION_1_8 + "-bin.zip"
        );
    }
    
    @Test
    public void autoInstallMultipleAnt() {
        AntInstallation.install(jenkins, INSTALL_NAME_1_8, INSTALL_VERSION_1_8);
        AntInstallation.install(jenkins, INSTALL_NAME_1_10, INSTALL_VERSION_1_10);

        buildHelloWorld(INSTALL_NAME_1_10).shouldContainsConsoleOutput(
            "Unpacking (http|https)://archive.apache.org/dist/ant/binaries/apache-ant-" + INSTALL_VERSION_1_10 + "-bin.zip"
        );
    }

    @Test @Native("ant")
    public void locallyInstalledAnt() {
        AntInstallation ant = ToolInstallation.addTool(jenkins, AntInstallation.class);
        ant.name.set("native_ant");
        String antHome = ant.useNative();
        ant.getPage().save();

        job.configure();
        job.copyResource(resource("ant/echo-helloworld.xml"), "build.xml");
        AntBuildStep step = job.addBuildStep(AntBuildStep.class);
        step.antName.select("native_ant");
        step.targets.set("-version");
        job.save();

        String expectedVersion = localAntVersion(antHome);
        job.startBuild().shouldSucceed().shouldContainsConsoleOutput(Pattern.quote(expectedVersion));
    }

    private Build buildHelloWorld(final String name) {
        job.configure(() -> {
            job.copyResource(resource("ant/echo-helloworld.xml"), "build.xml");
            AntBuildStep ant = job.addBuildStep(AntBuildStep.class);
            if (name!=null)
                ant.antName.select(name);
            ant.targets.set("hello");
            return null;
        });

        return job.startBuild().shouldSucceed().shouldContainsConsoleOutput("Hello World");
    }

    private String localAntVersion(String antHome) {
        return jenkins.runScript(String.format("'%s/bin/ant -version'.execute().text", antHome));
    }
}
