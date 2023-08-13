package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.jenkinsci.test.acceptance.Matchers.hasAction;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.maven.MavenInstallation;
import org.jenkinsci.test.acceptance.plugins.workflow_multibranch.GithubBranchSource;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.jenkinsci.test.acceptance.po.WorkflowMultiBranchJob;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests a multibranch pipeline flow
 */
@WithPlugins({"git", "javadoc@1.4", "workflow-basic-steps", "workflow-durable-task-step", "workflow-multibranch", "github-branch-source@2.5.5"})
public class WorkflowMultibranchTest extends AbstractJUnitTest {

    @Before
    public void setup() {
        MavenInstallation.installMaven(jenkins, "M3", "3.9.4");
    }

    @Ignore("cannot run quickly as anonymous due to github rate limiting")
    @Test
    public void testMultibranchPipeline() {
        final WorkflowMultiBranchJob multibranchJob = jenkins.jobs.create(WorkflowMultiBranchJob.class);
        this.configureJobWithGithubBranchSource(multibranchJob);
        multibranchJob.save();
        multibranchJob.waitForBranchIndexingFinished(20);

        this.assertBranchIndexing(multibranchJob);

        final WorkflowJob successJob = multibranchJob.getJob("jenkinsfile_success");
        final WorkflowJob failureJob = multibranchJob.getJob("jenkinsfile_failure");
        this.assertExistAndRun(successJob, true);
        this.assertExistAndRun(failureJob, false);

        multibranchJob.open();
        multibranchJob.reIndex();
        multibranchJob.waitForBranchIndexingFinished(20);

        this.assertExistAndRun(successJob, true);
        this.assertExistAndRun(failureJob, false);
    }

    private void configureJobWithGithubBranchSource(final WorkflowMultiBranchJob job) {
        final GithubBranchSource ghBranchSource = job.addBranchSource(GithubBranchSource.class);
        ghBranchSource.repoUrl("https://github.com/varyvoltest/maven-basic.git");
    }

    private void assertBranchIndexing(final WorkflowMultiBranchJob job) {
        assertThat(job, anyOf(/* 1.x */hasAction("Branch Indexing"), /* 2.x */hasAction("Scan Repository"), /* 2.1.0 */hasAction("Scan Repository Now")));

        final String branchIndexingLog = job.getBranchIndexingLog();

        assertThat(branchIndexingLog, containsString("Scheduled build for branch: jenkinsfile_failure"));
        assertThat(branchIndexingLog, containsString("Scheduled build for branch: jenkinsfile_success"));
        assertThat(branchIndexingLog, not(containsString("Scheduled build for branch: master")));
        assertThat(branchIndexingLog, containsString("3 branches were processed"));
    }

    private void assertExistAndRun(final WorkflowJob job, final boolean withSuccess) {
        try {
            IOUtils.toString(job.url("").openStream(), StandardCharsets.UTF_8);
        } catch (final IOException ex) {
            Assert.fail("Job has not been created");
        }

        final Build.Result expectedResult = (withSuccess) ? Build.Result.SUCCESS : Build.Result.FAILURE;
        assertEquals(job.getNextBuildNumber(), 2);
        assertEquals(job.build(1).getResult(), expectedResult.name());
    }

}
