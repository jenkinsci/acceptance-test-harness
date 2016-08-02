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

import java.util.concurrent.Callable;
import java.util.regex.Pattern;

@SuppressWarnings("CdiInjectionPointsInspection")
@WithPlugins("ant")
public class AntPluginTest extends AbstractJUnitTest {
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
        AntInstallation.install(jenkins, "ant_1.8.4", "1.8.4");

        buildHelloWorld("ant_1.8.4").shouldContainsConsoleOutput(
                "Unpacking http://archive.apache.org/dist/ant/binaries/apache-ant-1.8.4-bin.zip"
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
        job.configure(new Callable<Object>() {
            @Override public Object call() {
                job.copyResource(resource("ant/echo-helloworld.xml"), "build.xml");
                AntBuildStep ant = job.addBuildStep(AntBuildStep.class);
                if (name!=null)
                    ant.antName.select(name);
                ant.targets.set("hello");
                return null;
            }
        });

        return job.startBuild().shouldSucceed().shouldContainsConsoleOutput("Hello World");
    }

    private String localAntVersion(String antHome) {
        return jenkins.runScript(String.format("'%s/bin/ant -version'.execute().text", antHome));
    }
}
