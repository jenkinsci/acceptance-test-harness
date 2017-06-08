package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.jenkinsci.test.acceptance.po.stageview.StageView;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by boris on 17.04.17.
 * Base Implementation of the stageview test as a component. Important aspect of this
 * testclass is the correct visualisation depending of stages and builds (matrix). Might
 * needs adaption for future blue ocean
 */
@WithPlugins("workflow-aggregator")
public class StageViewTest extends AbstractJUnitTest {


    public static final String SINGLE_JOB = "stageview_plugin/single_job.txt";
    public static final String MULTI_JOB = "stageview_plugin/multi_job.txt";
    public static final String MUTLI_JOB_FAIL = "stageview_plugin/multi_job_fail.txt";
    public static final String MUTLI_JOB_ABORTED = "stageview_plugin/multi_job_aborted.txt";
    public static final String MUTLI_JOB_UNSTABLE = "stageview_plugin/multi_job_unstable.txt";

    public static final String JOB_PATH = "/job/Pipeline-Test/";

    /**
     * This tests create a simple stage. It checks if after the first build the stage view is now part of the job page.
     *
     * @throws Exception
     */
    @Test
    public void jobShouldContainStageview() throws Exception {
        WorkflowJob job = this.createPipelineFromFile(SINGLE_JOB);
        Build build = job.startBuild().shouldSucceed();
        job.open();
        StageView stageView = new StageView(job, JOB_PATH);
        assertThat(stageView.getRootElementName().getText(), containsString("Stage View"));
    }

    /**
     * This tests verfies the hieght of the diplay. The standard height is 11 of the maximum builds dislayed.
     */
    @Test
    public void multiBuildJobShouldContainCorrectNumberOfJobsBuilt() {
        WorkflowJob job = this.createPipelineFromFile(SINGLE_JOB);
        Build build = null;
        for (int i = 0; i < 8; i++) {
            build = job.startBuild().shouldSucceed();
        }
        assertThat(build, notNullValue());
        job.open();

        StageView stageView = new StageView(job, JOB_PATH);
        assertThat(stageView.getAllStageViewJobs(), hasSize(8)); //as not max display

        for (int i = 0; i < 10; i++) {
            build = job.startBuild().shouldSucceed();
        }
        assertThat(build, notNullValue());
        job.open();
        stageView = new StageView(job, JOB_PATH);
        assertThat(stageView.getAllStageViewJobs(), hasSize(11));//max diplay is 11
    }

    /**
     * This tests verfies the width of the display. Stageviews have to adapt
     * to new stages with future builds.
     */
    @Test
    public void multiBuildJobShouldContainCorrectNumberOfJobsHeadline() {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        String pre = new String("node {\n");
        String post = new String("}");
        String singleStage = new String("stage ('Clone sources'){\n" +
                "           echo 'cloned'\n" +
                "    }\n");
        job.script.set("");
        job.sandbox.check();
        job.save();
        Build build = null;
        build = job.startBuild().shouldSucceed();

        for (int i = 0; i < 10; i++) {
            final StringBuilder stageBuilder2 = new StringBuilder();
            stageBuilder2.append(pre);
            stageBuilder2.append(this.repeatString(singleStage, i + 1));
            stageBuilder2.append(post);
            job.edit(() -> {
                job.script.set(stageBuilder2.toString());
            });
            build = job.startBuild().shouldSucceed();
        }

        assertThat(build, notNullValue());
        job.open();
        StageView stageView = new StageView(job, JOB_PATH);
        assertThat(stageView.getAllStageViewJobs(), hasSize(11));
        assertThat(stageView.getStageViewHeadlines(), hasSize(10));

    }

    /**
     * Test validates against the current build number. Every row(aka build) contains the correct build number.
     */
    @Test
    public void jobNumberShouldbeCorrect() {
        WorkflowJob job = this.createPipelineFromFile(SINGLE_JOB);
        Build build = job.startBuild().shouldSucceed();
        job.open();
        job.getNavigationLinks();
        StageView stageView = new StageView(job, JOB_PATH);
        assertThat(stageView.getLatestBuild().getBuildNo(), containsString(String.valueOf(build.getNumber())));
    }

