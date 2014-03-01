package org.jenkinsci.test.acceptance.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import hudson.util.VersionNumber;
import org.jenkinsci.test.acceptance.po.StringParameter;

import javax.inject.Inject;

import static org.jenkinsci.test.acceptance.cucumber.By2.*;

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
        find(xpath("//input[@name='%s']",name)).sendKeys(String.valueOf(n));
    }
}
