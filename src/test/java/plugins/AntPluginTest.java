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

/**
 * Plugin test for Ant.
 *
 * Also acting as an example for writing tests in plain-old JUnit.
 */
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

    /**
     Scenario: Add Auto-Installed Ant
       Given I have installed the "ant" plugin
       And I have Ant "1.8.4" auto-installation named "ant_1.8.4" configured
       And a job
       When I add an Ant build step for "ant_1.8.4"
         """
           <project default="hello">
             <target name="hello">
               <echo message="Hello World"/>
             </target>
           </project>
         """
       And I build the job
       Then the build should succeed
       And console output should contain
           """
           Unpacking http://archive.apache.org/dist/ant/binaries/apache-ant-1.8.4-bin.zip
           """
     */
    @Test
    public void autoInstallAnt() {
        AntInstallation.install(jenkins, "ant_1.8.4", "1.8.4");

        buildHelloWorld("ant_1.8.4").shouldContainsConsoleOutput(
                "Unpacking http://archive.apache.org/dist/ant/binaries/apache-ant-1.8.4-bin.zip"
        );
    }

    @Test @Native("ant")
    public void locallyInstalledAnt() {
        String expectedVersion = localAntVersion();

        AntInstallation ant = ToolInstallation.addTool(jenkins, AntInstallation.class);
        ant.name.set("native_ant");
        ant.useNative();
        ant.getPage().save();

        job.configure();
        job.copyResource(resource("ant/echo-helloworld.xml"), "build.xml");
        AntBuildStep step = job.addBuildStep(AntBuildStep.class);
        step.antName.select("native_ant");
        step.targets.set("-version");
        job.save();

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

    private String localAntVersion() {
        return jenkins.runScript("'ant -version'.execute().text");
    }
}
