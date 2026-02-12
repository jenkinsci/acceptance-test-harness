package org.jenkinsci.test.acceptance.plugins.gitlab_plugin;

import org.jenkinsci.test.acceptance.plugins.credentials.BaseStandardCredentials;
import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Describable;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;
import org.jenkinsci.test.acceptance.po.PageObject;

@Describable("GitLab Personal Access Token")
public class GitLabPersonalAccessTokenCredential extends BaseStandardCredentials {

    private Control token = control(by.path("/credentials/token"));

    public GitLabPersonalAccessTokenCredential(PageObject context, String path) {
        super(context, path);
    }

    public GitLabPersonalAccessTokenCredential(PageAreaImpl area, String relativePath) {
        super(area, relativePath);
    }

    public void setToken(String gitLabToken) {
        token.set(gitLabToken);
    }
}
