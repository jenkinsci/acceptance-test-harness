package org.jenkinsci.test.acceptance;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

import javax.inject.Inject;

/**
 * @author Kohsuke Kawaguchi
 */
@StepDefinition
public class SampleSteps {
    @Inject
    Context c;

    @Given("^I eat (.+)$")
    public void eat(String name) {
        c.ate.add(name);
    }

    @Then("^I feel good$")
    public void feelingGreat() {
        System.out.println("Great");
    }
}
