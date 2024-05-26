package core;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.google.inject.Inject;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.apache.commons.lang3.SystemUtils;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.*;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Before;
import org.junit.Test;

/**
 * Feature: Archive artifacts
 */
@WithPlugins("command-launcher")
public class ArtifactsTest extends AbstractJUnitTest {

    public static final int LARGE_FILE_GB = 3;
    public static final int NO_SMALL_FILES = 200;

    @Inject
    private SlaveController slaveController;
    private FreeStyleJob job;
    private Slave slave;

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        job = jenkins.jobs.create();
        slave = slaveController.install(jenkins).get();
    }

    /**
     * Tests archiving a large file.
     * {@link #LARGE_FILE_GB} GB in size.
     * Assumes there is enough disk space.
     */
    @Test
    public void test_large_file() {
        job.configure();
        job.setLabelExpression(slave.getName());
        if (SystemUtils.IS_OS_WINDOWS) {
            job.addBatchStep("fsutil file createnew " + LARGE_FILE_GB + "GB-%BUILD_NUMBER%-file.txt " + (LARGE_FILE_GB * 1000 * 1000) + "\n" +
                             "dir");
        }
        else {
            job.addShellStep("#!/bin/bash\n" +
                             "dd if=/dev/zero of=" + LARGE_FILE_GB + "GB-${BUILD_NUMBER}-file.txt bs=" + LARGE_FILE_GB + "M count=1000\n" +
                             "ls -l");
        }
        ArtifactArchiver archiver = job.addPublisher(ArtifactArchiver.class);
        archiver.includes("*-file.txt");
        job.save();
        Build build = job.scheduleBuild().waitUntilFinished(240);
        Artifact artifact = build.getArtifact(LARGE_FILE_GB + "GB-" + build.getNumber() + "-file.txt");
        assertThat(artifact, is(notNullValue()));
        artifact.assertThatExists(true);
    }

    /**
     * Tests archiving a number of small files.
     * Creates and archives {@link #NO_SMALL_FILES} files each 1KB in size.
     * Assumes there is enough disk space.
     */
    @Test
    public void test_many_small_files() {
        job.configure();
        job.setLabelExpression(slave.getName());
        if (SystemUtils.IS_OS_WINDOWS) {
            job.addBatchStep("del /F /Q job*.txt\n" + 
                             "for /l %%x in (1, 1, " + NO_SMALL_FILES +") do fsutil file createnew job-%BUILD_NUMBER%-file-%%x.txt 1000\n" +
                             "dir");

        }
        else {
            job.addShellStep("#!/bin/bash\n" +
                    "rm ./job*.txt\n" +
                    "for i in {1.." + NO_SMALL_FILES + "}\n" +
                    "do\n" +
                    " dd if=/dev/zero of=job-${BUILD_NUMBER}-file-$i.txt bs=1k count=1\n" +
                    "done\n" +
                    "ls -l");
        }
        ArtifactArchiver archiver = job.addPublisher(ArtifactArchiver.class);
        archiver.includes("*-file*.txt");
        job.save();
        Build build = job.scheduleBuild().waitUntilFinished();
        List<Artifact> artifacts = build.getArtifacts();
        assertThat(artifacts, is(notNullValue()));
        assertThat("Incorrect number of artifacts", artifacts.size(), is(equalTo(NO_SMALL_FILES)));
    }
}
