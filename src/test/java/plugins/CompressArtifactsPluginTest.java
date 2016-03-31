/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jvnet.hudson.test.Issue;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.compress_artifacts.CompressingArtifactManager;
import org.jenkinsci.test.acceptance.plugins.maven.MavenBuild;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Artifact;
import org.jenkinsci.test.acceptance.po.ArtifactArchiver;
import org.jenkinsci.test.acceptance.po.BatchCommandBuildStep;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.ShellBuildStep;
import org.junit.Test;

@Since("1.532") // No artifact managers before 1.532
@WithPlugins("compress-artifacts")
public class CompressArtifactsPluginTest extends AbstractJUnitTest {

    private static final String ARTIFACT_NAME = "the.artifact";

    @Test
    public void archive_compressed_artifacts() {
        CompressingArtifactManager.setup(jenkins);
        Build build = generateArtifact();

        assertThat(build.getArtifacts(), hasSize(1));
        build.getArtifact(ARTIFACT_NAME).shouldHaveContent("content");
        assertThat(build, hasCompressedArtifacts());
    }

    @Test
    public void access_uncompressed_artifact_after_plugin_was_installed() {
        Build build = generateArtifact();

        // Works after installation
        assertThat(build.getArtifacts(), hasSize(1));
        build.getArtifact(ARTIFACT_NAME).shouldHaveContent("content");
        assertThat(build, not(hasCompressedArtifacts()));

        CompressingArtifactManager.setup(jenkins);

        // Works after configuration
        assertThat(build.getArtifacts(), hasSize(1));
        build.getArtifact(ARTIFACT_NAME).shouldHaveContent("content");
        assertThat(build, not(hasCompressedArtifacts()));
    }

    @Test @Issue("JENKINS-27042")
    public void archiveLargerThan4GInTotal() throws Exception {
        CompressingArtifactManager.setup(jenkins);

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.addPublisher(ArtifactArchiver.class).includes("*");
        if (SystemUtils.IS_OS_UNIX) {
            job.addBuildStep(ShellBuildStep.class).command( // Generate archive larger than 4G
                "#!/bin/bash\nwget $JENKINS_URL/jnlpJars/jenkins-cli.jar -O stuff.jar; for i in {0..7000}; do cp -l stuff.jar stuff.${i}.jar; done"
                                            );
        }
        else {
            // On windows although we could create hard links this would exceed the number of hard links you can create for a given file. 
            // and sparse files compress too well. 
            //job.addBuildStep(BatchCommandBuildStep.class).command("for /l %%x in (0, 1, 7000) do fsutil file createnew stuff.%%x.jar 819200");
            job.addBuildStep(BatchCommandBuildStep.class).command("powershell -command \"& { " +
                                            "try { " +
                                            "  Invoke-WebRequest %JENKINS_URL%/jnlpJars/jenkins-cli.jar -OutFile stuff.jar " +
                                            "} catch { " +
                                            "  echo 'download of jenkins-cli.jar failed'; " +
                                            "  exit 2 "+
                                            "}}\n " +
                                            "for /l %%x in (0, 1, 7000) do cp stuff.jar stuff.%%x.jar\n");

        }
        job.save();

        Build build = job.scheduleBuild().waitUntilFinished(10 * 60).shouldSucceed();

        long length = Long.parseLong(jenkins.runScript(
                "new FilePath(Jenkins.instance.getJob('%s').lastBuild.artifactsDir).parent.child('archive.zip').length()",
                job.name
        ));
        assertThat(length, greaterThanOrEqualTo(4L * 1024 * 1024 * 1024));
    }

    @Test @Issue("JENKINS-27558")
    @WithPlugins("maven-plugin")
    public void archiveMavenProject() {
        MavenInstallation.installSomeMaven(jenkins);

        MavenModuleSet mp = jenkins.jobs.create(MavenModuleSet.class);
        mp.configure();
        mp.copyDir(resource("/maven_plugin/multimodule/"));

        mp.goals.set("clean package -B -DskipTests=true");

        mp.addPublisher(ArtifactArchiver.class).includes("module_a/**/*");
        mp.save();

        MavenBuild raw = mp.startBuild().shouldSucceed().as(MavenBuild.class);

        CompressingArtifactManager.setup(jenkins);

        MavenBuild compressed = mp.startBuild().shouldSucceed().as(MavenBuild.class);

        compareArtifacts(raw, compressed);
        compareArtifacts(raw.module("gid$root"), compressed.module("gid$root"));
        compareArtifacts(raw.module("gid$module_a"), compressed.module("gid$module_a"));
        compareArtifacts(raw.module("gid$module_b"), compressed.module("gid$module_b"));
    }

    private void compareArtifacts(Build lhs, Build rhs) {
        final List<Artifact> lhsArtifacts = lhs.getArtifacts();
        for (Artifact ra: lhsArtifacts) {
            String rap = ra.getRelativePath();
            assertEquals(rap, rhs.getArtifact(rap).getRelativePath());
        }
        assertEquals("Artifacts differs", lhsArtifacts.size(), rhs.getArtifacts().size());
        assertThat("No artifacts", lhsArtifacts.size(), greaterThan(0));
    }

    private Build generateArtifact() {
        FreeStyleJob job = jenkins.jobs.create();
        if (SystemUtils.IS_OS_UNIX) {
            job.addBuildStep(ShellBuildStep.class).command("echo 'content' > " + ARTIFACT_NAME);
        }
        else {
            job.addBuildStep(BatchCommandBuildStep.class).command("echo 'content' > " + ARTIFACT_NAME);
        }
        job.addPublisher(ArtifactArchiver.class).includes(ARTIFACT_NAME);
        job.save();

        return job.startBuild().shouldSucceed();
    }

    private Matcher<Build> hasCompressedArtifacts() {
        return new Matcher<Build>("Build has compressed artifacts") {
            @Override
            public boolean matchesSafely(Build build) {
                final String script = "!Jenkins.instance.rootPath.child('jobs/%s/builds/%s/archive/').exists()";
                String ret = jenkins.runScript(script, build.job.name, build.getNumber());
                return equalTo("true").matches(ret);
            }
        };
    }
}
