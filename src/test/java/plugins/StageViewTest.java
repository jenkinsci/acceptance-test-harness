package plugins;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.junit.WithPlugins;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.BuildHistory;
import org.jenkinsci.test.acceptance.po.WorkflowJob;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;

/**
 * Created by boris on 17.04.17.
 */
public class StageViewTest extends AbstractJUnitTest{

    /**
     * This tests create a simple stage. It checks if after the first build the stage view is now part of the job page.
     * @throws Exception
     */
    @WithPlugins("workflow-aggregator@1.1")
    @Test
    public void jobShouldContainStageview() throws Exception {
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
        WebElement webElement = this.driver.findElement(By.id("pipeline-box"));
        assertThat(webElement.getText(),containsString("Stage View"));
    }

    /**
     * Does check multiple formattings on the stage view.
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
     * Does check multiple formattings on the stage view.
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

    }







}
