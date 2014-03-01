package org.jenkinsci.test.acceptance.steps;

import cucumber.api.java.en.Given;
import org.jenkinsci.test.acceptance.controller.JenkinsController;
import org.jenkinsci.test.acceptance.po.JenkinsConfig;

import javax.inject.Inject;

/**
 * @author Kohsuke Kawaguchi
 */
public class JenkinsConfigurationSteps extends AbstractSteps {
    @Inject
    JenkinsController controller;

    @Given("^I add Java version \"([^\"]*)\" with name \"([^\"]*)\" installed automatically to Jenkins config page$")
    public void I_add_Java_version_with_name_installed_automatically_to_Jenkins_config_page(String version, String name) throws Throwable {
        controller.waitForUpdates();
        JenkinsConfig c = my.jenkins.getConfigPage();
        c.enterOracleCredential(System.getenv("ORACLE_LOGIN"), System.getenv("ORACLE_PASSWORD"));
        c.configure();
        c.addTool("JDK");
        c.addJdkAutoInstallation(name,version);
        c.save();
    }
}
