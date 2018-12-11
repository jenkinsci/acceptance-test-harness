package plugins;

import static org.jenkinsci.test.acceptance.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.*;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.junit.Test;

import hudson.util.VersionNumber;

import org.jenkinsci.test.acceptance.plugins.jacoco.JacocoPublisher;
import org.jenkinsci.test.acceptance.plugins.jacoco.JacocoResultPage;
import org.jenkinsci.test.acceptance.plugins.maven.MavenBuildStep;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;

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
            maven.properties("jacoco.version=" + getJacocoLibraryVersion(), true);

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

    // https://wiki.jenkins-ci.org/display/JENKINS/JaCoCo+Plugin
    // Unfortunately JaCoCo 0.7.5 breaks compatibility to previous binary formats of the jacoco.exec files. The JaCoCo plugin up to version
    // 1.0.19 is based on JaCoCo 0.7.4, thus you cannot use this version with projects which already use JaCoCo 0.7.5 or newer. JaCoCo plugin
    // starting with version 2.0.0 uses JaCoCo 0.7.5 and thus requires also this version to be used in your projects. Please stick to JaCoCo plugin
    // 1.0.19 or lower if you still use JaCoCo 0.7.4 or lower
    private String getJacocoLibraryVersion() {
        boolean old = jenkins.getPlugin("jacoco").getVersion().isOlderThan(new VersionNumber("2.0.0"));
        return old ? OLD_VERSION: NEW_VERSION;
    }

    private static final String OLD_VERSION = "0.7.4.201502262128";
    private static final String NEW_VERSION = "0.8.2";
}
