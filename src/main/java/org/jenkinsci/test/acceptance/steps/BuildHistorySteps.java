package org.jenkinsci.test.acceptance.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;
import static org.jenkinsci.test.acceptance.cucumber.By2.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class BuildHistorySteps extends AbstractSteps {
    @Then("^the job (should|should not) have build (\\d+)$")
    public void the_job_should_not_have_build(String shouldOrNot, int n) throws Throwable {
        my.job.build(n).open();

        if (shouldOrNot.equals("should")) {
            assertThat(driver, hasContent("Build #" + n));
        } else {
            assertThat(driver, not(hasContent("Build #" + n)));
        }
    }

    @And("^I lock the build$")
    public void I_lock_the_build() throws Throwable {
        my.job.getLastBuild().open();
        find(button("Keep this build forever")).click();
    }
}
