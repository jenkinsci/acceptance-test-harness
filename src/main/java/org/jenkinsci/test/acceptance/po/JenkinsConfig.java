package org.jenkinsci.test.acceptance.po;

import static org.hamcrest.CoreMatchers.*;
import static org.jenkinsci.test.acceptance.Matchers.*;

/**
 * Page object for the system configuration page.
 *
 * @author Kohsuke Kawaguchi
 */
public class JenkinsConfig extends PageObject {
    public final Jenkins jenkins;

    public final Control numExecutors = control("/jenkins-model-MasterBuildConfiguration/numExecutors");

    public JenkinsConfig(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url("configure"));
        this.jenkins = jenkins;
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

        String name = type.getAnnotation(ToolInstallationPageObject.class).name();

        clickButton("Add " + name);
        sleep(100);
        String path = find(by.button("Delete " + name)).getAttribute("path");
        String prefix = path.substring(0, path.length() - 18);

        return newInstance(type, this, prefix);
    }
}
