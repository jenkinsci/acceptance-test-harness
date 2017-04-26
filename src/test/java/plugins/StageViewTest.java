package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.BuildHistory;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
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
 * Base Implementation of the stageview test.
 * TODO: Build po for / in progress
 */
@WithPlugins("workflow-aggregator")
public class StageViewTest extends AbstractJUnitTest{


    /**
     * This tests create a simple stage. It checks if after the first build the stage view is now part of the job page.
     * @throws Exception
     */

    @Test
    public void jobShouldContainStageview() throws Exception {
        WorkflowJob job = this.saveWorkflowJobWithFile("stageview_plugin/single_job.txt");
        Build build = job.startBuild().shouldSucceed();
        job.open();
        assertThat(driver.findElement(By.id("pipeline-box")).getText(),containsString("Stage View"));
    }

    /**
     * Does check multiple formattings on the stage view. So far unordered.
     * @throws Exception
     */
    @WithPlugins("workflow-aggregator@1.1")
    @Test
    public void stageViewContainsStageNames() throws Exception {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set("node {\n" +
                "    stage ('Clone sources'){\n" +
                "    echo 'cloned'\n" +
                "    } " +
                "}");

        job.sandbox.check();
        job.save();
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
    @WithPlugins("workflow-aggregator@1.1")
    @Test
    public void stageViewContainsMultipleStages() throws Exception {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set("node {\n" +
                "    stage ('Clone sources'){\n" +
                "    echo 'cloned'\n" +
                "    }\n " +
                "    stage ('build'){\n" +
                "    echo 'build'\n" +
                "    }\n " +
                "}");
        job.sandbox.check();
        job.save();
        Build build = job.startBuild().shouldSucceed();
        job.open();
        WebElement webElement = this.driver.findElement(By.xpath("//*[@id=\"pipeline-box\"]/div/div/table/thead/tr"));
        assertThat(webElement.getText(),containsString("Clone sources"));
        assertThat(webElement.getText(),containsString("build"));
        //assertThat(webElement.getText(),containsString("build"));
    }

    /**
     * Does check multiple jobs in the stage view.
     * @throws Exception
     */
    @WithPlugins("workflow-aggregator@1.1")
    @Test
    public void stageViewContainsMultipleStagesWithFail() throws Exception {
        WorkflowJob job = jenkins.jobs.create(WorkflowJob.class);
        job.script.set("node {\n" +
                "    stage ('Clone sources'){\n" +
                "    echo 'cloned'\n" +
                "    }\n " +
                "    stage ('build'){\n" +
                "    echo 'Meant to fail'\n" +
                "    sh \"exit 1\"\n" +
                "    }\n " +
                "}");
        job.sandbox.check();
        job.save();
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
     * Helper Method for Workflow job egneration. The filename represents
     * the File to be reat as the pipeline definition file
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
