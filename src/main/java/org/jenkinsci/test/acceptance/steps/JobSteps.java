package org.jenkinsci.test.acceptance.steps;

import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.hamcrest.CoreMatchers;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;

import javax.inject.Singleton;

import static org.hamcrest.CoreMatchers.containsString;

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
}
