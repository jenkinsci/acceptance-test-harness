package org.jenkinsci.test.acceptance.po;

import groovy.lang.Closure;
import org.jenkinsci.test.acceptance.cucumber.By2;
import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.concurrent.Callable;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class JenkinsConfig extends PageObject {
    public final Jenkins jenkins;

    public JenkinsConfig(Jenkins jenkins) throws Exception {
        super(jenkins.injector,new URL(jenkins.url,"configure"));
        this.jenkins = jenkins;
    }

    public void enterOracleCredential(String login, String password) throws Exception {
        visit("descriptorByName/hudson.tools.JDKInstaller/enterCredential");
        find(By2.input("username")).sendKeys(login);
        find(By2.input("password")).sendKeys(password);
        clickButton("OK");
        clickButton("Close");
    }

    public void configure(Closure body) throws Exception {
        configure();
        body.call(this);
        save();
    }

    public <T> T configure(Callable<T> body) throws Exception {
        configure();
        T v = body.call();
        save();
        return v;
    }

    public void configure() throws Exception {
        open();
    }

    public void save() {
        clickButton("Save");
        assertThat(driver, not(hasContent("This page expects a form submission")));
    }

    public void addTool(String name) {
        clickButton(name);
    }

    public void addJdkAutoInstallation(String name, String version) throws Exception {
        ensureConfigPage();
        find(By2.path("/hudson-model-JDK/tool/name")).sendKeys(name);
        // by default Install automatically is checked
        WebElement select = find(By2.path("/hudson-model-JDK/tool/properties/hudson-tools-InstallSourceProperty/installers/id"));
        select.findElement(By2.xpath("//option[@value='%s']",version)).click();
        find(By2.path("/hudson-model-JDK/tool/properties/hudson-tools-InstallSourceProperty/installers/acceptLicense")).click();
    }
}
