package org.jenkinsci.test.acceptance.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;

import javax.inject.Singleton;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class JobSteps extends AbstractSteps {
    @When("^I create a job named \"([^\"]*)\"$")
    public void I_create_a_job_named(String name) throws Throwable {
        my.job = jenkins.createJob(FreeStyleJob.class, name);
    }

    @Given("^a job$")
    public void a_job() throws Throwable {
        my.job = jenkins.createJob(FreeStyleJob.class);
    }

    @When("^I configure the job$")
    public void I_configure_the_job() throws Exception {
        visit(my.job.getConfigUrl());
    }

    @And("^I save the job$")
    public void I_save_the_job() throws Exception {
        my.job.save();
    }

    @And("^I build the job$")
    public void I_build_the_job() throws Exception {
        my.job.queueBuild();
    }

    @Then("^console output should contain \"([^\"]*)\"$")
    public void console_output_should_contain(String text) throws Exception {
        Build build = my.job.getLastBuild().waitUntilFinished();
        assertThat(build.getConsole(), containsString(text));
    }

    @And("^I visit the job page$")
    public void I_visit_the_job_page() throws Exception {
        my.job.open();
    }

    @And("^I build (\\d+) jobs$")
    public void I_build_jobs(int n) throws Exception {
        for (int i=0; i<n; i++)
            my.job.queueBuild();
    }

    @Then("^the (\\d+) builds should run concurrently$")
    public void the_builds_should_run_concurrently(int n) throws Throwable {
        // Wait until all jobs have started
        for (int i=0; i<n; i++)
            my.job.build(i+1).waitUntilStarted();

        // then all jobs should be in progress at the same time
        for (int i=0; i<n; i++)
            assertTrue(my.job.build(i + 1).isInProgress());
    }
}
