package plugins;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.plugins.stageview.StageView;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Test;

/**
 * Base Implementation of the stageview test as a component. Important aspect of this
 * testclass is the correct visualisation depending of stages and builds (matrix). Might
 * needs adaption for future blue ocean
 * @author Boris Dippolter
 */
@WithPlugins({"pipeline-stage-view", "workflow-durable-task-step", "workflow-basic-steps"})
public class StageViewTest extends AbstractJUnitTest {

    public static final String SINGLE_JOB = "stageview_plugin/single_job.txt";
    public static final String MULTI_JOB = "stageview_plugin/multi_job.txt";
    public static final String MULTI_JOB_IRR_NAMES = "stageview_plugin/multi_job_irregularnames.txt";
    public static final String MUTLI_JOB_FAIL = "stageview_plugin/multi_job_fail.txt";
    public static final String MUTLI_JOB_ABORTED = "stageview_plugin/multi_job_aborted.txt";
    public static final String MUTLI_JOB_UNSTABLE = "stageview_plugin/multi_job_unstable.txt";

    public static final String JOB_PATH = "/job/Pipeline-Test/";

    @Test
    public void jobShouldContainStageView() {
        WorkflowJob job = this.createPipelineFromFile(SINGLE_JOB);
        job.startBuild().shouldSucceed();
        job.open();
        StageView stageView = new StageView(job, JOB_PATH);
        assertThat(stageView.getRootElementName().getText(), containsString("Stage View"));
    }

    /**
     * This tests verifies the height of the diplay. The standard height is 11 of the maximum builds dislayed
     * aka Pagination
     */
    @Test
    @WithPlugins("pipeline-stage-view")
    public void multiBuildJobShouldContainCorrectNumberOfJobsBuilt() {
        WorkflowJob job = this.createPipelineFromFile(SINGLE_JOB);
        Build build = null;
        for (int i = 0; i < 8; i++) {
            build = job.startBuild().shouldSucceed();
        }
        assertThat(build, notNullValue());
        job.open();

        StageView stageView = new StageView(job, JOB_PATH);
        assertThat(stageView.getAllStageViewJobs(), hasSize(8)); // as not max display

        for (int i = 0; i < 10; i++) {
            build = job.startBuild().shouldSucceed();
        }
        assertThat(build, notNullValue());
        job.open();
        stageView = new StageView(job, JOB_PATH);
        assertThat(stageView.getAllStageViewJobs(), hasSize(10)); // max diplay is 10
    }

    /**
     * This tests verfies the width of the display. Stageviews have to adapt
     * to new stages with future builds.
     */
    @Test
    @WithPlugins("pipeline-stage-view")
    public void multiBuildJobShouldContainCorrectNumberOfJobsHeadline() {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        String pre = "node {\n";
        String post = "}";
        String singleStage = "stage ('Clone sources'){\n" + "           echo 'cloned'\n" + "    }\n";
        job.script.set("");
        job.sandbox.check();
        job.save();
        Build build = job.startBuild().shouldSucceed();

        for (int i = 0; i < 10; i++) {
            String text = pre + repeatString(singleStage, i + 1) + post;
            job.configure(() -> job.script.set(text));
            build = job.startBuild().shouldSucceed();
        }

        assertThat(build, notNullValue());
        job.open();
        StageView stageView = new StageView(job, JOB_PATH);
        assertThat(stageView.getAllStageViewJobs(), hasSize(10));
        assertThat(stageView.getStageViewHeadlines(), hasSize(10));
    }

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
     * Does check multiple jobs in the stage view. Second part uses long name to verify the display.
     * Note: emptyjob names are not allowed
     */
    @Test
    public void stageViewContainsMultipleStages() {
        WorkflowJob job = this.createPipelineFromFile(MULTI_JOB);
        job.startBuild().shouldSucceed();
        job.open();
        StageView stageView = new StageView(job, JOB_PATH);
        assertThat(stageView.getStageViewHeadlines().get(0).getName(), containsString("Clone sources"));
        assertThat(stageView.getStageViewHeadlines().get(1).getName(), containsString("Build"));

        job = this.createPipelineFromFile(MULTI_JOB_IRR_NAMES);
        job.startBuild().shouldSucceed();
        job.open();
        stageView = new StageView(job, JOB_PATH);
        assertThat(stageView.getStageViewHeadlines().get(0).getName(), containsString("-"));
        assertThat(
                stageView.getStageViewHeadlines().get(1).getName(),
                containsString("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"));
        assertThat(stageView.getStageViewHeadlines().get(2).getName(), containsString(",.-;:_*+#"));
    }

    /**
     * Does check multiple jobs in the stage view. One with a failed, and one with a success.
     */
    @Test
    public void stageViewContainsMultipleStagesWithFail() {
        WorkflowJob job = this.createPipelineFromFile(MUTLI_JOB_FAIL);
        job.startBuild().shouldFail();
        job.open();
        job.getNavigationLinks();
        StageView stageView = new StageView(job, JOB_PATH);
        String firstStage = stageView.getLatestBuild().getStageViewItem(0).toString();
        String secondStage = stageView.getLatestBuild().getStageViewItem(1).toString();
        assertThat(stageView.getLatestBuild().getCssClasses(), containsString("FAILED"));
        assertThat(firstStage, allOf(containsString("ms"), not(containsString("failed"))));
        assertThat(secondStage, containsString("failed"));
    }

    /**
     * Does check multiple jobs in the stage view. One with a unstable, and one with a success. Unstable jobs
     * are represented with yellow color and represented with the css class "UNSTABLE".
     */
    @Test
    public void stageViewContainsMultipleStagesWithUnstable() {
        WorkflowJob job = this.createPipelineFromFile(MUTLI_JOB_UNSTABLE);
        job.startBuild().shouldBeUnstable();
        job.open();
        job.getNavigationLinks();
        StageView stageView = new StageView(job, JOB_PATH);
        String firstStage = stageView.getLatestBuild().getStageViewItem(0).toString();
        assertThat(stageView.getLatestBuild().getCssClasses(), containsString("UNSTABLE"));
        assertThat(firstStage, containsString("ms"));
    }

    /**
     * Does check multiple jobs in the stage view. One with a success, and one with aborted.
     * Aborted jobs are not represented in the satgeview. They are also shown green.
     *
     */
    @Test
    public void stageViewContainsMultipleStagesWithAborted() {
        WorkflowJob job = this.createPipelineFromFile(MUTLI_JOB_ABORTED);
        job.startBuild().shouldAbort();
        job.open();
        job.getNavigationLinks();
        StageView stageView = new StageView(job, JOB_PATH);
        String firstStage = stageView.getLatestBuild().getStageViewItem(0).toString();
        assertThat(stageView.getLatestBuild().getCssClasses(), containsString("ABORTED"));
        assertThat(firstStage, containsString("ms"));
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
                .lines()
                .collect(Collectors.joining("\n"));
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
        return Stream.generate(() -> str).limit(times).collect(Collectors.joining());
    }
}
