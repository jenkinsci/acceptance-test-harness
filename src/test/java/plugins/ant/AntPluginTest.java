package plugins.ant;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.ant.AntBuildStep;
import org.jenkinsci.test.acceptance.plugins.ant.AntGlobalConfig;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import java.util.concurrent.Callable;

/**
 * Plugin test for Ant.
 *
 * Also acting as an example for writing tests in plain-old JUnit.
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings("CdiInjectionPointsInspection")
@WithPlugins("ant")
public class AntPluginTest extends AbstractJUnitTest {
    @Inject
    Jenkins jenkins;

    AntGlobalConfig antgc;
    FreeStyleJob job;

    @Before
    public void setUp() {
        antgc = new AntGlobalConfig(jenkins);
        job = jenkins.createJob(FreeStyleJob.class);
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
        buildHelloWorld();
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
        JenkinsConfig c = jenkins.getConfigPage();
        c.configure();
        c.addTool("Add Ant");
        antgc.addAutoInstallation("ant_1.8.4", "1.8.4");
        c.save();

        buildHelloWorld().shouldContainsConsoleOutput(
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
    @Test
    public void locallyInstalledAnt() {
        JenkinsConfig c = jenkins.getConfigPage();
        c.configure();
        c.addTool("Add Ant");
        antgc.addFakeInstallation("local_ant_1.8.4", "/tmp/fake-ant");
        c.save();

        buildHelloWorld().shouldContainsConsoleOutput(
            "fake ant at /tmp/fake-ant/bin/ant"
        );
    }

    private Build buildHelloWorld() {
        job.configure(new Callable<Object>() {
            public Object call() {
                job.addCreateFileStep("build.xml", resource("echo-helloworld.xml").asText());
                job.addBuildStep(AntBuildStep.class).targets.sendKeys("hello");
                return null;
            }
        });

        return job.queueBuild().shouldSucceed().shouldContainsConsoleOutput("Hello World");
    }
}
