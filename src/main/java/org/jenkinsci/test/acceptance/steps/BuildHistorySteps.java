package org.jenkinsci.test.acceptance.steps;

import cucumber.api.java.en.Then;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class BuildHistorySteps extends AbstractSteps {
    @Then("^the job (should|should not) have build (\\d+)$")
    public void the_job_should_not_have_build(String shouldOrNot, int n) throws Throwable {
        my.job.build(n).open();

        if (shouldOrNot.equals("should")) {
            assertThat(driver.getPageSource(), containsString("Build #" + n));
        } else {
            assertThat(driver.getPageSource(), not(containsString("Build #" + n)));
        }
    }
}
