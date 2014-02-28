package org.jenkinsci.test.acceptance.steps;

import cucumber.api.PendingException;
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

    /**
     * These job steps often assume contextual "it" job. This variable captures that.
     */
    Job job;

    @When("^I create a job named \"([^\"]*)\"$")
    public void I_create_a_job_named(String name) throws Throwable {
        //   @job = Jenkins::Job.create_named 'FreeStyle', @base_url, name
        job = jenkins.createJob(FreeStyleJob.class, name);
    }


}
