package org.jenkinsci.test.acceptance.steps;

import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.jenkinsci.test.acceptance.po.FreeStyleJob;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.Job;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Kohsuke Kawaguchi
 */
@Singleton
public class JobSteps extends AbstractSteps {


    @When("^I create a job named \"([^\"]*)\"$")
    public void I_create_a_job_named(String name) throws Throwable {
        //   @job = Jenkins::Job.create_named 'FreeStyle', @base_url, name
        my.job = jenkins.createJob(FreeStyleJob.class, name);
    }


    @Given("^a job$")
    public void a_job() throws Throwable {
        // Express the Regexp above with the code you wish you had
        my.job = jenkins.createJob(FreeStyleJob.class);
    }

    @When("^I configure the job$")
    public void I_configure_the_job() throws Throwable {
        visit(my.job.getConfigUrl());
    }
}
