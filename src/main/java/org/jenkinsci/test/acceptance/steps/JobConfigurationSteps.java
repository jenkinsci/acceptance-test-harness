package org.jenkinsci.test.acceptance.steps;

import cucumber.api.java.en.And;

/**
 * @author Kohsuke Kawaguchi
 */
public class JobConfigurationSteps extends AbstractSteps {
    @And("^I add a shell build step \"([^\"]*)\"$")
    public void I_add_a_shell_build_step(String shell) throws Throwable {
        my.step = my.job.addShellStep(shell);
    }
}
