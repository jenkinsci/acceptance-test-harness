package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.ant.AntBuildStep;
import org.jenkinsci.test.acceptance.plugins.ant.AntInstallation;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
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

    /**
     Scenario: Configure a job with Ant build steps
       Given I have installed the "ant" plugin
       And a job
       When I configure the job
       And I add an Ant build step
         """
           <project default="hello">
             <target name="hello">
               <echo message="Hello World"/>
             </target>
           </project>
         """
       When I build the job
       Then the build should succeed
     */
    @Test
    public void allow_user_to_use_Ant_in_freestyle_project() {
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

    /**
     Scenario: Add locally installed Ant
       Given I have installed the "ant" plugin
       And fake Ant installation at "/tmp/fake-ant"
       And a job
       And I have Ant "local_ant_1.8.4" installed in "/tmp/fake-ant" configured
       When I add an Ant build step for "local_ant_1.8.4"
         """
           <project default="hello">
             <target name="hello">
               <echo message="Hello World"/>
             </target>
           </project>
         """
       And I build the job
       Then console output should contain "fake ant at /tmp/fake-ant/bin/ant"
       And the build should succeed
     */
    @Test @Native("ant")
    public void locallyInstalledAnt() {
        String expectedVersion = localAntVersion();

        jenkins.configure();
        AntInstallation ant = jenkins.getConfigPage().addTool(AntInstallation.class);
        ant.name.set("native_ant");
        ant.useNative();
        jenkins.save();

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