    /**
     * Does check multiple jobs in the stage view.
     *
     * @throws Exception
     */
    @Test
    public void stageViewContainsMultipleStages() throws Exception {
        WorkflowJob job = this.createPipelineFromFile(MULTI_JOB);
        Build build = job.startBuild().shouldSucceed();
        job.open();
        StageView stageView = new StageView(job, JOB_PATH);
        assertThat(stageView.getStageViewHeadlines().get(0).getName(), containsString("Clone sources"));
        assertThat(stageView.getStageViewHeadlines().get(1).getName(), containsString("Build"));
    }

    /**
     * Does check multiple jobs in the stage view. One with a failed, and one with a success.
     *
     * @throws Exception
     */
    @Test
    public void stageViewContainsMultipleStagesWithFail() throws Exception {
        WorkflowJob job = this.createPipelineFromFile(MUTLI_JOB_FAIL);
        Build build = job.startBuild().shouldFail();
        job.open();
        job.getNavigationLinks();
        StageView stageView = new StageView(job, JOB_PATH);
        String firstJob = stageView.getLatestBuild().getStageViewItem(0).toString();
        String secondJob = stageView.getLatestBuild().getStageViewItem(1).toString();
        assertThat(stageView.getLatestBuild().getCssClasses(), containsString("FAILED"));
        assertThat(firstJob, containsString("ms"));
        assertThat(secondJob, containsString("failed"));
    }

    /**
     * Does check multiple jobs in the stage view. One with a unstable, and one with a success. Unstable jobs
     * are represented with yellow color and represented with the css class "UNSTABLE".
     *
     * @throws Exception
     */
    @Test
    public void stageViewContainsMultipleStagesWithUnstable() throws Exception {
        WorkflowJob job = this.createPipelineFromFile(MUTLI_JOB_UNSTABLE);
        Build build = job.startBuild().shouldBeUnstable();
        job.open();
        job.getNavigationLinks();
        StageView stageView = new StageView(job, JOB_PATH);
        String firstJob = stageView.getLatestBuild().getStageViewItem(0).toString();
        String secondJob = stageView.getLatestBuild().getStageViewItem(1).toString();
        assertThat(stageView.getLatestBuild().getCssClasses(), containsString("UNSTABLE"));
        assertThat(firstJob, containsString("ms"));
    }

    /**
     * Does check multiple jobs in the stage view. One with a success, and one with aborted.
     * Aborted jobs are not represented in the satgeview. They are also shown green.
     *
     * @throws Exception
     */
    @Test
    public void stageViewContainsMultipleStagesWithAborted() throws Exception {
        WorkflowJob job = this.createPipelineFromFile(MUTLI_JOB_ABORTED);
        Build build = job.startBuild().shouldAbort();
        job.open();
        job.getNavigationLinks();
        StageView stageView = new StageView(job, JOB_PATH);
        String firstJob = stageView.getLatestBuild().getStageViewItem(0).toString();
        String secondJob = stageView.getLatestBuild().getStageViewItem(1).toString();
        assertThat(stageView.getLatestBuild().getCssClasses(), containsString("ABORTED"));
        assertThat(firstJob, containsString("ms"));
    }

    /**
     * Helper method to convenient located a file int he ressource folder
     *
     * @param fileName the naame of the file including path
     * @return return the file content as a String
     */
    private String readFromRessourceFolder(String fileName) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        return new BufferedReader(new InputStreamReader(classloader.getResourceAsStream(fileName)))
                .lines().collect(joining("\n"));
    }


    /**
     * Helper Method for Workflow job generation. The filename represents
     * the File to be read as the pipeline definition file
     *
     * @param fileName the naame of the file including path
     * @return return the newly generated workflow job with a defined pipeline
     */
    private WorkflowJob createPipelineFromFile(String fileName) {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(readFromRessourceFolder(fileName));
        job.sandbox.check();
        job.save();
        return job;
    }

    /**
     * Helper method to generate repeated Strings
     *
     * @param str   The String to repeate
     * @param times n times
     * @return the repeated String
     */
    private String repeatString(String str, int times) {
        return Stream.generate(() -> str).limit(times).collect(joining());
    }


}
