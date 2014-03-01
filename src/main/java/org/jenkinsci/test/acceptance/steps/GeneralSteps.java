package org.jenkinsci.test.acceptance.steps;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

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
        check(find(by.xpath("//input[@name='%s']", name)));
    }

    @When("^I click the \"([^\"]*)\" button$")
    public void clickButton(String name) {
        find(by.xpath("//button[text()='%s']", name)).click();
    }

    @Then("^the page should say \"([^\"]*)\"$")
    public void the_page_should_say(String content) throws Exception {
        String url = driver.getCurrentUrl();
        assertThat(url+" doesn't have expected content!", driver, hasContent(content));
    }

    @And("^I wait for (\\d+) seconds$")
    public void I_wait_for_seconds(int n) throws Throwable {
        Thread.sleep(n*1000);
    }

    @Then("^the error description should contain$")
    public void the_error_description_should_contain(String msg) throws Throwable {
        assertThat(
            waitFor(by.css("#error-description pre")).getText(),
            containsString(msg));
    }

    @And("^I close the error dialog$")
    public void I_close_the_error_dialog() throws Throwable {
        find(by.css(".container-close")).click();
    }
}
