package plugins;

import static org.hamcrest.MatcherAssert.*;
import static org.jenkinsci.test.acceptance.Matchers.*;
import static org.junit.Assert.assertEquals;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.jacoco.JacocoPublisher;
import org.jenkinsci.test.acceptance.plugins.jacoco.JacocoResultPage;
import org.jenkinsci.test.acceptance.plugins.maven.MavenBuildStep;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.junit.Test;

/**
 * Checks the successfully execution of jacoco coverage reports.
 */
@WithPlugins("jacoco")
public class JacocoPluginTest extends AbstractJUnitTest {

    @Test
    public void checkSuccessfulExecutionAndsummary() {
        MavenInstallation.installSomeMaven(jenkins);
        FreeStyleJob job = jenkins.jobs.create(); {
            job.copyDir(resource("/jacoco/test"));

            MavenBuildStep maven = job.addBuildStep(MavenBuildStep.class);
            maven.targets.set("clean package -B");

            JacocoPublisher publisher = job.addPublisher(JacocoPublisher.class);
            publisher.changeBuildStatus.check();
        }
        job.save();

        Build build = job.startBuild().waitUntilFinished().shouldSucceed();
        assertThat(build, hasAction("Coverage Report"));

        JacocoResultPage total = new JacocoResultPage(build);
        assertEquals(45.45, total.instructionCoverage(), 0.01);
        assertEquals(50.0, total.branchCoverage(), 0.01);
        assertEquals(33.33, total.complexityScore(), 0.01);
        assertEquals(50.0, total.lineCoverage(), 0.01);
        assertEquals(50.0, total.methodCoverage(), 0.01);
        assertEquals(100.0, total.classCoverage(), 0.01);

        JacocoResultPage pkg = new JacocoResultPage(total, "(default)/App");
        assertEquals(45.45, pkg.instructionCoverage(), 0.01);
        assertEquals(50.0, pkg.branchCoverage(), 0.01);
        assertEquals(33.33, pkg.complexityScore(), 0.01);
        assertEquals(50.0, pkg.lineCoverage(), 0.01);
        assertEquals(50.0, pkg.methodCoverage(), 0.01);
        assertEquals(100.0, pkg.classCoverage(), 0.01);
    }
}
