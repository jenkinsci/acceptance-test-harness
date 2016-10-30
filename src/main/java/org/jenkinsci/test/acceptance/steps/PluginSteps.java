package org.jenkinsci.test.acceptance.steps;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.jenkinsci.test.acceptance.update_center.PluginSpec;

import static org.junit.Assume.assumeTrue;

/**
 * @author Kohsuke Kawaguchi
 */
public class PluginSteps extends AbstractSteps {

    @Given("^I have installed the \"([^\"]*)\" plugin( from the update center)?$")
    public void I_have_installed_the_plugin(String name) throws Throwable {
        if (my.jenkins.getPluginManager().installPlugins(new PluginSpec(name))) {
            assumeTrue("This test requires a restartable Jenkins", my.jenkins.canRestart());
            my.jenkins.restart();
        }
    }

    @Then("^plugin page \"([^\"]*)\" should exist$")
    public void plugin_page_should_exist(String name) throws Throwable {
        my.jenkins.visit("plugin/"+name);
    }
}
