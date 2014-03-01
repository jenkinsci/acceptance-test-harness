package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.WebElement;

import java.net.URL;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class JenkinsConfig extends PageObject {
    public final Jenkins jenkins;

    public JenkinsConfig(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url("configure"));
        this.jenkins = jenkins;
    }

    public void enterOracleCredential(String login, String password) {
        visit("descriptorByName/hudson.tools.JDKInstaller/enterCredential");
        find(by.input("username")).sendKeys(login);
        find(by.input("password")).sendKeys(password);
        clickButton("OK");
        clickButton("Close");
    }

    @Override
    public URL getConfigUrl() {
        return url;
    }

    public void save() {
        clickButton("Save");
        assertThat(driver, not(hasContent("This page expects a form submission")));
    }

    public void addTool(String name) {
        clickButton(name);
    }

    public void addJdkAutoInstallation(String name, String version) {
        ensureConfigPage();
        find(by.path("/hudson-model-JDK/tool/name")).sendKeys(name);
        // by default Install automatically is checked
        WebElement select = find(by.path("/hudson-model-JDK/tool/properties/hudson-tools-InstallSourceProperty/installers/id"));
        select.findElement(by.xpath("//option[@value='%s']",version)).click();
        find(by.path("/hudson-model-JDK/tool/properties/hudson-tools-InstallSourceProperty/installers/acceptLicense")).click();
    }
}
