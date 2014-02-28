package org.jenkinsci.test.acceptance.steps;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static org.hamcrest.CoreMatchers.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class GeneralSteps extends AbstractSteps {
    @When("^I visit the home page$")
    public void I_visit_the_home_page() throws Exception {
        jenkins.visit("");
    }

    @Then("^the page should say \"([^\"]*)\"$")
    public void the_page_should_say(String content) throws Exception {
        String url = driver.getCurrentUrl();
        assertThat(url+" doesn't have expected content!", driver.getPageSource(), containsString(content));
    }
}
