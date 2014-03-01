package org.jenkinsci.test.acceptance.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import org.jenkinsci.test.acceptance.cucumber.Should;

/**
 * @author Kohsuke Kawaguchi
 */
public class BuildHistorySteps extends AbstractSteps {
    @Then("^the job (should|should not) have build (\\d+)$")
    public void the_job_should_not_have_build(Should should, int n) throws Throwable {
        my.job.build(n).open();
        assertThat(driver, should.haveContent("Build #" + n));
    }

    @And("^I lock the build$")
    public void I_lock_the_build() throws Throwable {
        my.job.getLastBuild().open();
        find(by.button("Keep this build forever")).click();
    }
}
