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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import org.jenkinsci.test.acceptance.Matcher;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.Bug;
import org.jenkinsci.test.acceptance.junit.Since;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.compress_artifacts.CompressingArtifactManager;
import org.jenkinsci.test.acceptance.po.ArtifactArchiver;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;
import org.jenkinsci.test.acceptance.po.ShellBuildStep;
import org.junit.Test;

@Since("1.532") // No artifact managers before 1.532
public class CompressArtifactsPluginTest extends AbstractJUnitTest {

    private static final String ARTIFACT_NAME = "the.artifact";

    @Test
    @WithPlugins("compress-artifacts")
    public void archive_compressed_artifacts() {
        configureArtifactCompression();
        Build build = generateArtifact();

        assertThat(build.getArtifacts(), hasSize(1));
        build.getArtifact(ARTIFACT_NAME).shouldHaveContent("content");
        assertThat(build, hasCompressedArtifacts());
    }

    @Test
    public void access_uncompressed_artifact_after_plugin_was_installed() {
        Build build = generateArtifact();

        installCompressPlugin();

        // Works after installation
        assertThat(build.getArtifacts(), hasSize(1));
        build.getArtifact(ARTIFACT_NAME).shouldHaveContent("content");
        assertThat(build, not(hasCompressedArtifacts()));

        configureArtifactCompression();

        // Works after configuration
        assertThat(build.getArtifacts(), hasSize(1));
        build.getArtifact(ARTIFACT_NAME).shouldHaveContent("content");
        assertThat(build, not(hasCompressedArtifacts()));
    }

    @Test @Bug("JENKINS-27042")
    @WithPlugins("compress-artifacts")
    public void archiveLargerThan4GInTotal() throws Exception {
        configureArtifactCompression();

        FreeStyleJob job = jenkins.jobs.create();
        job.configure();
        job.addPublisher(ArtifactArchiver.class).includes("*");
        job.addBuildStep(ShellBuildStep.class).command( // Generate archive larger than 4G
                "wget $JENKINS_URL/jnlpJars/jenkins-cli.jar -O stuff.jar; for i in {0..7000}; do cp -l stuff.jar stuff.${i}.jar; done"
        );
        job.save();

        Build build = job.scheduleBuild().waitUntilFinished(10 * 60).shouldSucceed();

        long length = Long.parseLong(jenkins.runScript(
                "new FilePath(Jenkins.instance.getJob('%s').lastBuild.artifactsDir).parent.child('archive.zip').length()",
                job.name
        ));
        assertThat(length, greaterThanOrEqualTo(4L * 1024 * 1024 * 1024));

        build.getArtifact("stuff.jar").open();
        for (int i = 1; i <= 70; i++) {
            build.getArtifact("stuff." + i + "42.jar").open();
        }
    }

    private void installCompressPlugin() {
        @SuppressWarnings("deprecation")
        boolean restart = jenkins.getPluginManager().installPlugins("compress-artifacts");
        if (restart) jenkins.restart();
    }

    private void configureArtifactCompression() {
        JenkinsConfig config = jenkins.getConfigPage();
        config.configure();
        config.addArtifactManager(CompressingArtifactManager.class);
        config.save();
    }

    private Build generateArtifact() {
        FreeStyleJob job = jenkins.jobs.create();
        job.addBuildStep(ShellBuildStep.class).command("echo 'content' > " + ARTIFACT_NAME);
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
