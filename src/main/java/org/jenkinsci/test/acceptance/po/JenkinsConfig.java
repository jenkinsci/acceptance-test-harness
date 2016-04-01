package org.jenkinsci.test.acceptance.po;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jenkinsci.test.acceptance.Matchers.hasContent;

import org.openqa.selenium.WebElement;

import java.net.URL;
import java.util.List;

/**
 * Page object for the system configuration page.
 *
 * @author Kohsuke Kawaguchi
 */
public class JenkinsConfig extends ConfigurablePageObject {
    public final Jenkins jenkins;

    public final Control numExecutors = control("/jenkins-model-MasterBuildConfiguration/numExecutors");

    public final Control addCloudButton = control("/jenkins-model-GlobalCloudConfiguration/hetero-list-add[cloud]");

    public JenkinsConfig(Jenkins jenkins) {
        super(jenkins.injector, jenkins.url("configure"));
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

    public <T extends Cloud> T addCloud(Class<T> type) {
        jenkins.ensureConfigPage();

        addCloudButton.selectDropdownMenu(type);

        List<WebElement> all = all(by.name("cloud"));
        WebElement last = all.get(all.size()-1);

        return newInstance(type, this, last.getAttribute("path"));
    }

    public void setJenkinsUrl(String url) {
        control("/jenkins-model-JenkinsLocationConfiguration/url").set(url);
    }
    
    public void setShell(String path) {
        control("/hudson-tasks-Shell/shell").set(path);
    }
}
