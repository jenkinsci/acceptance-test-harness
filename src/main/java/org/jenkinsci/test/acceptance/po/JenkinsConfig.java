package org.jenkinsci.test.acceptance.po;

import java.net.URL;
import org.openqa.selenium.By;

/**
 * Page object for the system configuration page.
 *
 * @author Kohsuke Kawaguchi
 */
public class JenkinsConfig extends ConfigurablePageObject {
    public final Jenkins jenkins;

    public final Control quietPeriod = control("/jenkins-model-GlobalQuietPeriodConfiguration/quietPeriod");

    public JenkinsConfig(Jenkins jenkins) {
        super(jenkins, jenkins.url("configure"));
        this.jenkins = jenkins;
    }

    @Override
    public URL getConfigUrl() {
        return url;
    }

    public <T extends ArtifactManagement.Factory> T addArtifactManager(Class<T> type) {
        jenkins.ensureConfigPage();

        return new ArtifactManagement(this).add(type);
    }

    public void clearArtifactManagers() {
        jenkins.ensureConfigPage();
        new ArtifactManagement(this).clear();
    }

    public void setJenkinsUrl(String url) {
        control("/jenkins-model-JenkinsLocationConfiguration/url").set(url);
    }

    public void setShell(String path) {
        control("/hudson-tasks-Shell/shell").set(path);
    }

    public void setQuietPeriod(int seconds) {
        quietPeriod.set(seconds);
    }

    public void setDescription(String desc) {
        control("/system_message").set(desc);
    }

    public String getHomeDirectory() {
        ensureConfigPage();

        return driver.findElement(By.xpath("//div[contains(text(), 'Home directory')]//..//*[@class='setting-main']"))
                .getText();
    }
}
