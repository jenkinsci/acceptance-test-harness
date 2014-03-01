package org.jenkinsci.test.acceptance.steps;

import cucumber.api.PendingException;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.cucumber.By2.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class GeneralSteps extends AbstractSteps {
    @When("^I visit the home page$")
    public void I_visit_the_home_page() throws Exception {
        jenkins.visit("");
    }

    @When("^I check the \"([^\"]*)\" checkbox$")
    public void checkTheCheckbox(String name) {
        check(find(xpath("//input[@name='%s']", name)));
    }

    @When("^I click the \"([^\"]*)\" button$")
    public void clickButton(String name) {
        find(xpath("//button[text()='%s']", name)).click();
    }

    @Then("^the page should say \"([^\"]*)\"$")
    public void the_page_should_say(String content) throws Exception {
        String url = driver.getCurrentUrl();
        assertThat(url+" doesn't have expected content!", driver.getPageSource(), containsString(content));
    }
}
