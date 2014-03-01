package org.jenkinsci.test.acceptance.steps;

import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import hudson.util.VersionNumber;
import org.jenkinsci.test.acceptance.po.ArtifactArchiver;
import org.jenkinsci.test.acceptance.po.StringParameter;

import javax.inject.Inject;

/**
 * @author Kohsuke Kawaguchi
 */
public class JobConfigurationSteps extends AbstractSteps {
    @Inject
    GeneralSteps generalsteps;

    @And("^I add a shell build step \"([^\"]*)\"$")
    public void I_add_a_shell_build_step(String shell) throws Exception {
        my.step = my.job.addShellStep(shell);
    }

    @And("^I enable concurrent builds$")
    public void I_enable_concurrent_builds() throws Exception {
        generalsteps.checkTheCheckbox("_.concurrentBuild");
    }

    @And("^I add a string parameter \"([^\"]*)\"$")
    public void I_add_a_string_parameter(String name) throws Exception {
        StringParameter p = my.job.addParameter(StringParameter.class);
        p.setName(name);
    }

    @When("^I disable the job$")
    public void I_disable_the_job() throws Throwable {
        my.job.configure();
        my.job.disable();
        my.job.save();
    }

    @And("^I set (\\d+) builds? to keep$")
    public void I_set_builds_to_keep(int n) throws Throwable {
        check("logrotate");
        String name = jenkins.getVersion().compareTo(new VersionNumber("1.503"))<0 ? "logrotate_nums" : "_.numToKeepStr";
        find(by.xpath("//input[@name='%s']",name)).sendKeys(String.valueOf(n));
    }

    @And("^I add always fail build step$")
    public void I_add_always_fail_build_step() throws Throwable {
        my.job.addShellStep("exit 1");
    }

    @When("^I add a shell build step \"([^\"]*)\" in the job configuration$")
    public void I_add_a_shell_build_step_in_the_job_configuration(String shell) throws Throwable {
        my.job.configure();
        my.job.addShellStep(shell);
        my.job.save();
    }

    @When("^I add a shell build step in the job configuration$")
    public void I_add_a_shell_build_step_in_the_job_configuration2(String shell) throws Throwable {
        I_add_a_shell_build_step_in_the_job_configuration(shell);
    }

    @And("^I set artifact \"([^\"]*)\" to archive$")
    public void I_set_artifact_to_archive(String artifacts) throws Throwable {
        my.artifactArchiver = my.job.addPublisher(ArtifactArchiver.class).includes(artifacts);
    }

    @And("^I set artifact \"([^\"]*)\" to archive in the job configuration$")
    public void I_set_artifact_to_archive_in_the_job_configuration(String includes) throws Throwable {
        my.job.configure();
        my.artifactArchiver = my.job.addPublisher(ArtifactArchiver.class).includes(includes);
        my.job.save();
    }

    @And("^I set artifact \"([^\"]*)\" to archive and exclude \"([^\"]*)\" in the job configuration$")
    public void I_set_artifact_to_archive_and_exclude_in_the_job_configuration(String include, String exclude) throws Throwable {
        my.job.configure();
        my.artifactArchiver = my.job.addPublisher(ArtifactArchiver.class)
            .includes(include).excludes(exclude);
        my.job.save();
    }

    @And("^I want to keep only the latest successful artifacts$")
    public void I_want_to_keep_only_the_latest_successful_artifacts() throws Throwable {
        my.artifactArchiver.latestOnly(true);
    }

    @And("^I schedule job to run periodically at \"([^\"]*)\"$")
    public void I_schedule_job_to_run_periodically_at(String cron) throws Throwable {
        check("hudson-triggers-TimerTrigger");
        find(by.path("/hudson-triggers-TimerTrigger/spec")).sendKeys(cron);
    }

    @And("^I use \"([^\"]*)\" as custom workspace$")
    public void I_use_as_custom_workspace(String ws) throws Throwable {
        my.job.useCustomWorkspace(ws);
    }
}
