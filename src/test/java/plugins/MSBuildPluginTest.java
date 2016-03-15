package plugins;

import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Native;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.msbuild.MSBuildStep;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

/**
 * This test is designed to be run on a Windows machine with MSBuild installed and in the PATH
 * 
 * Tests will be skipped if:
 *      - MSBUild.exe is in the path. This implies that machine running Jenkins is windows.
 *      - The test and Jenkins are being run on different machines
 */
@WithPlugins("msbuild")
@Native({"MSBuild"})
public class MSBuildPluginTest extends AbstractJUnitTest {

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
            msBuildStep.setMSBuildFile(buildFile);
            if (cmdArguments != null && !cmdArguments.isEmpty()) {
                msBuildStep.setCmdLineArgs(cmdArguments);
            }
            job.save();
            return job;
        } else {
            throw new IllegalArgumentException("Workspace and buildFile must be different from null.");
        }
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
