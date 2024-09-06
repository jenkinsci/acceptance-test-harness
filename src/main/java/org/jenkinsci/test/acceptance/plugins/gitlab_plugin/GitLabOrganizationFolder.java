package org.jenkinsci.test.acceptance.plugins.gitlab_plugin;

import com.google.inject.Injector;
import java.net.URL;
import java.time.Duration;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.Folder;

@Describable("jenkins.branch.OrganizationFolder")
public class GitLabOrganizationFolder extends Folder {
    public GitLabOrganizationFolder(Injector injector, URL url, String name) {
        super(injector, url, name);
    }

    public void create(String owner) {
        control(by.path("/hetero-list-add[navigators]")).click();
        find(by.partialLinkText("GitLab Group")).click();
        find(by.path("/navigators/projectOwner")).sendKeys(owner);
    }

    @Override
    public URL getConfigUrl() {
        return null;
    }

    public String getCheckLog() {
        return driver.getPageSource();
    }

    public GitLabOrganizationFolder waitForCheckFinished(final int timeout) {
        waitFor()
                .withTimeout(Duration.ofSeconds(timeout))
                .until(() -> GitLabOrganizationFolder.this.getCheckLog().contains("Finished: "));

        return this;
    }
}
