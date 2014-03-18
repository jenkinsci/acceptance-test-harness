package org.jenkinsci.test.acceptance.po;

import org.openqa.selenium.WebElement;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Page object for the system configuration page.
 *
 * @author Kohsuke Kawaguchi
 */
public class JenkinsConfig extends PageObject {
    public final Jenkins jenkins;

    public JenkinsConfig(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url("configure"));
        this.jenkins = jenkins;
    }

    public void enterOracleCredential(String login, String password) {
        jenkins.visit("descriptorByName/hudson.tools.JDKInstaller/enterCredential");
        find(by.input("username")).sendKeys(login);
        find(by.input("password")).sendKeys(password);
        clickButton("OK");
        clickButton("Close");
    }

    public void configure() {
        jenkins.configure();
    }

    public void save() {
        clickButton("Save");
        assertThat(driver, not(hasContent("This page expects a form submission")));
    }

    public void addTool(String name) {
        clickButton(name);
    }

    public <T extends ToolInstallation> T addTool(Class<T> type) {
        jenkins.ensureConfigPage();
        String name = type.getAnnotation(ToolInstallationPageObject.class).value();

        clickButton("Add " + name);
        sleep(100);
        String path = find(by.button("Delete " + name)).getAttribute("path");
        String prefix = path.substring(0, path.length() - 18);

        try {
            T tool = type.getConstructor(JenkinsConfig.class, String.class).newInstance(this, prefix);
            {// TODO do not leave the page
                jenkins.getLogger("all").waitForLogged(tool.updatesPattern());
                configure();
                clickButton("Add " + name);
                sleep(100);
            }
            return tool;
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    public void addJdkAutoInstallation(String name, String version) {
        jenkins.ensureConfigPage();
        find(by.path("/hudson-model-JDK/tool/name")).sendKeys(name);
        // by default Install automatically is checked
        WebElement select = find(by.path("/hudson-model-JDK/tool/properties/hudson-tools-InstallSourceProperty/installers/id"));
        select.findElement(by.xpath("//option[@value='%s']",version)).click();
        find(by.path("/hudson-model-JDK/tool/properties/hudson-tools-InstallSourceProperty/installers/acceptLicense")).click();
    }
}
