package plugins;

import javafx.scene.paint.Color;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.*;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;

/**
 * Created by boris on 17.04.17.
 * Base Implementation of the stageview test as a component. Important aspect of this testclass is the correct
 * visualisation depending of stages and builds (matrix).
 *
 * TODO: Build po for / in progress
 */
@WithPlugins("workflow-aggregator")
public class StageViewTest extends AbstractJUnitTest{


    public static final String SINGLE_JOB = "stageview_plugin/single_job.txt";
    public static final String MULTI_JOB = "stageview_plugin/multi_job.txt";
    public static final String MUTLI_JOB_FAIL = "stageview_plugin/multi_job_fail.txt";

    private PageObject context;
    private String path;
    private StageView stageView;

    @Before
    public void before() {
        stageView = new StageView(context, path);
    }
    
    /**
     * This tests create a simple stage. It checks if after the first build the stage view is now part of the job page.
     * @throws Exception
     */
    @Test
    public void jobShouldContainStageview() throws Exception {
        WorkflowJob job = this.saveWorkflowJobWithFile(SINGLE_JOB);
        Build build = job.startBuild().shouldSucceed();
        job.open();
        assertThat(stageView.getRootElementName().getText(),containsString("Stage View"));
    }

    /**
     * Test validates against the current build number. Every row(aka build) contains the correct build number.
     */
    public void jobNumberShouldbeCorrect() {
            WorkflowJob job = this.saveWorkflowJobWithFile(SINGLE_JOB);
            Build build = job.startBuild().shouldFail();
            job.open();
            job.getNavigationLinks();
            WebElement webElement = this.driver.findElement(By.xpath("//*[@id=\"pipeline-box\"]/div/div/table/tbody[2]/tr[1]/td[1]/div/div/div[1]/span"));
            assertThat(webElement.getText(),containsString(String.valueOf(build.getNumber());
    }

    /**
     * Does check multiple formattings on the stage view. So far unordered.
     * @throws Exception
     */
    @Test
    public void stageViewContainsStageNames() throws Exception {
        WorkflowJob job = this.saveWorkflowJobWithFile(SINGLE_JOB);
        Build build = job.startBuild().shouldSucceed();
        job.open();
        WebElement webElement = this.driver.findElement(By.xpath("//*[@id=\"pipeline-box\"]/div/div/table/thead/tr"));
        assertThat(webElement.getText(),containsString("Clone sources"));
        assertThat(driver.getPageSource(),containsString("SUCCESS"));
        assertThat(driver.getPageSource(),containsString("Average stage times"));
    }

    /**
     * Does check multiple jobs in the stage view.
     * @throws Exception
     */
    @Test
    public void stageViewContainsMultipleStages() throws Exception {
        WorkflowJob job = this.saveWorkflowJobWithFile(MULTI_JOB);
        Build build = job.startBuild().shouldSucceed();
        job.open();
        WebElement webElement = this.driver.findElement(By.xpath("//*[@id=\"pipeline-box\"]/div/div/table/thead/tr"));
        assertThat(webElement.getText(),containsString("Clone sources"));
        assertThat(webElement.getText(),containsString("build"));
        //assertThat(webElement.getText(),containsString("build"));
    }

    /**
     * Does check multiple jobs in the stage view. One with a failed, and one with a success.
     * //TODO check the color of these jobs
     * @throws Exception
     */
    @Test
    public void stageViewContainsMultipleStagesWithFail() throws Exception {
        WorkflowJob job = this.saveWorkflowJobWithFile(MUTLI_JOB_FAIL);
        Build build = job.startBuild().shouldFail();
        job.open();
        job.getNavigationLinks();
        WebElement webElement = this.driver.findElement(By.xpath("//*[@id=\"pipeline-box\"]/div/div/table/thead/tr"));
        assertThat(driver.getPageSource(),containsString("FAILED"));
        assertThat(driver.getPageSource(),containsString("SUCCESS"));
        //order check
        //gesamtergebnis
        //refactor
    }

    /**
     * Helper method to convenient located a file int he ressource folder
     * @param fileName the naame of the file including path
     * @return return the file content as a String
     */
    private String readFromRessourceFolder(String fileName) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        return  new BufferedReader(new InputStreamReader(classloader.getResourceAsStream(fileName)))
                .lines().collect(Collectors.joining("\n"));
    }


    /**
     * Helper Method for Workflow job generation. The filename represents
     * the File to be read as the pipeline definition file
     * @param fileName the naame of the file including path
     * @return return the newly generated workflow job with a defined pipeline
     */
    private WorkflowJob saveWorkflowJobWithFile(String fileName) {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set(readFromRessourceFolder(fileName));
        job.sandbox.check();
        job.save();
        return job;
    }

}
