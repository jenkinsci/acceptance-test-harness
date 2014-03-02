package org.jenkinsci.test.acceptance.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;

/**
 * @author Kohsuke Kawaguchi
 */
public class PluginSteps extends AbstractSteps {

    @Given("^I have installed the \"([^\"]*)\" plugin( from the update center)?$")
    public void I_have_installed_the_plugin(String name) throws Throwable {
        my.jenkins.getPluginManager().installPlugin(name);
    }

    @Then("^plugin page \"([^\"]*)\" should exist$")
    public void plugin_page_should_exist(String name) throws Throwable {
        my.jenkins.visit("plugin/"+name);
    }
}
