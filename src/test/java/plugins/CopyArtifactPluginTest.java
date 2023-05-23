/*
 * The MIT License
 *
 * Copyright 2014 Sony Mobile Communications Inc.
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

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.copyartifact.CopyArtifactBuildStep;
import org.jenkinsci.test.acceptance.po.*;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Test the Copy Artifacts plugin.
 * @author <a href="tomas.westling@sonymobile.com">Tomas Westling</a>
 */
@WithPlugins({"command-launcher", "copyartifact"})
public class CopyArtifactPluginTest extends AbstractJUnitTest{
    @Inject
    private SlaveController slaveController;
    private FreeStyleJob job1, job2;
    private Slave slave;
    public static int NO_SMALL_FILES = 100;
    public static int NO_FILES_LONG_NAME = 29;

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        job1 = jenkins.jobs.create();
        job2 = jenkins.jobs.create();
        slave = slaveController.install(jenkins).get();
    }

    /**
     * Tests that files can be archived, then copied from another project.
     */
    @Test
    public void TestCopyFiles() {
        setupAndRunJobToCopyFrom("#!/bin/bash\n" +
                "rm ./job1*.txt\n" +
                "for i in {1.." + NO_SMALL_FILES + "}\n" +
                "do\n" +
                "dd if=/dev/zero of=job1-${BUILD_NUMBER}-file-$i.txt bs=1k count=1\n" +
                "done\n" +
                "ls -l");

        List<Artifact> artifacts = setupAndRunJobToCopyTo();

        assertThat(artifacts, is(notNullValue()));
        assertThat(artifacts.size(), is(equalTo(NO_SMALL_FILES)));
    }


    /**
     * Tests that the file archiving and copying works for very long path names.
     */
    @Test
    public void TestCopyFileLongPathName() {
        setupAndRunJobToCopyFrom("#!/bin/bash\n" +
                "rm -r ./job* \n" +
                "text=abcdefghijklmnopqrstuvwxyz+abcdefghijklmnopqrstuvwxyz\n" +
                "filename=job-${BUILD_NUMBER}-$text\n" +
                "for i in {1.." + NO_FILES_LONG_NAME + "}\n" +
                "do\n" +
                "mkdir $filename \n" +
                "cd $filename\n" +
                "pwd\n" +
                "dd if=/dev/zero of=$filename-$i.txt bs=1k count=1\n" +
                "done");

        List<Artifact> artifacts = setupAndRunJobToCopyTo();

        assertThat(artifacts, is(notNullValue()));
        assertThat(artifacts.size(), is(equalTo(NO_FILES_LONG_NAME)));
    }

    /**
     * Sets up and runs a build of the job that will produce the artifacts.
     *
     * @param shellStep the command to run in the shell step.
     */

    private void setupAndRunJobToCopyFrom(String shellStep) {
        job1.configure();
        job1.setLabelExpression(slave.getName());
        job1.addShellStep(shellStep);
        ArtifactArchiver archiver = job1.addPublisher(ArtifactArchiver.class);
        archiver.includes("**/*.txt");
        job1.save();
        job1.scheduleBuild().waitUntilFinished();
    }

    /**
     * Sets up and runs a build of the job that will consume the artifacts.
     *
     */
    private List<Artifact> setupAndRunJobToCopyTo() {
        //Copy the artifacts from the first job, archive them and assert that they are there.
        job2.configure();
        job2.setLabelExpression(slave.getName());
        CopyArtifactBuildStep copyArtifactBuildStep = job2.addBuildStep(CopyArtifactBuildStep.class);
        copyArtifactBuildStep.projectName(job1.name);
        copyArtifactBuildStep.includes("**/*.txt");
        copyArtifactBuildStep.flatten(true);
        ArtifactArchiver archiver = job2.addPublisher(ArtifactArchiver.class);
        archiver.includes("**/*.txt");
        job2.save();
        Build build2 = job2.scheduleBuild().waitUntilFinished();
        return build2.getArtifacts();
    }
}
