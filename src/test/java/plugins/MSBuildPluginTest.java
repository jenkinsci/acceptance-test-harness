package plugins;

import static org.junit.Assert.assertThat;

import java.io.IOException;

import javax.inject.Named;

import org.hamcrest.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.TestActivation;
import org.jenkinsci.test.acceptance.junit.WithOS;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.msbuild.MSBuildInstallation;
import org.jenkinsci.test.acceptance.msbuild.MSBuildStep;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;

import com.google.inject.Inject;

/**
 * This test is designed to be run on a Windows machine with MSBuild installed
 * 
 * In order for the test to know where the MSBuild installation dir is, an
 * environment variable must be set. i.e:
 * 
 *      MSBUILD_EXE = C:\Windows\Microsoft.NET\Framework\v2.0.50727\MSBuild.exe
 * 
 * Tests will be skipped if:
 *      - The environment variable is not set 
 *      - The test is not running on a Windows machine
 *      - MSBUild.exe is not present in the specified location
 */
@WithPlugins("msbuild")
@TestActivation({"MSBUILD_EXE"})
@WithOS(os = {WithOS.OS.WINDOWS})
public class MSBuildPluginTest extends AbstractJUnitTest {

    @Inject
    @Named("MSBuildPluginTest.MSBUILD_EXE") 
    private String MS_BUILD_EXECUTABLE;
    
    private static final String MSBUILD_NAME = "MSBuildInstallation";

    /**
     * Builds a FreeStyle job with a MSBuild step with the configuration passed as parameter
     * 
     * @param workspacePath The workspace where the project is located
     * @param buildFile The build file (.proj or .sln)
     * @param cmdArguments the command line arguments to execute. Can be null.
     * @return a job
     * @throws IllegalArgumentException if workspacePath or buildFile are null
     */
    private FreeStyleJob msBuildJob(String workspacePath, String buildFile, String cmdArguments) {
        if (workspacePath != null && buildFile != null) {
            FreeStyleJob job = jenkins.jobs.create(FreeStyleJob.class);
            job.copyDir(resource(workspacePath));
            MSBuildStep msBuildStep = job.addBuildStep(MSBuildStep.class);
            msBuildStep.setMSBuildName(MSBUILD_NAME).setMSBuildFile(buildFile);
            if (cmdArguments != null && !cmdArguments.isEmpty()) {
                msBuildStep.setCmdLineArgs(cmdArguments);
            }
            job.save();
            return job;
        } else {
            throw new IllegalArgumentException("Workspace and buildFile must be different from null.");
        }
    }
    
    @Before
    public void setUp() {
         try {
             Runtime.getRuntime().exec(MS_BUILD_EXECUTABLE);
         } catch (IOException e){
             throw new AssumptionViolatedException("Test will be skipped. MSBuild executable not reachable: " + MS_BUILD_EXECUTABLE);
         }

        // Configure MSBuild
        jenkins.configure();
        // Show Add MsBuild option
        MSBuildInstallation msbuild = jenkins.getConfigPage().addTool(MSBuildInstallation.class);
        msbuild.installedIn(MS_BUILD_EXECUTABLE).name.set(MSBUILD_NAME);
        jenkins.save();
    }

    @Test
    public void buildProjNoCmdLineArgumentsTest() {
        FreeStyleJob job = msBuildJob("/msbuild_plugin/projProject/", "project.proj", null);

        // Job should run successfully
        Build b = job.scheduleBuild();
        b.shouldSucceed();
        assertThat(b.getConsole(), Matchers.containsString("Build succeeded."));
        assertThat(b.getConsole(), Matchers.not(Matchers.containsString("Done building project \"project.proj\".")));
    }

    @Test
    public void buildProjCmdLineArgumentsTest() {
        FreeStyleJob job = msBuildJob("/msbuild_plugin/projProject/", "project.proj", "/verbosity:detailed");

        // Job should run successfully
        Build b = job.scheduleBuild();
        b.shouldSucceed();
        String console = b.getConsole();
        assertThat(console, Matchers.containsString("Build succeeded."));
        assertThat(console, Matchers.containsString("Done Building Project"));
    }
    
    @Test
    public void buildSlnNoCmdLineArgumentsTest() {
        FreeStyleJob job = msBuildJob("/msbuild_plugin/slnProject/", "project.sln", null);
        
        // Job should run successfully
        Build b = job.scheduleBuild();
        b.shouldSucceed();
        assertThat(b.getConsole(), Matchers.containsString("Build succeeded."));
    }
    
    @Test
    public void buildSlnCmdLineArgumentsTest() {
        FreeStyleJob job = msBuildJob("/msbuild_plugin/slnProject/", "project.sln", "/verbosity:detailed");
        
        // Job should run successfully
        Build b = job.scheduleBuild();
        b.shouldSucceed();
        String console = b.getConsole();
        assertThat(console, Matchers.containsString("Build succeeded."));
        assertThat(console, Matchers.containsString("Done Building Project"));
    }
}
