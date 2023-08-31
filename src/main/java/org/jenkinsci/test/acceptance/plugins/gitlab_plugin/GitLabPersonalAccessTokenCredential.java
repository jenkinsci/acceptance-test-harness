package org.jenkinsci.test.acceptance.plugins.gitlab_plugin;

import org.jenkinsci.test.acceptance.plugins.credentials.BaseStandardCredentials;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

import static org.jenkinsci.test.acceptance.Matchers.hasContent;

@Describable("GitLab Personal Access Token")
public class GitLabPersonalAccessTokenCredential extends BaseStandardCredentials {

    public GitLabPersonalAccessTokenCredential(PageObject context, String path) {
        super(context, path);
    }

    public GitLabPersonalAccessTokenCredential(PageAreaImpl area, String relativePath) {
        super(area, relativePath);
    }

    public void setToken(String token) {
        control(by.path("/credentials/token")).set(token);
    }

    public void create() {
        control(by.path("/Submit")).click();
        waitFor(driver, hasContent("Global credentials"), 2);
    }
}
